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

package org.impalaframework.module.source;

import java.util.Arrays;
import java.util.List;

import org.impalaframework.exception.ConfigurationException;
import org.impalaframework.module.ModuleDefinition;
import org.impalaframework.module.RootModuleDefinition;
import org.impalaframework.module.type.TypeReaderRegistry;
import org.impalaframework.module.type.TypeReaderRegistryFactory;
import org.impalaframework.resolver.StandaloneModuleLocationResolver;

import junit.framework.TestCase;

public class IncrementalModuleDefinitionSourceTest extends TestCase {
    
    private RootModuleDefinition rootModuleDefinition;
    private StandaloneModuleLocationResolver resolver;
    private TypeReaderRegistry typeReaderRegistry;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.typeReaderRegistry = TypeReaderRegistryFactory.getTypeReaderRegistry();
    }

    private void setExistingDefinition(String... moduleNames) {
        resolver = new StandaloneModuleLocationResolver();
        InternalModuleDefinitionSource moduleDefinitionSource = new InternalModuleDefinitionSource(typeReaderRegistry, resolver, moduleNames, true);
        rootModuleDefinition = moduleDefinitionSource.getModuleDefinition();
        System.out.println(rootModuleDefinition);
    }

    public void testGetSingleModulesToLoad() {
        setExistingDefinition("impala-core", "sample-module2");
        IncrementalModuleDefinitionSource moduleDefinitionSource = new IncrementalModuleDefinitionSource(resolver, typeReaderRegistry, rootModuleDefinition, "sample-module4");
        moduleDefinitionSource.getModuleDefinition();
        List<String> modulesToLoad = moduleDefinitionSource.getModulesToLoad();
        assertEquals(1, modulesToLoad.size());
        assertTrue(modulesToLoad.contains("sample-module4"));
    }
    
    public void testGetNoModulesToLoad() {
        setExistingDefinition("sample-module4");
        IncrementalModuleDefinitionSource moduleDefinitionSource = new IncrementalModuleDefinitionSource(resolver, typeReaderRegistry, rootModuleDefinition, "sample-module4");
        assertEquals(rootModuleDefinition, moduleDefinitionSource.getModuleDefinition());
        assertTrue(moduleDefinitionSource.getModulesToLoad().isEmpty());
    }
    
    public void testGetMultipleModulesToLoad() {
        setExistingDefinition("impala-core");
        IncrementalModuleDefinitionSource moduleDefinitionSource = new IncrementalModuleDefinitionSource(resolver, typeReaderRegistry, rootModuleDefinition, "sample-module4");
        moduleDefinitionSource.getModuleDefinition();
        List<String> modulesToLoad = moduleDefinitionSource.getModulesToLoad();
        assertEquals(2, modulesToLoad.size());
        assertTrue(modulesToLoad.contains("sample-module4"));
        assertTrue(modulesToLoad.contains("sample-module2"));
    }
    
    public void testLoadFiveAndSix() {
        setExistingDefinition("impala-core");
        IncrementalModuleDefinitionSource moduleDefinitionSource = new IncrementalModuleDefinitionSource(resolver, typeReaderRegistry, rootModuleDefinition, "sample-module6");
        RootModuleDefinition root = moduleDefinitionSource.getModuleDefinition();
        assertTrue(root.hasSibling("sample-module5"));
        
        ModuleDefinition definition5 = root.findChildDefinition("sample-module5", true);
        assertNotNull(definition5);
        assertFalse(root.hasSibling("sample-module6"));
        
        ModuleDefinition definition6 = root.findChildDefinition("sample-module6", true);
        assertNotNull(definition6);
        assertNotNull(definition5.findChildDefinition("sample-module6", true));
        List<String> asList = definition6.getDependentModuleNames(false);
        assertEquals(Arrays.asList("sample-module5", "sample-module4"), asList);
        
        List<String> modulesToLoad = moduleDefinitionSource.getModulesToLoad();
        assertEquals(2, modulesToLoad.size());
        assertTrue(modulesToLoad.contains("sample-module5"));
        assertTrue(modulesToLoad.contains("sample-module6"));
    }
    
    public void testLoadFive() {
        setExistingDefinition("impala-core");
        IncrementalModuleDefinitionSource moduleDefinitionSource = new IncrementalModuleDefinitionSource(resolver, typeReaderRegistry, rootModuleDefinition, "sample-module5");
        
        RootModuleDefinition root = moduleDefinitionSource.getModuleDefinition();
        assertTrue(root.hasSibling("sample-module5"));
        
        List<String> modulesToLoad = moduleDefinitionSource.getModulesToLoad();
        assertEquals(1, modulesToLoad.size());
        assertTrue(modulesToLoad.contains("sample-module5"));
    }
    
    public void testLoadSix() {
        setExistingDefinition("impala-core", "sample-module5");
        IncrementalModuleDefinitionSource moduleDefinitionSource = new IncrementalModuleDefinitionSource(resolver, typeReaderRegistry, rootModuleDefinition, "sample-module6");

        RootModuleDefinition root = moduleDefinitionSource.getModuleDefinition();
        assertTrue(root.hasSibling("sample-module5"));
        
        ModuleDefinition definition5 = root.findChildDefinition("sample-module5", true);
        assertNotNull(definition5);
        ModuleDefinition definition6 = definition5.findChildDefinition("sample-module6", true);
        assertNotNull(definition6);
        
        List<String> modulesToLoad = moduleDefinitionSource.getModulesToLoad();
        assertEquals(1, modulesToLoad.size());
        assertTrue(modulesToLoad.contains("sample-module6"));

        List<String> asList = definition6.getDependentModuleNames(false);
        assertEquals(Arrays.asList("sample-module5", "sample-module4"), asList);
    }
    
    public void testGetModuleDefinitionFourFromCore() {
        setExistingDefinition("impala-core");
        IncrementalModuleDefinitionSource moduleDefinitionSource = new IncrementalModuleDefinitionSource(resolver, typeReaderRegistry, rootModuleDefinition, "sample-module4");
        checkDefinition(moduleDefinitionSource);
    }

    public void testGetModuleDefinitionFourFromTwo() {
        setExistingDefinition("impala-core", "sample-module2");
        IncrementalModuleDefinitionSource moduleDefinitionSource = new IncrementalModuleDefinitionSource(resolver, typeReaderRegistry, rootModuleDefinition, "sample-module4");
        checkDefinition(moduleDefinitionSource);
    }

    public void testGetModuleDefinitionFourFromFour() {
        setExistingDefinition("impala-core", "sample-module2", "sample-module4");
        IncrementalModuleDefinitionSource moduleDefinitionSource = new IncrementalModuleDefinitionSource(resolver, typeReaderRegistry, rootModuleDefinition, "sample-module4");
        checkDefinition(moduleDefinitionSource);
    }

    public void testGetModuleDefinitionDuffFromFour() {
        setExistingDefinition("impala-core", "sample-module2");
        IncrementalModuleDefinitionSource moduleDefinitionSource = new IncrementalModuleDefinitionSource(resolver, typeReaderRegistry, rootModuleDefinition, "duff-module");
        try {
            moduleDefinitionSource.getModuleDefinition();
        } catch (ConfigurationException e) {
            assertTrue(e.getMessage().contains("Application is using internally defined module structure, but no module.properties file is present on the classpath for module 'duff-module'"));
        }
    }

    private void checkDefinition(
            IncrementalModuleDefinitionSource moduleDefinitionSource) {
        RootModuleDefinition newDefinition = moduleDefinitionSource.getModuleDefinition();
        System.out.println("New definition: " + newDefinition);
        
        ModuleDefinition module2 = newDefinition.findChildDefinition("sample-module2", true);
        assertNotNull(module2);
        ModuleDefinition module4 = newDefinition.findChildDefinition("sample-module4", true);
        assertNotNull(module4);
        
        assertTrue(newDefinition.getChildModuleDefinitions().contains(module2));
        assertEquals(1, newDefinition.getChildModuleDefinitions().size());
        assertTrue(module2.getChildModuleDefinitions().contains(module4));
        assertEquals(1, module2.getChildModuleDefinitions().size());
        assertTrue(module4.getChildModuleDefinitions().isEmpty());
    }
}
