/*
 * Copyright 2007-2010 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.impalaframework.module.definition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.impalaframework.exception.InvalidStateException;
import org.impalaframework.graph.CyclicDependencyException;
import org.impalaframework.graph.GraphHelper;
import org.impalaframework.graph.Vertex;
import org.impalaframework.module.Freezable;
import org.impalaframework.module.ModuleDefinition;
import org.impalaframework.module.RootModuleDefinition;
import org.springframework.util.Assert;

/**
 * Class with responsibility for identifying dependencies as well as dependents
 * (modules which depend on the modules concerned). Also responsible for
 * ensuring that these dependencies are correctly sorted according to correct
 * module load order, so that modules can be loaded and unloaded in the correct
 * sequence, and so that each module's class loader graph can be built
 * correctly.
 * 
 * @author Phil Zoio
 */
public class DependencyManager implements Freezable {

    private static final Log logger = LogFactory.getLog(DependencyManager.class);

    private Map<String, Vertex> vertexMap = new LinkedHashMap<String, Vertex>();
    private Map<String, Set<Vertex>> dependents = new LinkedHashMap<String, Set<Vertex>>();
    private List<Vertex> sorted;
    private boolean frozen;

    public DependencyManager(List<ModuleDefinition> definitions) {
        super();
        this.buildVertexMap(definitions);
        freeze();
    }
    
    public DependencyManager(RootModuleDefinition rootDefinition) {
        super();
        
        Assert.notNull(rootDefinition, "rootDefintion cannot be null");
        
        List<ModuleDefinition> definitions = new ArrayList<ModuleDefinition>();
        definitions.add(rootDefinition);
        definitions.addAll(rootDefinition.getSiblings());
        
        this.buildVertexMap(definitions);
        
        if (logger.isDebugEnabled()) {
            logger.debug("Vertices after build dependency registry");
            dump();
        }
        freeze();
    }

    /* ********************* Methods to sort dependencies  ********************* */
    
    /**
     * Sorts in <i>reverse</i> order the collection of module definitions. Order determined by the 
     * topological sort order of all {@link ModuleDefinition}s known to this {@link DependencyManager} instance.
     * @see #sort(Collection)
     */
    public List<ModuleDefinition> reverseSort(Collection<ModuleDefinition> sortable) {
        
        Assert.notNull(sortable, "sortable cannot be null");
        
        final List<ModuleDefinition> sorted = doSort(sortable);
        Collections.reverse(sorted);
        
        if (logger.isDebugEnabled()) {
            logger.debug("Reverse sorted module defintions");
            logger.debug("Before: " + ModuleDefinitionUtils.getModuleNamesFromCollection(sortable));
            logger.debug("After: " + ModuleDefinitionUtils.getModuleNamesFromCollection(sorted));
        }
        
        return sorted;
    }
    
    /**
     * Sorts in order the collection of module definitions. Order determined by the 
     * topological sort order of all {@link ModuleDefinition}s known to this {@link DependencyManager} instance.
     * @see #reverseSort(Collection)
     */
    public List<ModuleDefinition> sort(Collection<ModuleDefinition> sortable) {

        Assert.notNull(sortable, "sortable cannot be null");
        
        List<ModuleDefinition> sorted = doSort(sortable);
        
        if (logger.isDebugEnabled()) {
            logger.debug("Reverse sorted module defintions");
            logger.debug("Before: " + ModuleDefinitionUtils.getModuleNamesFromCollection(sortable));
            logger.debug("After: " + ModuleDefinitionUtils.getModuleNamesFromCollection(sorted));
        }
        
        return sorted;
    }
    
    /* ********************* Methods to show dependencies and dependents  ********************* */

