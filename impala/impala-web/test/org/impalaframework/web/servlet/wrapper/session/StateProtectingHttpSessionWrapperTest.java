package org.impalaframework.web.servlet.wrapper.session;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
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
import org.impalaframework.web.WebConstants;
import org.impalaframework.web.servlet.qualifier.WebAttributeQualifier;
import org.springframework.util.ClassUtils;

import junit.framework.TestCase;

public class StateProtectingHttpSessionWrapperTest extends TestCase {

    private HttpServletRequest request;
    private HttpSession session;
    private ServletContext servletContext;
    private PartitionedHttpSessionWrapper wrapper;
    private ModuleManagementFacade moduleManagementFacade;
    private ModuleStateHolder moduleStateHolder;
    private SpringRuntimeModule springRuntimeModule;
    private ApplicationManager applicationManager;
    private WebAttributeQualifier webAttributeQualifier;
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
        webAttributeQualifier = createMock(WebAttributeQualifier.class);
        applicationId = "applicationId";
        
        wrapper = new PartitionedHttpSessionWrapper();
        wrapper.setServletContext(servletContext);
        wrapper.setWebAttributeQualifier(webAttributeQualifier);
        
        applicationManager = TestApplicationManager.newApplicationManager(null, moduleStateHolder, null);
    }
    
    public void testGetSessionWithProtection() {

        wrapper.setEnableModuleSessionProtection(true);
        
        expect(springRuntimeModule.getClassLoader()).andReturn(ClassUtils.getDefaultClassLoader());
        expect(servletContext.getAttribute(WebConstants.IMPALA_FACTORY_ATTRIBUTE)).andReturn(moduleManagementFacade);
        expect(moduleManagementFacade.getApplicationManager()).andReturn(applicationManager);
        expect(moduleStateHolder.getModule("mymodule")).andReturn(springRuntimeModule);
        
        replayMocks();

        HttpSession wrappedSession = wrapper.wrapSession(session, "mymodule", applicationId);
        assertTrue(wrappedSession instanceof StateProtectingHttpSession);
        StateProtectingHttpSession moduleAwareSession = (StateProtectingHttpSession) wrappedSession;
        assertNotNull(moduleAwareSession.getModuleClassLoader());
        assertSame(session, moduleAwareSession.getRealSession());

        verifyMocks();
    }
    
    public void testGetSessionNoFactoryAvailable() {

        wrapper.setEnableModuleSessionProtection(true);
        expect(servletContext.getAttribute(WebConstants.IMPALA_FACTORY_ATTRIBUTE)).andReturn(null);
        
        replayMocks();

        HttpSession wrappedSession = wrapper.wrapSession(session, "mymodule", applicationId);
        assertSame(session, wrappedSession);

        verifyMocks();
    }
    
    public void testGetSessionNoProtection() {

        replayMocks();

        HttpSession wrappedSession = wrapper.wrapSession(session, "mymodule", applicationId);
        assertSame(session, wrappedSession);

        verifyMocks();
    }
    
    public void testGetModuleAwareSession() {

        wrapper.setEnablePartitionedServletContext(true);
        
        replayMocks();

        HttpSession wrappedSession = wrapper.wrapSession(session, "mymodule", applicationId);
        assertNotSame(session, wrappedSession);
        assertTrue(wrappedSession instanceof PartitionedHttpSession);

        verifyMocks();
    }

    private void replayMocks() {
        replay(request);
        replay(servletContext);
        replay(moduleManagementFacade);
        replay(moduleStateHolder);
        replay(springRuntimeModule);
        replay(webAttributeQualifier);
    }

    private void verifyMocks() {
        verify(request);
        verify(servletContext);
        verify(moduleManagementFacade);
        verify(moduleStateHolder);
        verify(springRuntimeModule);
        verify(webAttributeQualifier);
    }

}
