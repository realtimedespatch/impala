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

package org.impalaframework.web.spring.bean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import javax.servlet.ServletContext;

import junit.framework.TestCase;

public class SystemPropertyServletContextParamFactoryBeanTest extends TestCase {

    private ServletContext servletContext;
    private SystemPropertyServletContextParamFactoryBean bean;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.clearProperty("myParam");
        servletContext = createMock(ServletContext.class);
        
        bean = new SystemPropertyServletContextParamFactoryBean();
        bean.setServletContext(servletContext);
        
        bean.setDefaultValue("defaultValue");
        bean.setParameterName("myParam");
        
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        System.clearProperty("myParam");
    }
    
    public void testProperties() throws Exception {
        assertEquals(String.class, bean.getObjectType());
        assertEquals(true, bean.isSingleton());
    }
    
    public void testGetDefaultValue() throws Exception {
        expect(servletContext.getInitParameter("myParam")).andReturn(null);
        replay(servletContext);
        
        bean.afterPropertiesSet();
        String value = (String) bean.getObject();
        assertEquals("defaultValue", value);
        
        verify(servletContext);
    }
    
    public void testGetInitParam() throws Exception {
        expect(servletContext.getInitParameter("myParam")).andReturn("scValue");
        replay(servletContext);
        
        bean.afterPropertiesSet();
        String value = (String) bean.getObject();
        assertEquals("scValue", value);
        
        verify(servletContext);
    }
    
    public void testGetSystemProperty() throws Exception {
        System.setProperty("myParam", "someValue");
        replay(servletContext);
        
        bean.afterPropertiesSet();
        String value = (String) bean.getObject();
        assertEquals("someValue", value);
        
        verify(servletContext);
    }

    
    public void testGetSystemPropertyPrefix() throws Exception {
        System.setProperty("myParam", "someValue");
        bean.setPrefix("myprefix-");
        replay(servletContext);
        
        bean.afterPropertiesSet();
        String value = (String) bean.getObject();
        assertEquals("myprefix-someValue", value);
        
        verify(servletContext);
    }
    
    

}
