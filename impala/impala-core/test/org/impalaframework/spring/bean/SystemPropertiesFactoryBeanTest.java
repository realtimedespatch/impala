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

import java.util.Properties;

import junit.framework.TestCase;

public class SystemPropertiesFactoryBeanTest extends TestCase {
    
    private SystemPropertiesFactoryBean factoryBean;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        factoryBean = new SystemPropertiesFactoryBean();
        factoryBean.setPropertyName("myProperty");
        System.clearProperty("myProperty");
    }
    
    public final void testDefault() throws Exception {
        factoryBean.setDefaultValue("myDefault");
        
        factoryBean.afterPropertiesSet();
        assertEquals("myDefault", factoryBean.getObject());
        
        Properties props = new Properties();
        factoryBean.setProperties(props);
        
        factoryBean.afterPropertiesSet();
        assertEquals("myDefault", factoryBean.getObject());
        
        //now set a valid property value
        props.setProperty("myProperty", "myPropertiesValue");
        factoryBean.afterPropertiesSet();
        assertEquals("myPropertiesValue", factoryBean.getObject());
        
        //now set a system property value
        System.setProperty("myProperty", "systemPropertyValue");
        factoryBean.afterPropertiesSet();
        assertEquals("systemPropertyValue", factoryBean.getObject());
    }

}
