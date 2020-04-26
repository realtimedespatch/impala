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

package org.impalaframework.web.servlet.invoker;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.easymock.classextension.EasyMock;
import org.impalaframework.module.spi.FrameworkLockHolder;

import junit.framework.TestCase;

public class ReadWriteLockInvokerTest extends TestCase {
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void testInvoke() throws Exception {
        HttpServlet servlet = new HttpServlet() {
            private static final long serialVersionUID = 1L;

            public void service(HttpServletRequest req, HttpServletResponse res)
                    throws ServletException, IOException {
                
            }
        };
        ReadWriteLockingInvoker invoker = new ReadWriteLockingInvoker(servlet, EasyMock.createMock(FrameworkLockHolder.class));
        invoker.invoke(EasyMock.createMock(HttpServletRequest.class), EasyMock.createMock(HttpServletResponse.class), null);
    }
}
