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
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import junit.framework.TestCase;

public class ConfigurableXmlLoaderIntegrationTest extends TestCase {

	private ServletContext servletContext;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		servletContext = createMock(ServletContext.class);
		System.clearProperty(LocationConstants.BOOTSTRAP_LOCATIONS_PROPERTY_PARAM);
		System.clearProperty(LocationConstants.BOOTSTRAP_LOCATIONS_RESOURCE_PARAM);
		System.clearProperty(LocationConstants.BOOTSTRAP_MODULES_RESOURCE_PARAM);
	}
	
	public void testConfigurableWebXmlBasedContextLoader() throws Exception {
		expect(servletContext.getInitParameter(LocationConstants.BOOTSTRAP_LOCATIONS_RESOURCE_PARAM)).andReturn("org/impalaframework/web/module/bootstrap_locations.properties");
		expect(servletContext.getInitParameter(WebConstants.ROOT_MODULE_NAME_PARAM)).andReturn(
			"project1,project2");
		expect(servletContext.getInitParameter(LocationConstants.BOOTSTRAP_MODULES_RESOURCE_PARAM)).andReturn("org/impalaframework/web/module/plugin_locations.properties");
		expect(servletContext.getInitParameter(LocationConstants.BOOTSTRAP_MODULES_RESOURCE_PARAM)).andReturn("org/impalaframework/web/module/plugin_locations.properties");
		servletContext.setAttribute(eq(WebConstants.IMPALA_FACTORY_ATTRIBUTE), isA(ModuleManagementFacade.class));		servletContext.setAttribute(eq(WebConstants.MODULE_DEFINITION_SOURCE_ATTRIBUTE), isA(SingleStringModuleDefinitionSource.class));
		
		replay(servletContext);

		ConfigurableWebXmlBasedContextLoader loader = new ConfigurableWebXmlBasedContextLoader();
		WebApplicationContext context = loader.createWebApplicationContext(servletContext, null);
		
		assertNotNull(context);
		assertTrue(context instanceof GenericWebApplicationContext);
		verify(servletContext);
	}
}
