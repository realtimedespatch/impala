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
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;

import org.easymock.EasyMock;
import org.impalaframework.exception.ConfigurationException;
import org.impalaframework.facade.ModuleManagementFacade;
import org.impalaframework.module.ModuleDefinitionSource;
import org.impalaframework.module.RootModuleDefinition;
import org.impalaframework.web.WebConstants;

import junit.framework.TestCase;

public class ImpalaContextLoaderTest extends TestCase {

	private WebXmlBasedContextLoader contextLoader;
	private ServletContext servletContext;
	private ModuleManagementFacade facade;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		contextLoader = new WebXmlBasedContextLoader();
		servletContext = createMock(ServletContext.class);
		facade = createMock(ModuleManagementFacade.class);
	}

	public void testBootstrapLocations() throws Exception {
		String[] locations = contextLoader.getBootstrapContextLocations(EasyMock.createMock(ServletContext.class));
		assertTrue(locations.length > 0);
	}

	public void testGetModuleDefinition() {
		expect(servletContext.getInitParameter(WebXmlBasedContextLoader.CONFIG_LOCATION_PARAM)).andReturn(
				"context1.xml, context2.xml");
		expect(servletContext.getInitParameter(WebConstants.ROOT_MODULE_NAME_PARAM)).andReturn(
			"project1,project2");
		expect(servletContext.getInitParameter(WebConstants.MODULE_NAMES_PARAM)).andReturn("p1, p2, p3");

		WebXmlBasedContextLoader contextLoader = new WebXmlBasedContextLoader();

		replay(servletContext);

		ModuleDefinitionSource builder = contextLoader.getModuleDefinitionSource(servletContext, facade);
		RootModuleDefinition rootModuleDefinition = builder.getModuleDefinition();

		List<String> list = new ArrayList<String>();
		list.add("context1.xml");
		list.add("context2.xml");

		assertEquals(list, rootModuleDefinition.getConfigLocations());

		assertTrue(Arrays.equals(new String[] { "p1", "p2", "p3" }, rootModuleDefinition.getChildModuleNames().toArray(new String[3])));

		verify(servletContext);
	}
	
	public void testNoRootProjects() {
		expect(servletContext.getInitParameter(WebXmlBasedContextLoader.CONFIG_LOCATION_PARAM)).andReturn(
				"context1.xml, context2.xml");
		expect(servletContext.getInitParameter(WebConstants.ROOT_MODULE_NAME_PARAM)).andReturn(
			null);

		WebXmlBasedContextLoader contextLoader = new WebXmlBasedContextLoader();

		replay(servletContext);

		try {
			contextLoader.getModuleDefinitionSource(servletContext, facade);
		}
		catch (ConfigurationException e) {
			assertEquals("Cannot create root module as the init-parameter 'rootModuleName' has not been specified", e.getMessage());
		}
		
		verify(servletContext);
	}

	public void testGetChildModuleDefinitionString() {

		expect(servletContext.getInitParameter(WebConstants.MODULE_NAMES_PARAM)).andReturn("plugin1, plugin2");

		replay(servletContext);
		assertEquals("plugin1, plugin2", contextLoader.getModuleDefinitionString(servletContext));
		verify(servletContext);
	}

}
