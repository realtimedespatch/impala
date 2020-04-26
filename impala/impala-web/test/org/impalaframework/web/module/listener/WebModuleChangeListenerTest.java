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

package org.impalaframework.web.module.listener;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

import java.util.ArrayList;

import javax.servlet.ServletContext;

import org.impalaframework.facade.ModuleManagementFacade;
import org.impalaframework.module.monitor.ModuleChangeEvent;
import org.impalaframework.module.monitor.ModuleChangeInfo;
import org.impalaframework.module.operation.ModuleOperation;
import org.impalaframework.module.operation.ModuleOperationConstants;
import org.impalaframework.module.operation.ModuleOperationInput;
import org.impalaframework.module.operation.ModuleOperationRegistry;
import org.impalaframework.module.operation.ModuleOperationResult;
import org.impalaframework.module.spi.Application;
import org.impalaframework.module.spi.ApplicationManager;
import org.impalaframework.module.spi.TestApplicationManager;
import org.impalaframework.web.WebConstants;

import junit.framework.TestCase;

public class WebModuleChangeListenerTest extends TestCase {

    public final void testEmpty() throws Exception {

        ServletContext servletContext = createMock(ServletContext.class);

        final WebModuleChangeListener listener = new WebModuleChangeListener();
        listener.setServletContext(servletContext);

        ArrayList<ModuleChangeInfo> info = new ArrayList<ModuleChangeInfo>();
        ModuleChangeEvent event = new ModuleChangeEvent(info);

        replay(servletContext);

        listener.moduleContentsModified(event);

        verify(servletContext);

        info.add(new ModuleChangeInfo("p1"));
        info.add(new ModuleChangeInfo("p2"));
        info.add(new ModuleChangeInfo("p2"));

        event = new ModuleChangeEvent(info);

    }   
    
    public final void testEventsNonEmpty() throws Exception {

        ArrayList<ModuleChangeInfo> info = new ArrayList<ModuleChangeInfo>();

        info.add(new ModuleChangeInfo("p1"));

        ModuleChangeEvent event = new ModuleChangeEvent(info);
        ServletContext servletContext = createMock(ServletContext.class);
        final WebModuleChangeListener listener = new WebModuleChangeListener();
        listener.setServletContext(servletContext);
        ModuleManagementFacade facade = createMock(ModuleManagementFacade.class);
        ModuleOperationRegistry moduleOperationRegistry = createMock(ModuleOperationRegistry.class);
        ModuleOperation moduleOperation = createMock(ModuleOperation.class);

        expect(servletContext.getAttribute(WebConstants.IMPALA_FACTORY_ATTRIBUTE)).andReturn(facade);
        ApplicationManager applicationManager = TestApplicationManager.newApplicationManager();
        Application application = applicationManager.getCurrentApplication();
        
        expect(facade.getApplicationManager()).andReturn(applicationManager);
        expect(facade.getModuleOperationRegistry()).andReturn(moduleOperationRegistry);
        expect(moduleOperationRegistry.getOperation(ModuleOperationConstants.ReloadNamedModuleOperation)).andReturn(moduleOperation);
        expect(moduleOperation.execute(eq(application), isA(ModuleOperationInput.class))).andReturn(ModuleOperationResult.EMPTY);
        
        replay(servletContext);
        replay(facade);
        replay(moduleOperationRegistry);
        replay(moduleOperation);
        
        listener.moduleContentsModified(event);

        verify(servletContext);
        verify(facade);
        verify(moduleOperationRegistry);
        verify(moduleOperation);

    }
}
