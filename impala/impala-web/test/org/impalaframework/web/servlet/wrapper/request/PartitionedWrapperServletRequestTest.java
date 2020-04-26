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

package org.impalaframework.web.servlet.wrapper.request;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.impalaframework.facade.ModuleManagementFacade;
import org.impalaframework.module.spi.ApplicationManager;
import org.impalaframework.module.spi.ModuleStateHolder;
import org.impalaframework.module.spi.TestApplicationManager;
import org.impalaframework.spring.module.SpringRuntimeModule;
import org.impalaframework.util.ReflectionUtils;
import org.impalaframework.web.WebConstants;
import org.impalaframework.web.servlet.wrapper.CacheableHttpSession;
import org.impalaframework.web.servlet.wrapper.RequestModuleMapping;
import org.impalaframework.web.servlet.wrapper.session.PartitionedHttpSessionWrapper;
import org.impalaframework.web.servlet.wrapper.session.StateProtectingHttpSession;
import org.springframework.util.ClassUtils;

import junit.framework.TestCase;

public class PartitionedWrapperServletRequestTest extends TestCase {

    private HttpServletRequest request;
    private HttpSession session;
    private ServletContext servletContext;
    private MappedHttpServletRequest wrapperRequest;
    private ModuleManagementFacade moduleManagementFacade;
    private ModuleStateHolder moduleStateHolder;
    private SpringRuntimeModule springRuntimeModule;
    private ApplicationManager applicationManager;
    private String applicationId;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        request = createMock(HttpServletRequest.class);
        servletContext = createMock(ServletContext.class);
        session = createMock(HttpSession.class);
        moduleManagementFacade = createMock(ModuleManagementFacade.class);
        moduleStateHolder = createMock(ModuleStateHolder.class);
        springRuntimeModule = createMock(SpringRuntimeModule.class);
        applicationId = "applicationId";
        final PartitionedHttpSessionWrapper httpSessionWrapper = new PartitionedHttpSessionWrapper();
        httpSessionWrapper.setServletContext(servletContext);
        httpSessionWrapper.setEnableModuleSessionProtection(true);
        wrapperRequest = new MappedHttpServletRequest(servletContext, request, httpSessionWrapper, new RequestModuleMapping("/mymodule", "mymodule", null, null), applicationId );
        
        applicationManager = TestApplicationManager.newApplicationManager(null, moduleStateHolder, null);
    }
    
    public void testGetSession() {
    
        expect(springRuntimeModule.getClassLoader()).andReturn(ClassUtils.getDefaultClassLoader());
        expect(servletContext.getAttribute(WebConstants.IMPALA_FACTORY_ATTRIBUTE)).andReturn(moduleManagementFacade);
        expect(moduleManagementFacade.getApplicationManager()).andReturn(applicationManager);
        expect(moduleStateHolder.getModule("mymodule")).andReturn(springRuntimeModule);
        request.setAttribute(eq(MappedHttpServletRequest.class.getName()+".WRAPPED_SESSION"), isA(CacheableHttpSession.class));
        
        replayMocks();

        HttpSession wrappedSession = wrapperRequest.wrapSession(session);
        assertTrue(wrappedSession instanceof StateProtectingHttpSession);
        StateProtectingHttpSession moduleAwareSession = (StateProtectingHttpSession) wrappedSession;
        
        final ClassLoader classLoader = ReflectionUtils.getFieldValue(moduleAwareSession, "moduleClassLoader", ClassLoader.class);
        assertNotNull(classLoader);
        
        final HttpSession storedSession = ReflectionUtils.getFieldValue(moduleAwareSession, "realSession", HttpSession.class);
        assertSame(session, storedSession);

        verifyMocks();
    }
    
    public void testGetSessionNoFactoryAvailable() {
        
        expect(servletContext.getAttribute(WebConstants.IMPALA_FACTORY_ATTRIBUTE)).andReturn(null);
        
        replayMocks();

        HttpSession wrappedSession = wrapperRequest.wrapSession(session);
        assertSame(session, wrappedSession);

        verifyMocks();
    }

    private void verifyMocks() {
        verify(request);
        verify(servletContext);
        verify(moduleManagementFacade);
        verify(moduleStateHolder);
        verify(springRuntimeModule);
    }

    private void replayMocks() {
        replay(request);
        replay(servletContext);
        replay(moduleManagementFacade);
        replay(moduleStateHolder);
        replay(springRuntimeModule);
    }

}
