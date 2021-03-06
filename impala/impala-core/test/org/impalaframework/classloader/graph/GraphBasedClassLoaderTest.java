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

package org.impalaframework.classloader.graph;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.impalaframework.exception.InvalidStateException;
import org.impalaframework.module.ModuleDefinition;
import org.impalaframework.module.RootModuleDefinition;
import org.impalaframework.module.definition.DependencyManager;
import org.impalaframework.module.definition.ModuleTypes;
import org.impalaframework.module.definition.SimpleModuleDefinition;
import org.impalaframework.module.holder.graph.GraphClassLoaderFactory;
import org.impalaframework.module.holder.graph.GraphClassLoaderRegistry;
import org.impalaframework.util.PropertyUtils;
import org.springframework.core.io.support.PropertiesLoaderUtils;

public class GraphBasedClassLoaderTest extends TestCase {

    private ModuleDefinition aDefinition;
    private ModuleDefinition cDefinition;
    private ModuleDefinition eDefinition;
    private TestDependencyManager dependencyManager;
    private GraphClassLoaderFactory factory;
    private ModuleDefinition bDefinition;
    private GraphClassLoaderRegistry classLoaderRegistry;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        List<ModuleDefinition> definitions = new ArrayList<ModuleDefinition>();
        
        aDefinition = newDefinition(definitions, "a");
        bDefinition = newDefinition(definitions, "b", "a");
        cDefinition = newDefinition(definitions, "c");
        newDefinition(definitions, "d", "b");
        eDefinition = newDefinition(definitions, "e", "c,d");
        newDefinition(definitions, "f", "b,e");
        newDefinition(definitions, "g", "c,d,f");   
        
        /*
        a
        b depends on a
        c
        d depends on b
        e depends on c, d
        f depends on b, e
        g on c, d, f
         */
        