    /**
     * Gets ordered list of modules definitions on which a particular named module depends
     */
    public List<ModuleDefinition> getOrderedModuleDependencies(String name) {

        Assert.notNull(name, "name cannot be null");
        
        final List<Vertex> vertices = getVertexDependencyList(name);
        List<ModuleDefinition> moduleDefinitions = getVerticesForModuleDefinitions(vertices);
        
        if (logger.isDebugEnabled()) {
            logger.debug("Ordered dependencies for module '" + name + "': " + ModuleDefinitionUtils.getModuleNamesFromCollection(moduleDefinitions));
        }
        
        return moduleDefinitions;
    }
    
    /**
     * Gets subgraph of named module plus dependents
     */
    public List<ModuleDefinition> getOrderedModuleDependants(String name) {

        Assert.notNull(name, "name cannot be null");
        
        List<Vertex> vertices = getVertexAndOrderedDependants(name);
        List<ModuleDefinition> moduleDefinitions = getVerticesForModuleDefinitions(vertices);
        
        if (logger.isDebugEnabled()) {
            logger.debug("Ordered dependents for module '" + name + "': " + ModuleDefinitionUtils.getModuleNamesFromCollection(moduleDefinitions));
        }
        
        return moduleDefinitions;
    }

    /**
     * Gets the {@link ModuleDefinition} which are direct dependents of the {@link ModuleDefinition} argument.
     */
    public Collection<ModuleDefinition> getDirectDependants(String name) {
        
        Assert.notNull(name, "name cannot be null");
        
        //make sure this module is present
        getRequiredVertex(name);
        
        final Collection<Vertex> vertices = dependents.get(name);
        
        if (vertices == null) {
            return Collections.emptySet();
        }
        
        List<ModuleDefinition> moduleDefinitions = getVerticesForModuleDefinitions(vertices);   
        
        if (logger.isDebugEnabled()) {
            logger.debug("Ordered dependents for module '" + name + "': " + ModuleDefinitionUtils.getModuleNamesFromCollection(moduleDefinitions));
        }       
        
        return moduleDefinitions;
    }
    
    /* ********************* returns all the modules known by the dependency registry **************** */
    
    /**
     * Returns all {@link ModuleDefinition} known to this {@link DependencyManager} instance, returned in topological sort order.
     */
    public Collection<ModuleDefinition> getAllModules() {
        
        final Collection<Vertex> vertices = this.vertexMap.values();
        
        List<Vertex> ordered = populatedOrderedVertices(vertices);
        
        final LinkedHashSet<ModuleDefinition> definitions = new LinkedHashSet<ModuleDefinition>();
        for (Vertex vertex : ordered) {
            definitions.add(vertex.getModuleDefinition());
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("Returning all module defintions: " + ModuleDefinitionUtils.getModuleNamesFromCollection(definitions));
        }   
        
        return definitions;
    }   
    
    /* ********************* Methods to add subgraph of vertices ********************* */
    
    protected void addModule(String parent, ModuleDefinition moduleDefinition) {
        
        ModuleDefinitionUtils.ensureNotFrozen(this);
        
        Assert.notNull(parent, "parent cannot be null");
        Assert.notNull(moduleDefinition, "moduleDefinition cannot be null");
        
        logger.info("With parent '" + parent + "', adding module: " + moduleDefinition);
        
        final Vertex parentVertex = getRequiredVertex(parent);
        
        ModuleDefinition parentDefinition = parentVertex.getModuleDefinition();
        parentDefinition.addChildModuleDefinition(moduleDefinition);
        moduleDefinition.setParentDefinition(parentDefinition);
        
        //now recursively add definitions
        List<Vertex> addedVertices = new ArrayList<Vertex>();
        populateDefinition(addedVertices, moduleDefinition);
        
        populateVertexDependencies(addedVertices);
        
        //rebuild the sorted vertex list
        resort();
        
        if (logger.isInfoEnabled()) {
            logger.info("Added module " + moduleDefinition);
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("Vertices after adding module");
            dump();
        }
    }

    private Vertex getRequiredVertex(String moduleName) {
        final Vertex parentVertex = vertexMap.get(moduleName);
        if (parentVertex == null) {
            
            if (logger.isDebugEnabled()) {
                logger.debug("Module '" + moduleName + "' not found.");
                dump();
            }
            
            throw new InvalidStateException("No module '" 
                    + moduleName + "' is registered with current instance of dependency manager.");
        }
        return parentVertex;
    }
    
