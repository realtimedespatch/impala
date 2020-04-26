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

import java.util.concurrent.Executors;

import org.springframework.core.io.ClassPathResource;

import junit.framework.TestCase;

public class ManualDynamicPropertiesPropertySourceTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        System.clearProperty("property.folder");
    }
    
    public void testDynamicProperties() throws Exception {
        DynamicPropertiesFactoryBean factoryBean = new DynamicPropertiesFactoryBean();
        factoryBean.setLocation(new ClassPathResource("reload/reloadable.properties"));
        
        DynamicPropertySource source = new DynamicPropertySource();
        doTest(factoryBean, source);
    }
    
    public void testDynamicPropertiesWithExecutorService() throws Exception {
        DynamicPropertiesFactoryBean factoryBean = new DynamicPropertiesFactoryBean();
        factoryBean.setLocation(new ClassPathResource("reload/reloadable.properties"));
        
        DynamicPropertySource source = new DynamicPropertySource();
        source.setExecutorService(Executors.newScheduledThreadPool(2));
        doTest(factoryBean, source);
    }

    private void doTest(DynamicPropertiesFactoryBean factoryBean,
            DynamicPropertySource source) throws Exception,
            InterruptedException {
        source.setFactoryBean(factoryBean);
        source.setReloadInitialDelay(1);
        source.setReloadInterval(3);
        source.afterPropertiesSet();
        
        while (true) {
            System.out.println(source.getValue("property1"));
            Thread.sleep(1000);
        }
    }

    //this test will dynamically reflect the value of the classpath resource reload/reloadable.properties
    public void testExternalProperties() throws Exception, InterruptedException {
        ExternalDynamicPropertySource source = new ExternalDynamicPropertySource();
        source.setFileName("reload/reloadable.properties");
        source.setReloadInitialDelay(1);
        source.setReloadInterval(3);
        source.afterPropertiesSet();
        
        while (true) {
            System.out.println(source.getValue("property1"));
            Thread.sleep(1000);
        }
    }
    
    //this test will dynamically reflect the value of the file system resource files/reload/reloadable.properties.
    //in other words, it overrides the value in the testExternalProperties() method
    public void testExternalPropertiesWithFileOverride() throws Exception {
        System.setProperty("property.folder", "files");
        testExternalProperties();
    }
    
}
