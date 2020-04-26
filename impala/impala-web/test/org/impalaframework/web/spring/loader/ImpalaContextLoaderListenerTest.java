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

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.impalaframework.exception.ConfigurationException;
import org.impalaframework.exception.ExecutionException;
import org.impalaframework.web.WebConstants;
import org.springframework.beans.BeansException;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import junit.framework.TestCase;

public class ImpalaContextLoaderListenerTest extends TestCase {

    private ServletContext servletContext;

    private ImpalaContextLoaderListener contextLoader;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        contextLoader = new ImpalaContextLoaderListener();
        servletContext = createMock(ServletContext.class);
    }

    public final void testContextInitializedServletContextEvent() {
        expect(servletContext.getInitParameter(WebConstants.CONTEXT_LOADER_CLASS_NAME)).andReturn(
                TestContextLoader.class.getName());

        replay(servletContext);
        contextLoader.contextInitialized(new ServletContextEvent(servletContext));
        assertEquals(1, TestContextLoader.getAccessCount());
        verify(servletContext);
    }

    public final void testClassNotFound() {
        expect(servletContext.getInitParameter(WebConstants.CONTEXT_LOADER_CLASS_NAME)).andReturn("unknownclass");

        replay(servletContext);
        try {
            contextLoader.contextInitialized(new ServletContextEvent(servletContext));
            fail();
        }
        catch (ConfigurationException e) {
            assertEquals("Unable to instantiate context loader class unknownclass", e.getMessage());
        }
        verify(servletContext);
    }

    public final void testInvalidClassType() {
        expect(servletContext.getInitParameter(WebConstants.CONTEXT_LOADER_CLASS_NAME)).andReturn(
                String.class.getName());

        replay(servletContext);
        try {
            contextLoader.contextInitialized(new ServletContextEvent(servletContext));
            fail();
        }
        catch (ExecutionException e) {
            //assertEquals(
                //  "Error instantiating context loader class java.lang.String: java.lang.String cannot be cast to org.springframework.web.context.ContextLoader",
                //  e.getMessage());
        }
        verify(servletContext);
    }

    public final void testContextDestroyedWithNoLoader() {
        replay(servletContext);
        contextLoader.contextDestroyed(new ServletContextEvent(servletContext));
        verify(servletContext);
    }
    
    public final void testContextDestroyed() {
        expect(servletContext.getInitParameter(WebConstants.CONTEXT_LOADER_CLASS_NAME)).andReturn(
                TestContextLoader.class.getName());
        
        replay(servletContext);
        contextLoader.contextInitialized(new ServletContextEvent(servletContext));
        contextLoader.contextDestroyed(new ServletContextEvent(servletContext));
        assertEquals(1, TestContextLoader.getCloseCount());
        verify(servletContext);
    }

}

class TestContextLoader extends ContextLoader {

    static int accessCount;

    static int closeCount;

    TestContextLoader() {
        super();
        accessCount = 0;
        closeCount = 0;
    }

    @Override
    public WebApplicationContext initWebApplicationContext(ServletContext servletContext) throws IllegalStateException,
            BeansException {
        accessCount++;
        return new GenericWebApplicationContext();
    }

    @Override
    public void closeWebApplicationContext(ServletContext servletContext) {
        closeCount++;
    }

    public static int getAccessCount() {
        return accessCount;
    }

    public static int getCloseCount() {
        return closeCount;
    }

    public static void setAccessCount(int accessCount) {
        TestContextLoader.accessCount = accessCount;
    }

}
