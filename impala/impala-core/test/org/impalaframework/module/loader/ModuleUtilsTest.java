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

package org.impalaframework.module.loader;

import org.easymock.EasyMock;
import org.impalaframework.exception.ExecutionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import junit.framework.TestCase;

/**
 * @author Phil Zoio
 */
public class ModuleUtilsTest extends TestCase {
    
    public void testCastToBeanDefinitionRegistry() {
        BeanDefinitionRegistry bdr = ModuleUtils.castToBeanDefinitionRegistry(new DefaultListableBeanFactory());
        assertNotNull(bdr);
    
        try {
            ModuleUtils.castToBeanDefinitionRegistry(EasyMock.createMock(ConfigurableListableBeanFactory.class));
        }
        catch (ExecutionException e) {
            assertTrue(e.getMessage().contains("is not an instance of BeanDefinitionRegistry"));
        }
    }

}
