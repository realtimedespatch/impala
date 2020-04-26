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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import javax.servlet.ServletContext;

import org.impalaframework.constants.LocationConstants;
import org.impalaframework.facade.ModuleManagementFacade;
import org.impalaframework.module.source.SingleStringModuleDefinitionSource;
import org.impalaframework.web.WebConstants;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import junit.framework.TestCase;

public class WebXmlBasedContextLoaderIntegrationTest extends TestCase {

	private ServletContext servletContext;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		servletContext = createMock(ServletContext.class);
		System.clearProperty(LocationConstants.BOOTSTRAP_LOCATIONS_PROPERTY_PARAM);
		System.clearProperty(LocationConstants.BOOTSTRAP_LOCATIONS_RESOURCE_PARAM);
		System.clearProperty(LocationConstants.BOOTSTRAP_MODULES_RESOURCE_PARAM);
	}
	
	public void testWebXmlBasedContextLoader() throws Exception {
		expect(servletContext.getInitParameter(ContextLoader.CONFIG_LOCATION_PARAM)).andReturn("parentTestContext.xml");
		expect(servletContext.getInitParameter(WebConstants.ROOT_MODULE_NAME_PARAM)).andReturn(
			"project1,project2");
		expect(servletContext.getInitParameter(WebConstants.MODULE_NAMES_PARAM)).andReturn("sample-module1");
		servletContext.setAttribute(eq(WebConstants.IMPALA_FACTORY_ATTRIBUTE), isA(ModuleManagementFacade.class));
		servletContext.setAttribute(eq(WebConstants.MODULE_DEFINITION_SOURCE_ATTRIBUTE), isA(SingleStringModuleDefinitionSource.class));
		
		replay(servletContext);

		WebXmlBasedContextLoader loader = new WebXmlBasedContextLoader(){
			@Override
			public String[] getBootstrapContextLocations(ServletContext servletContext) {
				String[] locations = new String[] { 
						"META-INF/impala-bootstrap.xml",
						"META-INF/impala-web-bootstrap.xml"};
				return locations;
			}
		};
		WebApplicationContext context = loader.createWebApplicationContext(servletContext, null);
		
		assertNotNull(context);
		assertTrue(context instanceof GenericWebApplicationContext);
		verify(servletContext);
	}	
	
	
	public void testWebXmlBasedContextLoaderWithListener() throws Exception {
		expect(servletContext.getInitParameter(ContextLoader.CONFIG_LOCATION_PARAM)).andReturn("parentTestContext.xml");
		expect(servletContext.getInitParameter(WebConstants.ROOT_MODULE_NAME_PARAM)).andReturn(
			"project1,project2");
		expect(servletContext.getInitParameter(WebConstants.MODULE_NAMES_PARAM)).andReturn("sample-module1");
		servletContext.setAttribute(eq(WebConstants.IMPALA_FACTORY_ATTRIBUTE), isA(ModuleManagementFacade.class));
		servletContext.setAttribute(eq(WebConstants.MODULE_DEFINITION_SOURCE_ATTRIBUTE), isA(SingleStringModuleDefinitionSource.class));
		
		replay(servletContext);

		WebXmlBasedContextLoader loader = new WebXmlBasedContextLoader(){
			@Override
			public String[] getBootstrapContextLocations(ServletContext servletContext) {
				String[] locations = new String[] { 
						"META-INF/impala-bootstrap.xml",
						"META-INF/impala-web-bootstrap.xml",
						"META-INF/impala-web-listener-bootstrap.xml"};
				return locations;
			}
		};
		WebApplicationContext context = loader.createWebApplicationContext(servletContext, null);
		
		assertNotNull(context);
		assertTrue(context instanceof GenericWebApplicationContext);
		verify(servletContext);
	}
}
