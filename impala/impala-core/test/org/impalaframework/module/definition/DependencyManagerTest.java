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

import static org.impalaframework.classloader.graph.GraphTestUtils.assertContainsOnly;
import static org.impalaframework.classloader.graph.GraphTestUtils.assertModules;
import static org.impalaframework.classloader.graph.GraphTestUtils.findDefintion;
import static org.impalaframework.classloader.graph.GraphTestUtils.newDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.impalaframework.module.ModuleDefinition;

import junit.framework.TestCase;


public class DependencyManagerTest extends TestCase {
    
    private SimpleRootModuleDefinition rootDefinition;
    private DependencyManager manager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        rootDefinition = definitionSet1();
        manager = new DependencyManager(rootDefinition);
        manager.unfreeze();
    }
    
    public void testGetDirectDependees() throws Exception {
        assertDirectDependee("a", "root");
        assertDirectDependee("b", "root,e");
        assertDirectDependee("c", "f");
        assertDirectDependee("d", "e,b");
        assertDirectDependee("root", "e");
        assertDirectDependee("e", "f,g");
        assertDirectDependee("f", "g");
        assertDirectDependee("g", null);
    }
    
    public void testGetDependencies() throws Exception {
        assertDependencies("a", "a");
        assertDependencies("b", "d,b");
        assertDependencies("c", "c");
        assertDependencies("d", "d");
        assertDependencies("root", "a,d,b,root");
        assertDependencies("e", "a,d,b,root,e");
        assertDependencies("f", "a,d,c,b,root,e,f");
        assertDependencies("g", "a,d,c,b,root,e,f,g");
    }
    
    public void testGetDependees() throws Exception {
        assertDependees("a", "a,root,e,f,g");
        assertDependees("b", "b,root,e,f,g");
        assertDependees("c", "c,f,g");
        assertDependees("d", "d,b,root,e,f,g");
        assertDependees("root", "root,e,f,g");
        assertDependees("e", "e,f,g");
        assertDependees("f", "f,g");
        assertDependees("g", "g");
    }
    
    public void testAllModules() throws Exception {
        Collection<ModuleDefinition> allModules = manager.getAllModules();
        assertModules("a,c,d,b,root,e,f,g", allModules);
    }
    
    public void testAddH() throws Exception {
        //add h with parent root and depend on h
        manager.addModule("root", new SimpleModuleDefinition(null, "h", ModuleTypes.APPLICATION, null, new String[]{"a"}, null, null, null, null, true));
        Collection<ModuleDefinition> allModules = manager.getAllModules();
        assertModules("a,c,d,b,root,e,h,f,g", allModules);
        assertDependees("root", "root,e,h,f,g");
        assertDependencies("g", "a,d,c,b,root,e,f,g");
        assertDependencies("h", "a,d,b,root,h");
    }
    
    public void testAddI() throws Exception {
        //add i with parent c, and depending on g
        manager.addModule("c", new SimpleModuleDefinition(null, "i", ModuleTypes.APPLICATION, null, new String[]{"c", "g"}, null, null, null, null, true));
        Collection<ModuleDefinition> allModules = manager.getAllModules();
        assertModules("a,c,d,b,root,e,f,g,i", allModules);
        assertDependencies("i", "c,a,d,b,root,e,f,g,i");
        assertDependees("root", "root,e,f,g,i");
        assertDependees("a", "a,root,e,f,g,i");
        assertDependencies("g", "a,d,c,b,root,e,f,g");
    }

    private SimpleRootModuleDefinition definitionSet1() {
        List<ModuleDefinition> definitions = new ArrayList<ModuleDefinition>();
        
        //a has no parent or dependencies
        ModuleDefinition a = newDefinition(definitions, null, "a", null);
        
        //b depends on d but has no parent
        ModuleDefinition b = newDefinition(definitions, null, "b", "d");
        
        //c has no parent or dependencies
        ModuleDefinition c = newDefinition(definitions, null, "c", null);
        
        //d has no parent or dependencies
        ModuleDefinition d = newDefinition(definitions, null, "d", null);
        
        //root has siblings a to d, and depends on a and b
        SimpleRootModuleDefinition root = new SimpleRootModuleDefinition("root", 
                new String[] {"root.xml"}, 
                new String[] {"a", "b"}, 
                null, 
                new ModuleDefinition[] {a, b, c, d}, null, null, true);
        
        //e has parent root, and depends on b an d
        ModuleDefinition e = newDefinition(definitions, root, "e", "b,d");
        
        //has parent e, and depends on c
        newDefinition(definitions, e, "f", "c");

        //has parent e, depends on f
        newDefinition(definitions, e, "g", "f");
        return root;
    }   

    private void assertDependencies(String moduleName, String expected) {
        List<ModuleDefinition> orderedModuleDependencies = manager.getOrderedModuleDependencies(moduleName);
        assertModules(expected, orderedModuleDependencies);
    }

    private void assertDependees(String moduleName, String expected) {
        List<ModuleDefinition> orderedModuleDependencies = manager.getOrderedModuleDependants(moduleName);
        assertModules(expected, orderedModuleDependencies);
    }
    
    private void assertDirectDependee(
            String module,
            String expected) {
        
        ModuleDefinition definition = findDefintion(rootDefinition, module);
        Collection<ModuleDefinition> directDependees = manager.getDirectDependants(definition.getName());
        System.out.println();
        System.out.println(directDependees);
        assertContainsOnly(directDependees, expected);
    }

    
}
