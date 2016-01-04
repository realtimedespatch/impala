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

package org.impalaframework.facade;

import java.io.File;

import junit.framework.TestCase;

import org.impalaframework.file.FileMonitor;
import org.impalaframework.module.ModuleDefinitionSource;
import org.impalaframework.module.RootModuleDefinition;
import org.impalaframework.module.definition.ModuleTypes;
import org.impalaframework.module.loader.ModuleLoaderRegistry;
import org.impalaframework.module.modification.ModificationExtractorRegistry;
import org.impalaframework.module.source.SimpleModuleDefinitionSource;
import org.impalaframework.module.spi.Application;
import org.impalaframework.module.spi.ApplicationManager;
import org.impalaframework.module.spi.ModificationExtractorType;
import org.impalaframework.module.spi.ModuleStateHolder;
import org.impalaframework.module.spi.TransitionManager;
import org.impalaframework.module.spi.TransitionSet;
import org.impalaframework.spring.module.SpringModuleUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BootstrapContextTest extends TestCase {

    private static final String plugin1 = "sample-module1";

    private static final String plugin2 = "sample-module2";

    public void testBootstrapContext() throws Exception {
    	
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"META-INF/impala-bootstrap.xml"});
        ModificationExtractorRegistry calculatorRegistry = (ModificationExtractorRegistry) context.getBean("modificationExtractorRegistry");
        ModuleLoaderRegistry registry = (ModuleLoaderRegistry) context.getBean("moduleLoaderRegistry");
        
        assertNotNull(registry.getModuleLoader("spring-"+ModuleTypes.ROOT));
        assertNotNull(registry.getModuleLoader("spring-"+ModuleTypes.APPLICATION));

        ApplicationManager applicationManager = (ApplicationManager) context.getBean("applicationManager");
        Application application = applicationManager.getCurrentApplication();
        
        RootModuleDefinition definition = new Provider().getModuleDefinition();
        TransitionSet transitions = calculatorRegistry.getModificationExtractor(ModificationExtractorType.STRICT).getTransitions(application, null, definition);
        TransitionManager transitionManager = (TransitionManager) context.getBean("transitionManager");
        
        ModuleStateHolder moduleStateHolder = application.getModuleStateHolder();
        transitionManager.processTransitions(moduleStateHolder, application, transitions);

        ConfigurableApplicationContext parentContext = SpringModuleUtils.getRootSpringContext(moduleStateHolder);
        FileMonitor bean = (FileMonitor) parentContext.getBean("bean1");
        bean.lastModified((File) null);
        
        context.close();
    }

    class Provider implements ModuleDefinitionSource {
        ModuleDefinitionSource spec = new SimpleModuleDefinitionSource("impala-core", new String[] { "parentTestContext.xml" }, new String[] { plugin1, plugin2 });

        public RootModuleDefinition getModuleDefinition() {
            return spec.getModuleDefinition();
        }
    }
}
