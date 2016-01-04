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

package org.impalaframework.spring.service.bean;

import junit.framework.TestCase;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Phil Zoio
 */
public class ParentFactoryBeanTest extends TestCase {

    public void testParentFactoryBean() throws Exception {
        ClassPathXmlApplicationContext parentContext = new ClassPathXmlApplicationContext("childcontainer/parent-factory.xml");
        ClassPathXmlApplicationContext childContext = new ClassPathXmlApplicationContext(new String[]{"childcontainer/child-factory.xml"}, parentContext);
        
        Object prototypeParent = parentContext.getBean("prototypeParent");
        Object prototypeChild = childContext.getBean("prototypeParent");
        
        assertTrue(childContext.getBean("&prototypeParent") instanceof ParentFactoryBean);
        
        assertSame(prototypeParent.getClass().getName(), prototypeChild.getClass().getName());
        assertFalse(prototypeParent == prototypeChild);

        Object singletonParent = parentContext.getBean("singletonParent");
        Object singletonChild = childContext.getBean("singletonParent");
        
        assertTrue(childContext.getBean("&singletonParent") instanceof ParentFactoryBean);
        
        assertSame(singletonParent.getClass().getName(), singletonChild.getClass().getName());
        assertTrue(singletonParent == singletonChild);
        
        Object renamedChild = childContext.getBean("renamedParent");
        assertSame(renamedChild.getClass().getName(), singletonChild.getClass().getName());
        assertTrue(singletonParent == renamedChild);
        
        childContext.close();
    }
    
    public void testNoParent() throws Exception {

        try {
            new ClassPathXmlApplicationContext(new String[]{"childcontainer/child-factory.xml"});
            fail();
        }
        catch (BeanCreationException e) {
            Throwable rootCause = e.getRootCause();
            assertTrue(rootCause instanceof BeanDefinitionValidationException);
            BeanDefinitionValidationException cause = (BeanDefinitionValidationException) rootCause;
            assertTrue(cause.getMessage().startsWith("No parent bean factory of application context"));
        }
    }
    
}
