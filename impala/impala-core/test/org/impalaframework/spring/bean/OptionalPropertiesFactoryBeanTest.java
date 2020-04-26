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

import java.io.IOException;
import java.util.Properties;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import junit.framework.TestCase;

public class OptionalPropertiesFactoryBeanTest extends TestCase {

    private OptionalPropertiesFactoryBean factoryBean;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        factoryBean = new OptionalPropertiesFactoryBean();
    }
    
    public final void testNonexistOnly() throws IOException {
        Resource[] resources = new Resource[1];
        resources[0] = new ClassPathResource("aresourcewhichdoesnotexist");
        factoryBean.setLocations(resources);
        factoryBean.afterPropertiesSet();
        
        Properties properties = (Properties) factoryBean.getObject();
        assertEquals(0, properties.size());
    }
    
    public final void testSingleWithExisting() throws IOException {
        Resource resource = new ClassPathResource("beanset.properties");
        factoryBean.setLocation(resource);
        factoryBean.afterPropertiesSet();
        
        Properties properties = (Properties) factoryBean.getObject();
        assertEquals(3, properties.size());
        assertEquals("applicationContext-set1.xml", properties.get("set1"));
    }
    
    public final void testWithExisting() throws IOException {
        Resource[] resources = new Resource[2];
        resources[0] = new ClassPathResource("aresourcewhichdoesnotexist");
        resources[1] = new ClassPathResource("beanset.properties");
        factoryBean.setLocations(resources);
        factoryBean.afterPropertiesSet();
        
        Properties properties = (Properties) factoryBean.getObject();
        assertEquals(3, properties.size());
        assertEquals("applicationContext-set1.xml", properties.get("set1"));
    }

}
