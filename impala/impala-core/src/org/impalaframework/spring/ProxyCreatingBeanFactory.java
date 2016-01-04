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

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * Loads properties on startup - overrides getBeanDefinition to pick up
 * NoSuchBeanDefinitionException - disables preInstantiateSingletons
 */
public class ProxyCreatingBeanFactory extends DefaultListableBeanFactory {

    final Log log = LogFactory.getLog(ProxyCreatingBeanFactory.class);

    private Properties properties;

    public ProxyCreatingBeanFactory() {
        super();
        properties = loadProperties();
    }

    public ProxyCreatingBeanFactory(BeanFactory parentBeanFactory) {
        super(parentBeanFactory);
        properties = loadProperties();
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
        try {
            return super.getBeanDefinition(beanName);
        }
        catch (NoSuchBeanDefinitionException e) {
            String interfaceName = properties.getProperty(beanName);

            log.debug("bean name '" + beanName + "' interface name " + interfaceName);

            BeanDefinitionRegistry bdr = (BeanDefinitionRegistry) this;

            RootBeanDefinition interceptorDefinition = new RootBeanDefinition(DebuggingInterceptor.class);
            bdr.registerBeanDefinition(beanName + "_interceptor", interceptorDefinition);

            RootBeanDefinition proxyDefinition = new RootBeanDefinition(ProxyFactoryBean.class);
            proxyDefinition.getPropertyValues().addPropertyValue("interceptorNames", beanName + "_interceptor");
            proxyDefinition.getPropertyValues().addPropertyValue("proxyInterfaces", interfaceName);

            bdr.registerBeanDefinition(beanName, proxyDefinition);

            return proxyDefinition;
        }
    }

    private Properties loadProperties() {
        Properties props = null;
        try {
            props = PropertiesLoaderUtils.loadProperties(new ClassPathResource("beaninterfaces.properties"));
            log.debug("Properties: " +  props);
        }
        catch (IOException e1) {
            throw new BeanCreationException("Unable to load properties file beaninterfaces.properties", e1);
        }
        return props;
    }

    @Override
    public void preInstantiateSingletons() throws BeansException {
        if (logger.isInfoEnabled()) {
            logger.info("Pre-instantiating singletons in factory [" + this + "]");
        }

        String[] beanNames = this.getBeanDefinitionNames();

        for (int i = 0; i < beanNames.length; i++) {
            String beanName = beanNames[i];
            if (!containsSingleton(beanName) && containsBeanDefinition(beanName)) {

                BeanDefinition bd = getMergedBeanDefinition(beanName);
                if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) {
                    
                    if (bd instanceof RootBeanDefinition) {
                        RootBeanDefinition rootBeanDefinition = (RootBeanDefinition) bd;
                        
                        Class<?> beanClass = resolveBeanClass(rootBeanDefinition, beanName);
                        if (beanClass != null && FactoryBean.class.isAssignableFrom(beanClass)) {
                            getBean(FACTORY_BEAN_PREFIX + beanName);
                        }
                        else {
                            getBean(beanName);
                        }
                    } else {
                        log.warn("Unable to instantiate bean definition " + bd + " as this is not an instance of "
                                + RootBeanDefinition.class.getName());
                    }
                }
            }
        }
    }
}
