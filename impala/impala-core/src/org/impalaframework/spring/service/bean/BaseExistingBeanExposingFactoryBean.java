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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.impalaframework.spring.bean.factory.BeanFactoryUtils;
import org.impalaframework.spring.module.graph.GraphDelegatingApplicationContext;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Base class for exposing an existing bean
 * @author Phil Zoio
 */
public abstract class BaseExistingBeanExposingFactoryBean implements InitializingBean, FactoryBean<Object>, ApplicationContextAware {
    
    private static final Log logger = LogFactory.getLog(BaseExistingBeanExposingFactoryBean.class);
    
    private ApplicationContext applicationContext;

    /**
     * Whether to include the current bean definition when searching for the bean factory
     */
    protected abstract boolean getIncludeCurrentBeanFactory();

    /**
     * Returns the bean name to search for
     */
    protected abstract String getBeanNameToSearchFor();
    
    /**
     * Finds the first parent {@link BeanFactory} which contains a bean of the given name
     */
    BeanFactory findBeanFactory() {

        final ApplicationContext currentContext = maybeFindApplicationContext();
        
        if (currentContext != null) {
            
            if (currentContext instanceof ConfigurableApplicationContext) {
            
                @SuppressWarnings("resource")
				ConfigurableApplicationContext configurableContext = (ConfigurableApplicationContext) currentContext;
                return configurableContext.getBeanFactory();
            
            } else {
                logger.warn("Cannot return bean factory as application context is not an instance of " + ConfigurableApplicationContext.class.getName() + ": " + currentContext);
            }
        }
        
        throw new BeanDefinitionValidationException("No parent bean factory of application context [" + applicationContext.getDisplayName() + "] contains bean [" + getBeanNameToSearchFor() + "]");
    }

    private ApplicationContext maybeFindApplicationContext() {
        
        ApplicationContext currentContext = this.applicationContext;
        
        final String beanName = getBeanNameToSearchFor();
        if (getIncludeCurrentBeanFactory()) {
            if (currentContext.containsBeanDefinition(beanName)) {
                return currentContext;
            }
        }
        
        ApplicationContext parentContext = applicationContext.getParent();
        
        if (parentContext instanceof GraphDelegatingApplicationContext) {
            return ((GraphDelegatingApplicationContext) parentContext).getContainingApplicationContext(beanName);
        } else if (parentContext != null) {
            return BeanFactoryUtils.maybeFindApplicationContext(parentContext, beanName);
        } 
        return null;
    }

    public Class<?> getObjectType() {
        return null;
    }
    
    protected ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}