    /* ********************* Methods to remove vertices ********************* */
    
    /**
     * Removes the current module as well as any of it's dependents
     */
    protected void removeModule(String name) {
        
        ModuleDefinitionUtils.ensureNotFrozen(this);
        
        Assert.notNull(name, "name cannot be null");
        
        List<Vertex> orderedToRemove = getVertexAndOrderedDependants(name);
        removeVertexInOrder(orderedToRemove);
    }

    /* ********************* Private utility methods ********************* */
    
    private void buildVertexMap(List<ModuleDefinition> definitions) {
        
        Assert.notNull(definitions, "definitions cannot be null");
        
        List<Vertex> addedVertices = new ArrayList<Vertex>();
        for (ModuleDefinition moduleDefinition : definitions) {
            populateDefinition(addedVertices, moduleDefinition);
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("Added vertices: " + addedVertices);
        }
        
        //add the dependency relationships between the added vertices
        populateVertexDependencies(addedVertices);
        
        //rebuild the sorted vertex list
        resort();
    }

    /**
     * Gets the vertices for the modules for which the named module is a dependency
     */
    private List<Vertex> getVertexDependants(String name) {
        
        Assert.notNull(name, "name cannot be null");
        
        final List<Vertex> fullList = new ArrayList<Vertex>(sorted);
        
        List<Vertex> targetList = new ArrayList<Vertex>();
        populateDependants(targetList, name);

        List<Vertex> moduleVertices = new ArrayList<Vertex>();
        
        //iterate over the full list to get the order, but pick out only the module definitions which are dependents
        for (Vertex vertex : fullList) {
            if (targetList.contains(vertex)) {
                moduleVertices.add(vertex);
            }
        }
        return moduleVertices;
    }
    
    /**
     * Gets a list of vertices including the one corresponding with the name, plus its dependents
     * topologically sorted
     */
    private List<Vertex> getVertexAndOrderedDependants(String name) {
        
        Assert.notNull(name, "name cannot be null");
        
        final Vertex current = getRequiredVertex(name);
        
        //get all dependents
        final List<Vertex> dependents = getVertexDependants(name);
        List<Vertex> ordered = getOrderedDependants(current, dependents);
        return ordered;
    }
    
    /**
     * Gets vertices representing the current and its dependents, topologically sorted
     */
    private List<Vertex> getOrderedDependants(final Vertex currentVertex, final List<Vertex> dependents) {
        
        Assert.notNull(currentVertex, "currentVertex cannot be null");      
        Assert.notNull(dependents, "dependents cannot be null");
        
        List<Vertex> ordered = new ArrayList<Vertex>();
        ordered.add(currentVertex);
        ordered.addAll(populatedOrderedVertices(dependents));
        return ordered;
    }

    /**
     * Returns the {@link List} of {@link Vertex} instances on which the
     * {@link Vertex} corresponding with the name parameter depends.
     */
    private List<Vertex> getVertexDependencyList(String name) { 
        
        Assert.notNull(name, "name cannot be null");
        
        Vertex vertex = getRequiredVertex(name);
        
        //list the vertices in the correct order
        final List<Vertex> vertextList = GraphHelper.list(vertex);
        return vertextList;
    }

    /**
     * Returns the vertices contained in <code>sortable</code> according to the topological
     * sort order of vertices known to this {@link DependencyManager} instance.
     */
    private List<Vertex> populatedOrderedVertices(Collection<Vertex> sortable) {
        
        Assert.notNull(sortable, "sortable cannot be null");
        
        Collection<Vertex> copy = new HashSet<Vertex>(sortable);
        List<Vertex> ordered = new ArrayList<Vertex>();
        
        //get the ordered to remove list
        List<Vertex> sorted = this.sorted;
        for (Vertex vertex : sorted) {
            if (copy.contains(vertex)) {
                ordered.add(vertex);
                copy.remove(vertex);
            }
        }
        
        if (!copy.isEmpty()) {
            //should not be possible, as all of the modules have already been converted into vertexes. Hence
            //would be the sign of an non-obvious programming error
            throw new InvalidStateException("Sortable list contains modules not known by the current instance of dependency registry: " 
                    + GraphHelper.getModuleNamesFromCollection(copy));
        }
        
        return ordered;
    }
    
