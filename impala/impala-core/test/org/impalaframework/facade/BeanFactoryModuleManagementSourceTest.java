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

import junit.framework.TestCase;

import org.impalaframework.util.ObjectUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BeanFactoryModuleManagementSourceTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.clearProperty("bootstrapLocationsResource");
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        System.clearProperty("bootstrapLocationsResource");
    }
    
    public final void testBootstrapBeanFactory() {

        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("META-INF/impala-bootstrap.xml");
        Object bean = appContext.getBean("moduleManagementFacade");
        ModuleManagementFacade facade = ObjectUtils.cast(bean, ModuleManagementFacade.class);

        assertNotNull(facade.getModuleLocationResolver());
        assertNotNull(facade.getModuleLoaderRegistry());
        assertNotNull(facade.getModificationExtractorRegistry());
        assertNotNull(facade.getApplicationManager());
        assertNotNull(facade.getTransitionProcessorRegistry());
        assertNotNull(facade.getModuleLocationResolver());
        assertNotNull(facade.getModuleOperationRegistry());
        
        Object managementFactory = facade.getBean("moduleManagementFacade", ModuleManagementFacade.class);
        assertNotNull(managementFactory);
        
        appContext.close();
    }

}
