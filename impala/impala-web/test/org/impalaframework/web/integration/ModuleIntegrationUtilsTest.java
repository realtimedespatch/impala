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

package org.impalaframework.web.integration;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.impalaframework.facade.ModuleManagementFacade;
import org.impalaframework.web.WebConstants;
import org.impalaframework.web.servlet.wrapper.RequestModuleMapping;
import org.impalaframework.web.servlet.wrapper.request.IdentityHttpRequestWrapper;

import junit.framework.TestCase;

public class ModuleIntegrationUtilsTest extends TestCase {
    
    private ServletContext servletContext;
    private HttpServletRequest request;
    private ModuleManagementFacade facade;
    private RequestModuleMapping mapping;
    private String applicationId;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        servletContext = createMock(ServletContext.class);
        request = createMock(HttpServletRequest.class);
        facade = createMock(ModuleManagementFacade.class);
        
        mapping = new RequestModuleMapping("/mymodule", "mymodule", null, null);
        applicationId = "";
    }
    
    public void testGetRequestWrapper() throws Exception {
        
        expect(servletContext.getAttribute(WebConstants.IMPALA_FACTORY_ATTRIBUTE)).andReturn(facade);
        expect(facade.getBean(WebConstants.REQUEST_WRAPPER_FACTORY_BEAN_NAME)).andReturn(new IdentityHttpRequestWrapper());
        
        replayMocks(servletContext, request, facade);
        
        assertSame(request, ModuleIntegrationUtils.getWrappedRequest(request, servletContext, mapping, applicationId));
        
        verifyMocks(servletContext, request, facade);
    }   

    public void testGetRequestWrapperFacadeNull() throws Exception {
        
        expect(servletContext.getAttribute(WebConstants.IMPALA_FACTORY_ATTRIBUTE)).andReturn(null);
        
        replayMocks(servletContext, request, facade);
        
        assertSame(request, ModuleIntegrationUtils.getWrappedRequest(request, servletContext, mapping, applicationId));
        
        verifyMocks(servletContext, request, facade);
    }
    
    public void testGetRequestWrapperFactoryNull() throws Exception {

        expect(servletContext.getAttribute(WebConstants.IMPALA_FACTORY_ATTRIBUTE)).andReturn(facade);
        expect(facade.getBean(WebConstants.REQUEST_WRAPPER_FACTORY_BEAN_NAME)).andReturn(null);
        
        replayMocks(servletContext, request, facade);
        
        assertSame(request, ModuleIntegrationUtils.getWrappedRequest(request, servletContext, mapping, applicationId));
        
        verifyMocks(servletContext, request, facade);
    }

    private void verifyMocks(final ServletContext servletContext,
            final HttpServletRequest request,
            final ModuleManagementFacade facade) {
        verify(request);
        verify(servletContext);
        verify(facade);
    }

    private void replayMocks(final ServletContext servletContext,
            final HttpServletRequest request,
            final ModuleManagementFacade facade) {
        replay(request);
        replay(servletContext);
        replay(facade);
    }
    
}
