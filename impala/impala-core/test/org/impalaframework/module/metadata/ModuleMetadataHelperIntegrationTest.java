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

package org.impalaframework.module.metadata;

import java.util.Collection;

import junit.framework.TestCase;

import org.impalaframework.exception.NoServiceException;
import org.impalaframework.facade.Impala;
import org.impalaframework.module.ModuleDefinitionSource;
import org.impalaframework.module.RootModuleDefinition;
import org.impalaframework.module.source.InternalModuleDefinitionSource;
import org.impalaframework.module.source.SimpleModuleDefinitionSource;
import org.impalaframework.module.spi.Application;
import org.impalaframework.module.type.TypeReaderRegistry;
import org.impalaframework.module.type.TypeReaderRegistryFactory;
import org.impalaframework.resolver.StandaloneModuleLocationResolver;
import org.springframework.util.StringUtils;

public class ModuleMetadataHelperIntegrationTest extends TestCase {

    private static final String plugin1 = "sample-module1";

    private static final String plugin2 = "sample-module2";

    private static final String plugin4 = "sample-module4";

    public void setUp() {
        Impala.clear();
    }

    public void tearDown() {
        try {
            Impala.clear();
        }
        catch (Exception e) {
        }
    }

    public void testNoInit() {
        try {
            Impala.getRootRuntimeModule();
            fail();
        }
        catch (NoServiceException e) {
        }
    }

    public void testInit() {

        final Test1 test1 = new Test1();
        Impala.init(test1);
        
        final Application currentApplication = Impala.getCurrentApplication();
        ModuleMetadataHelper helper = new ModuleMetadataHelper();
        helper.setApplication(currentApplication);
        
        assertTrue(helper.isModuleDefinitionPresent("impala-core"));
        assertTrue(helper.isModuleDefinitionPresent(plugin1));
        assertFalse(helper.isModuleDefinitionPresent(plugin2));
        
        assertTrue(helper.isModulePresent("impala-core"));
        assertTrue(helper.isModulePresent(plugin1));
        assertFalse(helper.isModulePresent(plugin2));
    }
    
    public void testCapabilities() {
        
        TypeReaderRegistry typeReaderRegistry = TypeReaderRegistryFactory.getTypeReaderRegistry();
        StandaloneModuleLocationResolver resolver = new StandaloneModuleLocationResolver();
        String[] moduleNames = new String[] { 
                "impala-core",
                "sample-module1", 
                "sample-module2", 
                "sample-module3", 
                "sample-module4" };
        InternalModuleDefinitionSource moduleDefinitionSource = new InternalModuleDefinitionSource(typeReaderRegistry, resolver, moduleNames , true);

        Impala.init(moduleDefinitionSource);
        
        final Application currentApplication = Impala.getCurrentApplication();
        ModuleMetadataHelper helper = new ModuleMetadataHelper();
        helper.setApplication(currentApplication);
        
        Collection<String> capabilities = helper.getCapabilities();
        System.out.println(capabilities);
        assertEquals("four,one,three", StringUtils.collectionToCommaDelimitedString(capabilities));
        assertTrue(helper.hasCapability("one"));
        
        assertEquals("impala-core,sample-module1,sample-module2,sample-module3,sample-module4", StringUtils.collectionToCommaDelimitedString(helper.getLoadedModuleNames()));
    }
    
    class Test1 implements ModuleDefinitionSource {
        ModuleDefinitionSource source = new SimpleModuleDefinitionSource("impala-core", new String[] { "parentTestContext.xml" }, new String[] { plugin1 });

        public RootModuleDefinition getModuleDefinition() {
            return source.getModuleDefinition();
        }
    }

    class TestAll implements ModuleDefinitionSource {
        ModuleDefinitionSource source;   public TestAll() {
            source = new SimpleModuleDefinitionSource("impala-core", new String[] { "parentTestContext.xml" }, new String[] { plugin1, plugin2, plugin4 });
        }
        
        public RootModuleDefinition getModuleDefinition() {
            return source.getModuleDefinition();
        }
    }
}