        dependencyManager = new TestDependencyManager(definitions);
        factory = new GraphClassLoaderFactory();
        classLoaderRegistry = new GraphClassLoaderRegistry();
        factory.setModuleLocationResolver(new TestClassResolver());
        dependencyManager.unfreeze();
    }
    
    public void testFindResources() throws Exception {

        //finds copy in impala-core/bin directory
        GraphClassLoader eClassLoader = factory.newClassLoader(classLoaderRegistry, dependencyManager, eDefinition);
        final ArrayList<URL> eList = Collections.list(eClassLoader.getLocalResources("beanset.properties"));
        assertEquals(0, eList.size());
        
        //note that bClassLoader finds two copies, as there is also a copy in the module-b/bin directory
        //however does not find copy in impala-core directory
        GraphClassLoader bClassLoader = factory.newClassLoader(classLoaderRegistry, dependencyManager, bDefinition);
        final ArrayList<URL> bList = Collections.list(bClassLoader.getLocalResources("beanset.properties"));
        assertEquals(1, bList.size());
        System.out.println(bList.get(0));
    }
    
    public void testFreeze() throws Exception {
        
        dependencyManager.freeze();
        try {
            dependencyManager.addModule("module-a", new SimpleModuleDefinition("another"));
            fail();
        } catch (InvalidStateException e) {
            assertTrue(e.getMessage().contains("frozen"));
        }
        
        try {
            dependencyManager.removeModule("module-a");
            fail();
        } catch (InvalidStateException e) {
            assertTrue(e.getMessage().contains("frozen"));
        }
        
    }
    
    public void testResourceLoading() throws Exception {
        ClassLoader aClassLoader = factory.newClassLoader(classLoaderRegistry, dependencyManager, aDefinition);
        URL resource = aClassLoader.getResource("moduleA.txt");
        assertNotNull(resource);
        
        URL object = aClassLoader.getResource("java/lang/Object.class");
        assertNotNull(object);
        
        ClassLoader bClassLoader = factory.newClassLoader(classLoaderRegistry, dependencyManager, bDefinition);
        resource = bClassLoader.getResource("moduleA.txt");
        assertNotNull(resource);

        checkBeansetPropValue("modApropvalue", aClassLoader);
        checkBeansetPropValue("modBpropvalue", bClassLoader);
    }

    private void checkBeansetPropValue(String expected, ClassLoader classLoader) {
        final URL beansetResource = classLoader.getResource("beanset.properties");
        final Properties bProps = PropertyUtils.loadProperties(beansetResource);
        assertEquals(expected, bProps.getProperty("moduleAproperties"));
    }
    
    public void testMultiResourceLoading() throws Exception {
        ClassLoader aClassLoader = factory.newClassLoader(classLoaderRegistry, dependencyManager, aDefinition);
        Properties loadAllProperties = PropertiesLoaderUtils.loadAllProperties("beanset.properties", aClassLoader);
        
        //test that properties from beanset.properties in moduleA class space and on default class path are picked up
        assertTrue(loadAllProperties.containsKey("moduleAproperties"));
        assertTrue(loadAllProperties.containsKey("set3"));
    }

    public void testClassLoader() throws Exception {

        ClassLoader eClassLoader = factory.newClassLoader(classLoaderRegistry, dependencyManager, eDefinition);
        System.out.println(eClassLoader.toString());
        
        System.out.println(eClassLoader.loadClass("E"));
        System.out.println(eClassLoader.loadClass("EImpl"));
        System.out.println(eClassLoader.loadClass("C"));
        System.out.println(eClassLoader.loadClass("B"));
        System.out.println(eClassLoader.loadClass("A"));
        
        Object cfromE = eClassLoader.loadClass("CImpl").newInstance();
        
        ClassLoader cClassLoader = factory.newClassLoader(classLoaderRegistry, dependencyManager, cDefinition);
        System.out.println(cClassLoader.toString());
        
        Object cfromC = cClassLoader.loadClass("CImpl").newInstance();
        
        assertTrue(cfromC.getClass().isAssignableFrom(cfromE.getClass()));
        
        System.out.println("From C class loader: " + cfromC.getClass().getClassLoader());
        System.out.println("From E class loader: " + cfromE.getClass().getClassLoader());

        failToLoad(eClassLoader, "F");
        
        printModuleDependees(dependencyManager, "module-a");
        printModuleDependees(dependencyManager, "module-b");
        printModuleDependees(dependencyManager, "module-c");
        printModuleDependees(dependencyManager, "module-d");
        printModuleDependees(dependencyManager, "module-e");
        printModuleDependees(dependencyManager, "module-f");
        printModuleDependees(dependencyManager, "module-g");
        
        System.out.println("------------------ Removing vertices for c --------------------");
        dependencyManager.removeModule("module-c");

        //notice that any of c's dependents no longer appear now
        printModuleDependees(dependencyManager, "module-a");
        
        //now add c, depending on a
        ModuleDefinition newC = new SimpleModuleDefinition(null, "module-c", ModuleTypes.APPLICATION, null, new String[] {"module-a"}, null, null, null, null, true);
        
        //and e, with c as parent, and depending also on b
        new SimpleModuleDefinition(newC, "module-e", ModuleTypes.APPLICATION, null, new String[]{ "module-b" }, null, null, null, null, true);
        
        dependencyManager.addModule("module-a", newC);
        
        //we should see c and e in the list of dependencies
        printModuleDependees(dependencyManager, "module-a");
        
    }

    private void failToLoad(ClassLoader classLoader,
            final String className) {
        try {
            classLoader.loadClass(className);
            fail();
        } catch (ClassNotFoundException e) {
        }
    }

    private void printModuleDependees(DependencyManager dependencyManager,
            final String moduleName) {
        System.out.println("--------------- Module dependents: " + moduleName);
        final List<ModuleDefinition> dependents = dependencyManager.getOrderedModuleDependants(moduleName);
        for (ModuleDefinition moduleDefinition : dependents) {
            System.out.println(moduleDefinition.getName());
        }
        System.out.println("---------------------------------------------");
    }

    private ModuleDefinition newDefinition(List<ModuleDefinition> list, final String name, final String dependencies) {
        final String[] split = dependencies.split(",");
        for (int i = 0; i < split.length; i++) {
            split[i] = "module-" + split[i];
        }
        ModuleDefinition definition = new SimpleModuleDefinition(null, "module-" + name, ModuleTypes.APPLICATION, null, split, null, null, null, null, true);
        list.add(definition);
        return definition;
    }
    
    private ModuleDefinition newDefinition(List<ModuleDefinition> list, final String name) {
        ModuleDefinition definition = new SimpleModuleDefinition(null, "module-" + name, ModuleTypes.APPLICATION, null, new String[0], null, null, null, null, true);
        list.add(definition);
        return definition;
    }
    
}

class TestDependencyManager extends DependencyManager {

    public TestDependencyManager(RootModuleDefinition rootDefinition) {
        super(rootDefinition);
    }

    public TestDependencyManager(List<ModuleDefinition> definitions) {
        super(definitions);
    }

    @Override
    protected void addModule(String parent, ModuleDefinition moduleDefinition) {
        super.addModule(parent, moduleDefinition);
    }

    @Override
    protected void removeModule(String name) {
        super.removeModule(name);
    }
    
    
    
}