    /**
     * Get the list of {@link ModuleDefinition} corresponding with the vertex {@link Collection}
     */
    private static List<ModuleDefinition> getVerticesForModuleDefinitions(Collection<Vertex> moduleVertices) {

        Assert.notNull(moduleVertices, "moduleVertices cannot be null");
        
        List<ModuleDefinition> moduleDefinitions = new ArrayList<ModuleDefinition>();
        
        for (Vertex vertex : moduleVertices) {
            moduleDefinitions.add(vertex.getModuleDefinition());
        }
        
        return moduleDefinitions;
    }
    
    private List<Vertex> getVerticesForModules(Collection<ModuleDefinition> definitions) {

        Assert.notNull(definitions, "definitions cannot be null");
        
        List<Vertex> vertices = new ArrayList<Vertex>();
        for (ModuleDefinition moduleDefinition : definitions) {
            final Vertex vertex = getRequiredVertex(moduleDefinition.getName());
            
            vertices.add(vertex);
        }
        return vertices;
    }

    /**
     * Sets up the dependency relationship between a vertex and its dependency
     * @param vertex the vertex (dependant)
     * @param dependentVertex the dependency, that is the vertex the dependant depends on
     */
    private void populateVertexDependency(Vertex vertex, Vertex dependentVertex) {
        
        Assert.notNull(vertex, "vertex cannot be null");
        Assert.notNull(dependentVertex, "dependentVertex cannot be null");
        
        vertex.addDependency(dependentVertex);
        
        final String dependantName = dependentVertex.getName();
        
        Set<Vertex> list = dependents.get(dependantName);
        if (list == null) {
            list = new HashSet<Vertex>();
            dependents.put(dependantName, list);
        }
        list.add(vertex);
    }

    /**
     * Stores vertex for current module definition. Assumes none present
     * @return 
     */
    private Vertex populateVertex(ModuleDefinition moduleDefinition) {
        
        Assert.notNull(moduleDefinition, "moduleDefinition cannot be null");
        
        String name = moduleDefinition.getName();
        final Vertex vertex = new Vertex(moduleDefinition);
        vertexMap.put(name, vertex);
        return vertex;
    }

    /**
     * Recursive method to build the list of dependents for a particular named module.
     * Does not order the dependencies in any way
     */
    private void populateDependants(List<Vertex> targetList, String name) { 
        
        Assert.notNull(targetList, "targetList cannot be null");
        Assert.notNull(name, "name cannot be null");
        
        //recursively build the dependant list
        Set<Vertex> dependantList = dependents.get(name);
        if (dependantList != null) {
            targetList.addAll(dependantList);
            for (Vertex vertex : dependantList) {
                populateDependants(targetList, vertex.getName());
            }
        }
    }

    /**
     * Recursive method to add module definition.
     * @param addedVertices 
     */
    private void populateDefinition(List<Vertex> addedVertices, ModuleDefinition moduleDefinition) {

        Assert.notNull(addedVertices, "addedVertices cannot be null");
        Assert.notNull(moduleDefinition, "moduleDefinition cannot be null");
        
        addedVertices.add(populateVertex(moduleDefinition));
    
        final Collection<ModuleDefinition> childDefinitions = moduleDefinition.getChildModuleDefinitions();
        
        for (ModuleDefinition childDefinition : childDefinitions) {
            populateDefinition(addedVertices, childDefinition);
        }
    }

