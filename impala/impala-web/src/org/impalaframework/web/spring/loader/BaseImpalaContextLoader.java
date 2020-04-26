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

package org.impalaframework.web.spring.loader;

import java.util.Arrays;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.impalaframework.config.PropertiesLoader;
import org.impalaframework.constants.LocationConstants;
import org.impalaframework.exception.InvalidStateException;
import org.impalaframework.facade.ModuleManagementFacade;
import org.impalaframework.module.ModuleDefinitionSource;
import org.impalaframework.module.operation.ModuleOperation;
import org.impalaframework.module.operation.ModuleOperationConstants;
import org.impalaframework.module.operation.ModuleOperationInput;
import org.impalaframework.module.spi.Application;
import org.impalaframework.module.spi.ApplicationManager;
import org.impalaframework.spring.module.SpringModuleUtils;
import org.impalaframework.util.ObjectUtils;
import org.impalaframework.web.WebConstants;
import org.impalaframework.web.bootstrap.ServletContextLocationsRetriever;
import org.impalaframework.web.bootstrap.WebContextLocationResolver;
import org.impalaframework.web.config.ServletContextPropertiesLoader;
import org.impalaframework.web.helper.WebServletUtils;
import org.impalaframework.web.module.WebModuleUtils;
import org.impalaframework.web.module.source.ServletModuleDefinitionSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * Extension of <code>ContextLoader</code> which implements base functionality
 * for initializing Impala within a Spring web application. This class is
 * abstract. It defines the abstract method
 * <code>getModuleDefinitionSource</code>, which subclasses must implement.
 * The implementation of this method provides the strategy for loading metadata
 * which specifies the structure of the modules to be loaded as part of this web
 * application.
 * 
 * @author Phil Zoio
 */
public abstract class BaseImpalaContextLoader extends ContextLoader implements ServletModuleDefinitionSource {

    private static final Log logger = LogFactory.getLog(BaseImpalaContextLoader.class);

    /* ************************* Overridden superclass methods ******************** */

    @Override
    public WebApplicationContext initWebApplicationContext(
            ServletContext servletContext) throws IllegalStateException,
            BeansException {
        
        final WebApplicationContext superContext = super.initWebApplicationContext(servletContext);
        initImpalaApplicationContext(servletContext, superContext);
        
        return superContext;
    }
    

    /**
     * Creates the <code>WebApplicationContext</code> by bootstrapping Impala, retrieving the <code>ModuleDefinitionSource</code>
     * for loading the module metadata. It then instantiates the application context and returns it for further processing by
     * <code>ContextLoader</code>.
     */
    protected WebApplicationContext createWebApplicationContext(ServletContext servletContext, ApplicationContext parent)
            throws BeansException {
        
        final WebApplicationContext newContext = super.createWebApplicationContext(servletContext);
        	
    	//use the configure method to do configuration on the web application context
    	if (newContext instanceof ConfigurableWebApplicationContext) {
    		
			ConfigurableWebApplicationContext wac = (ConfigurableWebApplicationContext) newContext;
        	configureAndRefreshWebApplicationContext(wac, servletContext);
        	
	    	wac.setParent(parent);
	    	wac.setServletContext(servletContext);
    	}
        
        return newContext;
    }
    
    /**
     * Overrides the empty {@link ContextLoader#customizeContext(ServletContext, ConfigurableWebApplicationContext)}
     * by setting the parent {@link ApplicationContext} to use an empty location if the current location is simply the default,
     * and this does not exist. Effectively provides transparent support for "empty" parent {@link ApplicationContext}
     */
    @Override
    protected void customizeContext(ServletContext servletContext,
            ConfigurableWebApplicationContext applicationContext) {

        final String initParameter = servletContext.getInitParameter(CONFIG_LOCATION_PARAM);
        if (initParameter == null) {
            final ServletContextResource servletContextResource = new ServletContextResource(servletContext, XmlWebApplicationContext.DEFAULT_CONFIG_LOCATION);
            if (!servletContextResource.exists()) {
                try {
					applicationContext.setConfigLocation("classpath:META-INF/impala-web-empty.xml");
				} catch (UnsupportedOperationException e) {
					//no longer supported in Spring 3.2, so ignore this
				}
            }
        }
    }

