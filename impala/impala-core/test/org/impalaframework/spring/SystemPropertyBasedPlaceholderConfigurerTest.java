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

package org.impalaframework.spring;

import junit.framework.TestCase;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Phil Zoio
 */
public class SystemPropertyBasedPlaceholderConfigurerTest extends TestCase {

    public void test() {
        System.setProperty(SystemPropertyBasedPlaceholderConfigurer.DEFAULT_PROPERTY_FOLDER_SYSTEM_PROPERTY, "../impala-core/files");
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("externalconfig/spring-context.xml");
        assertEquals("bean10_value_modified", context.getBean("bean10"));
        assertEquals("bean11_value_modified", context.getBean("bean11"));
        assertEquals("bean20_value", context.getBean("bean20"));
        assertEquals("bean21_value_modified", context.getBean("bean21"));
        assertEquals("bean22_value_modified", context.getBean("bean22"));
        assertEquals("bean30_value", context.getBean("bean30"));
        assertEquals("bean40_value_modified", context.getBean("bean40"));
        assertEquals("bean50_value", context.getBean("bean50"));
        
        context.close();
    }
    
    public void testPlaceholder() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("placeholder/spring-context.xml");
        assertEquals("Phil", context.getBean("bean2"));
        
        context.close();
    }

}
