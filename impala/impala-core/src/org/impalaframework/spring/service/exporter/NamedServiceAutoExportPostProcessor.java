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

package org.impalaframework.spring.service.exporter;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.impalaframework.module.ModuleDefinition;
import org.impalaframework.module.definition.ModuleDefinitionAware;
import org.impalaframework.service.NamedServiceEndpoint;
import org.impalaframework.service.ServiceBeanReference;
import org.impalaframework.service.ServiceRegistry;
import org.impalaframework.service.ServiceRegistryEntry;
import org.impalaframework.service.registry.ServiceRegistryAware;
import org.impalaframework.spring.SelfAwareApplicationListenerAdapter;
import org.impalaframework.spring.service.SpringServiceBeanUtils;
import org.impalaframework.spring.service.StaticSpringServiceBeanReference;
import org.impalaframework.util.ObjectUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * {@link BeanPostProcessor} which attempts to register the created bean
 * with the {@link ServiceRegistry}, but only if the current bean's parent context
 * has a same-named bean which implements {@link NamedServiceEndpoint}.
 * 
 * @author Phil Zoio
 */
public class NamedServiceAutoExportPostProcessor extends SelfAwareApplicationListenerAdapter implements 
        ModuleDefinitionAware, 
        ServiceRegistryAware,
        BeanFactoryAware,
        BeanClassLoaderAware {

    private static final Log logger = LogFactory.getLog(NamedServiceAutoExportPostProcessor.class);

    private BeanFactory beanFactory;
    
    private ServiceRegistry serviceRegistry;

    private ModuleDefinition moduleDefinition;
    
    private ClassLoader beanClassLoader;
    
    private Map<String, ServiceRegistryEntry> referenceMap = new IdentityHashMap<String, ServiceRegistryEntry>();
    
    private Set<String> beansEncountered = new HashSet<String>();
    
    /* *************** Application Event ************** */
    
    public void inApplicationEvent(ApplicationEvent event) {
        
        final String name = moduleDefinitionName();
        
        if (event instanceof ContextRefreshedEvent) {
            
            logger.info("################################ " + this.getClass().getName() + " - context refreshed for " + name + " ####################");

            processBeanFactory(new BeanFactoryCallback() {
                public void doWithBean(String beanName, Object bean) {
                    maybeExportBean(bean, beanName);
                }
            });
           
        } else if (event instanceof ContextClosedEvent) {
            logger.info("################################ " + this.getClass().getName() + " - context closed for " + name + " ####################");
        
            processBeanFactory(new BeanFactoryCallback() {
                public void doWithBean(String beanName, Object bean) {
                    maybeUnexportBean(bean, beanName);
                }
            });
        }
    }

    /* *************** BeanPostProcessor methods ************** */
    
    public Object maybeExportBean(Object bean, String beanName) throws BeansException {  
        
        String moduleName = moduleName();
        
        //add check so that we don't try to add bean and factory bean
        if (!beansEncountered.contains(beanName) && !referenceMap.containsKey(beanName)) {
            
            if (SpringServiceBeanUtils.isSingleton(beanFactory, beanName)) {
        
                //only if there is a contribution end point corresponding with bean name do we register the service
                NamedServiceEndpoint endPoint = SpringModuleServiceUtils.findServiceEndpoint(beanFactory, beanName);
                if (hasRegisterableEndpoint(beanName, endPoint)) {          
                    logger.info("Contributing bean " + beanName + " from module " + moduleName);
                    
                    final ServiceBeanReference service = new StaticSpringServiceBeanReference(bean);
                    final ServiceRegistryEntry serviceReference = serviceRegistry.addService(beanName, moduleName, service, beanClassLoader);
                    referenceMap.put(beanName, serviceReference);
                }   
            } else {
                
                if (logger.isDebugEnabled()) {
                    logger.debug("Not auto-exporting " + beanName + " as this is not a singleton");
                }
            }
            
        } else {
            
            if (logger.isDebugEnabled()) {
                logger.debug("Already registered bean " + beanName);
            }
        }
        
        beansEncountered.add(beanName);
        return bean;
    }
    
    /* *************** DestructionAwareBeanPostProcessor methods ************** */

    public void maybeUnexportBean(Object bean, String beanName) throws BeansException {

        final String moduleName = moduleName();
        final ServiceRegistryEntry serviceRegistryReference = referenceMap.get(beanName);
        
        //if we have reference for this bean
        if (serviceRegistryReference != null) {

            logger.info("Removing bean " + beanName + " contributed from module " + moduleName);
            
            if (serviceRegistryReference != null) {
                serviceRegistry.remove(serviceRegistryReference);
            } 
        }
    }
    
    /* *************** package level methods ************** */

    void processBeanFactory(final BeanFactoryCallback callback) {
        
        ConfigurableListableBeanFactory listableBeanFactory = ObjectUtils.cast(beanFactory, ConfigurableListableBeanFactory.class);
           
        // gets list of all bean definitions in this context
        final String[] beanNames = listableBeanFactory.getBeanDefinitionNames();
        for (String beanName : beanNames) {

            final BeanDefinition beanDefinition = listableBeanFactory.getBeanDefinition(beanName);
            if (!beanDefinition.isAbstract()) {

                final Object bean = beanFactory.getBean(beanName);
                callback.doWithBean(beanName, bean);
            }
        }
    }

    private String moduleDefinitionName() {
        final String name = moduleDefinition != null ? moduleDefinition.getName() : "[Module definition not wired in]";
        return name;
    }
    
    /* *************** private methods ************** */

    private boolean hasRegisterableEndpoint(String beanName,
            NamedServiceEndpoint endPoint) {
        boolean b = endPoint != null && beanName.equals(endPoint.getExportName());
        return b;
    }
    
    private String moduleName() {
        String moduleName = null;
        if (moduleDefinition != null) {
            moduleName = moduleDefinition.getName();
        }
        return moduleName;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public void setModuleDefinition(ModuleDefinition moduleDefinition) {
        this.moduleDefinition = moduleDefinition;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setBeanClassLoader(ClassLoader beanClassLoader) {
        this.beanClassLoader = beanClassLoader;
    }
    
    interface BeanFactoryCallback {
        
        void doWithBean(String name, Object bean);
    }

}