    protected ConfigurableApplicationContext initImpalaApplicationContext(
            ServletContext servletContext,
            final WebApplicationContext superContext) {
        
        ModuleManagementFacade facade = createModuleManagementFacade(servletContext, superContext);

        // load the parent context, which is web-independent
        ModuleDefinitionSource moduleDefinitionSource = getModuleDefinitionSource(servletContext, facade);

        // add items to servlet context
        servletContext.setAttribute(WebConstants.MODULE_DEFINITION_SOURCE_ATTRIBUTE, moduleDefinitionSource);
        servletContext.setAttribute(WebConstants.IMPALA_FACTORY_ATTRIBUTE, facade);

        ApplicationManager applicationManager = facade.getApplicationManager();
        Application application = applicationManager.getCurrentApplication();
        
        ModuleOperationInput input = new ModuleOperationInput(moduleDefinitionSource, null, null);
        ModuleOperation operation = facade.getModuleOperationRegistry().getOperation(ModuleOperationConstants.UpdateRootModuleOperation);       
        operation.execute(application, input);

        ConfigurableApplicationContext context = SpringModuleUtils.getRootSpringContext(application.getModuleStateHolder());

        if (context == null) {
            logger.warn("Root module context is null: no modules loaded");
        }
        else if (!(context instanceof WebApplicationContext)) {
            throw new InvalidStateException("Application context " + context + " has class "
                    + context.getClass().getName() + " which is not an instance of "
                    + WebApplicationContext.class.getName());
        }
        
        return context;
    }

    /**
     * Overrides <code>ContextLoader</code> superclass. First, the modules are shut down. 
     * The superclass <code>closeWebApplicationContext</code> is then called.
     * Finally Impala is shut down, in the form of the <code>ModuleManagementFacade.close()</code>
     */
    @Override
    public void closeWebApplicationContext(ServletContext servletContext) {

        try {
            // the superclass closes the modules
            ModuleManagementFacade facade = WebServletUtils.getModuleManagementFacade(servletContext);
    
            if (facade != null) {
    
                servletContext.log("Closing modules and root application context hierarchy");
    
                facade.getApplicationManager().close();
    
                // now close the bootstrap factory
                facade.close();
            }        
        } finally {
            super.closeWebApplicationContext(servletContext);
        }
    }
    
    /**
     * Returns the Impala bootstrap context locations as determined by the implementation of {@link ServletContextLocationsRetriever}
     * and properties specified in the Impala properties file
     * @return an array of Spring config locations
     */
    public String[] getBootstrapContextLocations(ServletContext servletContext) {

        String resourceName = WebModuleUtils.getParamValue(servletContext, LocationConstants.BOOTSTRAP_LOCATIONS_RESOURCE_PARAM);
        if (resourceName == null) {
            resourceName = "impala.properties";
        }
        final PropertiesLoader propertiesLoader = new ServletContextPropertiesLoader(servletContext, resourceName);
        final WebContextLocationResolver locationResolver = new WebContextLocationResolver();
        
        final ServletContextLocationsRetriever resolver = new ServletContextLocationsRetriever(servletContext, locationResolver, propertiesLoader);
        final String[] toReturn = resolver.getContextLocations().toArray(new String[0]);
        
        if (logger.isInfoEnabled()) {
            logger.info("Impala context locations: " + Arrays.toString(toReturn));
        }
        
        return toReturn;
    }
    
    /* ************************* Internal helper methods ******************** */

    /**
     * Instantiates Impala in the form of a <code>ModuleManagementFacade</code> instance.
     */
	protected ModuleManagementFacade createModuleManagementFacade(ServletContext servletContext, WebApplicationContext parent) {
        
        String[] locations = getBootstrapContextLocations(servletContext);
        logger.info("Loading bootstrap context from locations " + Arrays.toString(locations));

        final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        @SuppressWarnings("resource")
        final GenericWebApplicationContext applicationContext = new GenericWebApplicationContext(beanFactory);
        applicationContext.setServletContext(servletContext);
        applicationContext.setParent(parent);

        XmlBeanDefinitionReader definitionReader = new XmlBeanDefinitionReader(beanFactory);
        for (int i = 0; i < locations.length; i++) {
            definitionReader.loadBeanDefinitions(new ClassPathResource(locations[i]));
        }
        applicationContext.refresh();

        return ObjectUtils.cast(applicationContext.getBean("moduleManagementFacade"), ModuleManagementFacade.class);
    }

    public abstract ModuleDefinitionSource getModuleDefinitionSource(ServletContext servletContext, ModuleManagementFacade factory);


}
