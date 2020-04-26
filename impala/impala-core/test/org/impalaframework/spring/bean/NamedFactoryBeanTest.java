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

package org.impalaframework.spring.bean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.springframework.beans.factory.BeanFactory;

import junit.framework.TestCase;

public class NamedFactoryBeanTest extends TestCase {

    private BeanFactory beanFactory;
    private NamedFactoryBean factoryBean;

    @Override
    protected void setUp() throws Exception {
        beanFactory = createMock(BeanFactory.class);
        factoryBean = new NamedFactoryBean();
        factoryBean.setBeanName("mybean");
        factoryBean.setObjectType(Integer.class);
        factoryBean.setBeanFactory(beanFactory);
    }
    
    public void testGetObject() throws Exception {
        
        expect(beanFactory.getBean("mybean", Integer.class)).andReturn(1);
        replay(beanFactory);
        assertEquals(new Integer(1), factoryBean.getObject());
        verify(beanFactory);
    }
    
    public void testSuffix() throws Exception {
        
        factoryBean.setSuffix("Suffix");
        expect(beanFactory.getBean("mybeanSuffix", Integer.class)).andReturn(1);
        replay(beanFactory);
        assertEquals(new Integer(1), factoryBean.getObject());
        verify(beanFactory);
    }

}
