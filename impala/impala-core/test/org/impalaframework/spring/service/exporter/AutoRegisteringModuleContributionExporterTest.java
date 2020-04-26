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

package org.impalaframework.spring.service.exporter;

import java.util.Collections;

import org.easymock.EasyMock;
import org.impalaframework.exception.ExecutionException;
import org.springframework.beans.factory.BeanFactory;

import junit.framework.TestCase;

public class AutoRegisteringModuleContributionExporterTest extends TestCase {

    private AutoRegisteringModuleContributionExporter exporter;

    @Override
    protected void setUp() throws Exception {
        exporter = new AutoRegisteringModuleContributionExporter();
    }

    public final void testInvalidGetBeanDefinitionRegistry() {
        try {
            exporter.getBeanDefinitionRegistry(EasyMock.createMock(BeanFactory.class));
            fail();
        }
        catch (ExecutionException e) {
            assertEquals(
                    "Cannot use " + AutoRegisteringModuleContributionExporter.class.getName() +
                    " with bean factory which does not implement org.springframework.beans.factory.support.BeanDefinitionRegistry",
                    e.getMessage());
        }
    }

    public void testGetContributionClasses() throws Exception {
        exporter.checkContributionClasses(Collections.emptyList(),
                "beanName", "java.util.List,java.io.Serializable");
    }

    public void testInvalidContributionClasses() throws Exception {
        try {
            exporter.checkContributionClasses(Collections.emptyList(), "beanName", "java.util.List,java.util.Map");
            fail();
        }
        catch (ExecutionException e) {
            assertEquals("Bean 'beanName' is not instance of type java.util.Map, declared in type list 'java.util.List,java.util.Map'", e.getMessage());
        }
    }

}
