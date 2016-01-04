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

package org.impalaframework.spring.service.config;

import junit.framework.TestCase;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class OptionalBeanDefinitionParserTest extends TestCase {

    public void testNamedBean() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("org/impalaframework/spring/service/config/optional-bean-context.xml");
        String target = (String)context.getBean("optionalTarget");
        assertEquals("target", target);
        
        String fallback = (String)context.getBean("optionalFallback");
        assertEquals("fallback", fallback);
        
        context.close();
    }
    
}
