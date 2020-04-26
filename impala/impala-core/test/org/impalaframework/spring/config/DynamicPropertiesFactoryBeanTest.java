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
package org.impalaframework.spring.config;

import java.util.Properties;

import org.springframework.core.io.FileSystemResource;

import junit.framework.TestCase;

public class DynamicPropertiesFactoryBeanTest extends TestCase {

    public void testProperties() throws Exception {
        DynamicPropertiesFactoryBean factoryBean = new DynamicPropertiesFactoryBean();
        factoryBean.setLocation(new FileSystemResource("../impala-core/files/reload/reloadable.properties"));
        
        Properties props = (Properties) factoryBean.getObject();
        System.out.println(props);
        
        Long lastModified = factoryBean.getLastModified();
        assertTrue(lastModified > 0L);
        
        factoryBean.getObject();
        System.out.println(props);
        assertEquals(lastModified, factoryBean.getLastModified());
    }   
    
    public void notestManuallyProperties() throws Exception {
        DynamicPropertiesFactoryBean factoryBean = new DynamicPropertiesFactoryBean();
        factoryBean.setLocation(new FileSystemResource("../impala-core/files/reload/reloadable.properties"));
        
        while (true) {
            Properties props = (Properties) factoryBean.getObject();
            System.out.println(props);
            Long lastModified = factoryBean.getLastModified();
            System.out.println(lastModified);
            Thread.sleep(1000);
        }
    }
}
