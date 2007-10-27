/*
 * Copyright 2007 the original author or authors.
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

package net.java.impala.spring.web;

import javax.servlet.ServletContext;

import net.java.impala.location.ClassLocationResolver;
import net.java.impala.location.PropertyClassLocationResolver;
import net.java.impala.spring.SpringContextHolder;
import net.java.impala.spring.monitor.ScheduledPluginMonitor;
import net.java.impala.spring.plugin.ApplicationPluginLoader;
import net.java.impala.spring.plugin.PluginLoaderRegistry;
import net.java.impala.spring.plugin.PluginTypes;
import net.java.impala.spring.plugin.SimpleSpringContextSpec;
import net.java.impala.spring.plugin.SpringContextSpec;
import net.java.impala.spring.plugin.WebPluginModificationListener;
import net.java.impala.spring.plugin.WebPluginTypes;
import net.java.impala.spring.plugin.XmlBeansetApplicationPluginLoader;
import net.java.impala.spring.util.ApplicationContextLoader;
import net.java.impala.spring.util.RegistryBasedApplicationContextLoader;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

public class RegistryBasedImpalaContextLoader extends ContextLoader {

	public static final String PLUGIN_NAMES_PARAM = "pluginNames";

	public static final String CONTEXT_HOLDER_PARAM = WebApplicationContext.class.getName() + ".CONTEXT_HOLDER";

	public static final String WEBAPP_LOCATION_PARAM = "webappConfigLocation";

	private boolean autoreload = true;	
	
	@Override
	protected WebApplicationContext createWebApplicationContext(ServletContext servletContext, ApplicationContext parent)
			throws BeansException {		
		
		ClassLocationResolver classLocationResolver = newClassLocationResolver();

		PluginLoaderRegistry registry = newRegistry(servletContext, classLocationResolver);

		ApplicationContextLoader loader = new RegistryBasedApplicationContextLoader(registry);
		SpringContextHolder holder = new SpringContextHolder(loader);
		
		if (autoreload ) {
			ScheduledPluginMonitor monitor = new ScheduledPluginMonitor();
			monitor.addModificationListener(new WebPluginModificationListener(servletContext));
			loader.setPluginMonitor(monitor);
		}
		
		//load the parent context, which is web-independent
		SpringContextSpec pluginSpec = getPluginSpec(servletContext);
		holder.loadParentContext(pluginSpec);

		// add context holder to servlet context
		servletContext.setAttribute(CONTEXT_HOLDER_PARAM, holder);
		WebApplicationContext parentContext = (WebApplicationContext) holder.getContext();
		
		return parentContext;
	}
	
	protected PluginLoaderRegistry newRegistry(ServletContext servletContext, ClassLocationResolver classLocationResolver) {
		PluginLoaderRegistry registry = new PluginLoaderRegistry();
		registry.setPluginLoader(PluginTypes.ROOT, new WebParentPluginLoader(classLocationResolver, servletContext));
		registry.setPluginLoader(PluginTypes.APPLICATION, new ApplicationPluginLoader(classLocationResolver));
		registry.setPluginLoader(PluginTypes.APPLICATION_WITH_BEANSETS, new XmlBeansetApplicationPluginLoader(classLocationResolver));
		registry.setPluginLoader(WebPluginTypes.SERVLET, new WebPluginLoader(classLocationResolver, servletContext));
		return registry;
	}

	protected SpringContextSpec getPluginSpec(ServletContext servletContext) {

		// subclasses can override to get PluginSpec more intelligently
		String[] locations = null;
		String configLocationString = servletContext.getInitParameter(CONFIG_LOCATION_PARAM);
		if (configLocationString != null) {
			locations = (StringUtils.tokenizeToStringArray(configLocationString,
					ConfigurableWebApplicationContext.CONFIG_LOCATION_DELIMITERS));
		}

		String[] pluginNames = null;
		String pluginNameString = servletContext.getInitParameter(PLUGIN_NAMES_PARAM);
		if (pluginNameString != null) {
			pluginNames = (StringUtils.tokenizeToStringArray(pluginNameString,
					ConfigurableWebApplicationContext.CONFIG_LOCATION_DELIMITERS));
		}
		SpringContextSpec pluginSpec = new SimpleSpringContextSpec(locations, pluginNames);
		return pluginSpec;
	}

	protected ClassLocationResolver newClassLocationResolver() {
		ClassLocationResolver classLocationResolver = new PropertyClassLocationResolver();
		return classLocationResolver;
	}

}
