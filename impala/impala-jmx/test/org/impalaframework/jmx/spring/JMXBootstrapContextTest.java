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

package org.impalaframework.jmx.spring;

import junit.framework.TestCase;

import org.impalaframework.facade.ModuleManagementFacade;
import org.impalaframework.module.ModuleDefinitionSource;
import org.impalaframework.module.RootModuleDefinition;
import org.impalaframework.module.source.SimpleModuleDefinitionSource;
import org.impalaframework.module.spi.Application;
import org.impalaframework.module.spi.ApplicationManager;
import org.impalaframework.module.spi.ModificationExtractorType;
import org.impalaframework.module.spi.ModuleStateHolder;
import org.impalaframework.module.spi.TransitionManager;
import org.impalaframework.module.spi.TransitionSet;
import org.impalaframework.util.ObjectUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class JMXBootstrapContextTest extends TestCase {

    private static final String plugin1 = "sample-module1";

    private static final String plugin2 = "sample-module2"; 
    
    private static final String rootProjectName = "impala-core";

    private ModuleManagementFacade facade;

    public void tearDown() {
        try {
            facade.close();
        }
        catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public void testBootstrapContext() throws Exception {
    	ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] {
                "META-INF/impala-bootstrap.xml",
                "META-INF/impala-jmx-bootstrap.xml" ,
                "META-INF/impala-jmx-adaptor-bootstrap.xml"});
        Object bean = context.getBean("moduleManagementFacade");
        facade = ObjectUtils.cast(bean, ModuleManagementFacade.class);
        RootModuleDefinition moduleDefinition = new Provider().getModuleDefinition();

        TransitionSet transitions = facade.getModificationExtractorRegistry()
                .getModificationExtractor(ModificationExtractorType.STICKY).getTransitions(null, null, moduleDefinition);

        ModuleStateHolder moduleStateHolder = facade.getApplicationManager().getCurrentApplication().getModuleStateHolder();
        TransitionManager transitionManager = facade.getTransitionManager();
        
        ApplicationManager applicationManager = facade.getApplicationManager();
        Application application = applicationManager.getCurrentApplication();
        
        transitionManager.processTransitions(moduleStateHolder, application, transitions);

        ModuleManagementOperations operations = (ModuleManagementOperations) facade.getBean("moduleManagementOperations");

        assertEquals("Could not find module duff", operations.reloadModule("duff"));
        assertEquals("Successfully reloaded sample-module1", operations.reloadModule(plugin1));

        context.close();
    }

    class Provider implements ModuleDefinitionSource {
        ModuleDefinitionSource source = new SimpleModuleDefinitionSource(rootProjectName, new String[] { "parentTestContext.xml" }, new String[] { plugin1, plugin2 });

        public RootModuleDefinition getModuleDefinition() {
            return source.getModuleDefinition();
        }
    }
}
