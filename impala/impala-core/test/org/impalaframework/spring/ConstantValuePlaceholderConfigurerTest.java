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

import org.impalaframework.constants.LocationConstants;
import org.impalaframework.exception.ConfigurationException;
import org.impalaframework.spring.ConstantValuePlaceholderConfigurer.ConstantStringValueResolver;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.ClassUtils;

public class ConstantValuePlaceholderConfigurerTest extends TestCase {

    public static String EMPTY;
    
    public void testConfigurer() throws Exception {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:org/impalaframework/spring/constantvalue.xml");
        assertEquals("module.class.dir", applicationContext.getBean("string1"));        
        assertEquals("stringval", applicationContext.getBean("string2"));
        
        applicationContext.close();
    }
    
    public void testResolver() {
        ConstantValuePlaceholderConfigurer c = new ConstantValuePlaceholderConfigurer();
        c.setBeanClassLoader(ClassUtils.getDefaultClassLoader());
        ConstantStringValueResolver resolver = c.new ConstantStringValueResolver();
        
        assertEquals("stringval", resolver.resolveStringValue("stringval"));
        assertEquals("module.class.dir", resolver.resolveStringValue("constant:[" + LocationConstants.class.getName() + ".MODULE_CLASS_DIR_PROPERTY]"));

        //check trim handling
        assertEquals("module.class.dir", resolver.resolveStringValue(" constant:[ " + LocationConstants.class.getName() + ".MODULE_CLASS_DIR_PROPERTY ] "));

        try {
            resolver.resolveStringValue("constant:[duffclass]");
            fail();
        }
        catch (ConfigurationException e) {
            assertEquals("Invalid expression 'duffclass' in expression 'constant:[duffclass]'. Must evaluate to constant (e.g. 'constant:[org.impalframework.constants.LocationConstant.MODULE_CLASS_DIR_PROPERTY]'", e.getMessage());
        }
        
        //value is null
        try {
            resolver.resolveStringValue("constant:[org.impalaframework.spring.ConstantValuePlaceholderConfigurerTest.EMPTY]");
            fail();
        }
        catch (ConfigurationException e) {
            assertEquals("Field 'EMPTY' in class org.impalaframework.spring.ConstantValuePlaceholderConfigurerTest in expression 'constant:[org.impalaframework.spring.ConstantValuePlaceholderConfigurerTest.EMPTY]' cannot evaluate to null", e.getMessage());
        }
        
        //field cannot be resolved
        try {
            resolver.resolveStringValue("constant:[org.impalaframework.spring.ConstantValuePlaceholderConfigurerTest.NOTPRESENT]");
            fail();
        }
        catch (ConfigurationException e) {
            assertEquals("Field 'NOTPRESENT' in class org.impalaframework.spring.ConstantValuePlaceholderConfigurerTest in expression 'constant:[org.impalaframework.spring.ConstantValuePlaceholderConfigurerTest.NOTPRESENT]' could not be evaluated. Could not evaluate constant in bean 'null'", e.getMessage());
        }
        
        //class cannot be resolved
        try {
            resolver.resolveStringValue("constant:[org.impalaframework.spring.DuffClass.NOTPRESENT]");
            fail();
        }
        catch (ConfigurationException e) {
            assertEquals("Class 'org.impalaframework.spring.DuffClass' in expression 'constant:[org.impalaframework.spring.DuffClass.NOTPRESENT]' could not be found. Could not evaluate constant in bean 'null'", e.getMessage());
        }
    }

}