    /**
     * Sets up the dependency relationships between vertices based on the 
     * dependency module names of the ModuleDefinitions
     * @param addedVertices 
     */
    private void populateVertexDependencies(List<Vertex> addedVertices) { 
        
        Assert.notNull(addedVertices, "addedVertices cannot be null");
        
        for (Vertex vertex : addedVertices) {
            populateVertexDependencies(vertex);
        }
    }

    /**
     * Sets up the dependencies for a particular named module
     */
    private void populateVertexDependencies(Vertex vertex) {
        
        Assert.notNull(vertex, "vertex cannot be null");
        
        final ModuleDefinition moduleDefinition = vertex.getModuleDefinition();
        
        populateDependencies(vertex, moduleDefinition, false);
        populateDependencies(vertex, moduleDefinition, true);
    }

    /**
     * Populates either optional or mandatory dependencies, depending on whether optional or not
     */
    private void populateDependencies(Vertex vertex, ModuleDefinition moduleDefinition, boolean optional) {
        
        final List<String> dependentModuleNames = moduleDefinition.getDependentModuleNames(optional);
        for (String dependent : dependentModuleNames) {
            
            final Vertex dependentVertex = vertexMap.get(dependent);
            
            if (dependentVertex == null) {
                if (!optional) {
                    throw new InvalidStateException(String.format("Unable to find entry for dependency named named '%s' for module definition '%s'.", 
                    	dependent, 
                    	moduleDefinition.getName()));
                }                
            } else {
                //register the vertex dependency
                populateVertexDependency(vertex, dependentVertex);
            }
        }
    }

    /**
     * Deregister from the dependencies list of dependents and the vertex map
     */
    private void removeVertexInOrder(List<Vertex> vertices) {
        
        Assert.notNull(vertices, "vertices cannot be null");
        
        for (Vertex vertex : vertices) {
            removeVertex(vertex);
        }
    }

    private void removeVertex(Vertex vertex) {
        
        Assert.notNull(vertex, "vertex cannot be null");
        
        final List<Vertex> dependencies = vertex.getDependencies();
        for (Vertex dependency : dependencies) {
            final String dependencyName = dependency.getName();
            final Set<Vertex> dependents = this.dependents.get(dependencyName);
            dependents.remove(dependency);
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("Removing vertex " + vertex.getName());
        }
        
        this.sorted.remove(vertex);
        this.vertexMap.remove(vertex.getName());
    } 

    private List<ModuleDefinition> doSort(Collection<ModuleDefinition> sortable) {
        
        Assert.notNull(sortable, "vertex cannot be null");
        
        //convert module definitions to vertices
        List<Vertex> vertices = this.getVerticesForModules(sortable);
        
        //sort these based in order
        List<Vertex> ordered = populatedOrderedVertices(vertices);
        
        //reconvert back to vertices
        return getVerticesForModuleDefinitions(ordered);
    }

    private void resort() {
        final List<Vertex> vertices = new ArrayList<Vertex>(vertexMap.values());
        for (Vertex vertex : vertices) {
            vertex.reset();
        }
        try {
            GraphHelper.topologicalSort(vertices);
        } catch (CyclicDependencyException e) {
            logCyclicDependencyError(vertices);
            throw e;
        }
        this.sorted = vertices;
    }

    private void logCyclicDependencyError(final List<Vertex> vertices) {
        logger.error("Cyclic dependency found. Outputting vertex dependencies:");
        for (Vertex vertex : vertices) {
            logger.error(vertex.getName() + ": ");
            final List<Vertex> dependencies = vertex.getDependencies();
            for (Vertex dependency : dependencies) {
                logger.error("  " + dependency.getName() + ",");
            }
            logger.error("-----------");
        }
    }
    
    private void dump() {
        if (!logger.isDebugEnabled()) return;
        logger.debug("Dependency registry state. Sorted vertices:");
        for (Vertex vertex : this.sorted) {
            logger.debug(vertex);
        }
    }

    public void freeze() {
        this.frozen = true;
    }

    public void unfreeze() {
        this.frozen = false;
    }

    public boolean isFrozen() {
        return frozen;
    }
    
}
