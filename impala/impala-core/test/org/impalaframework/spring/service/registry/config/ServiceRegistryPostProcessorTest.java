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

package org.impalaframework.spring.service.registry.config;

import org.impalaframework.service.ServiceRegistry;
import org.impalaframework.service.registry.ServiceRegistryAware;
import org.impalaframework.service.registry.internal.DelegatingServiceRegistry;
import org.impalaframework.spring.service.ProxyFactoryCreator;
import org.impalaframework.spring.service.proxy.DefaultProxyFactoryCreator;
import org.impalaframework.spring.service.proxy.ProxyFactoryCreatorAware;

import junit.framework.TestCase;

public class ServiceRegistryPostProcessorTest extends TestCase {

    public final void testPostProcessBeforeInitialization() {
        ServiceRegistry registry = new DelegatingServiceRegistry();
        ProxyFactoryCreator proxyFactoryCreator = new DefaultProxyFactoryCreator();
        ServiceRegistryPostProcessor postProcessor = new ServiceRegistryPostProcessor(registry, proxyFactoryCreator);
        TestRegistryAware testAware = new TestRegistryAware();
        TestProxyFactoryCreatorAware proxyFactoryCreatorAware = new TestProxyFactoryCreatorAware();
        postProcessor.postProcessBeforeInitialization(testAware, null);
        postProcessor.postProcessBeforeInitialization(proxyFactoryCreatorAware, null);
        assertSame(registry, testAware.getServiceRegistry());
        assertSame(proxyFactoryCreator, proxyFactoryCreatorAware.getProxyFactoryCreator());
        
        assertSame(testAware, postProcessor.postProcessAfterInitialization(testAware, null));
    }
}

class TestRegistryAware implements ServiceRegistryAware {

    private ServiceRegistry serviceRegistry;

    ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

}

class TestProxyFactoryCreatorAware implements ProxyFactoryCreatorAware {
    
    private ProxyFactoryCreator proxyFactoryCreator;

    public void setProxyFactoryCreator(ProxyFactoryCreator proxyFactoryCreator) {
        this.proxyFactoryCreator = proxyFactoryCreator;
    }

    public ProxyFactoryCreator getProxyFactoryCreator() {
        return proxyFactoryCreator;
    }
    
}
