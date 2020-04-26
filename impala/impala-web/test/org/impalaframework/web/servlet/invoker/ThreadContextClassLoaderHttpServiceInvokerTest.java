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

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.easymock.EasyMock;
import org.impalaframework.classloader.ModuleClassLoader;

import junit.framework.TestCase;

public class ThreadContextClassLoaderHttpServiceInvokerTest extends TestCase {
    
    private ClassLoader contextClassLoader;
    private ClassLoader originalClassLoader;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        originalClassLoader = Thread.currentThread().getContextClassLoader();
    }
    
    public void testInvokeWithTrue() throws Exception {
        ClassLoader classLoader = new ModuleClassLoader(new File[]{new File("./")});
        
        HttpServlet servlet = new HttpServlet() {
            private static final long serialVersionUID = 1L;

            public void service(HttpServletRequest req, HttpServletResponse res)
                    throws ServletException, IOException {
                contextClassLoader = Thread.currentThread().getContextClassLoader();
            }
        };
        ThreadContextClassLoaderHttpServiceInvoker invoker = new ThreadContextClassLoaderHttpServiceInvoker(servlet, true, classLoader);
        
        invoker.invoke(EasyMock.createMock(HttpServletRequest.class), EasyMock.createMock(HttpServletResponse.class), null);
        
        //check that the context class loader was correctly set in 
        assertSame(contextClassLoader, classLoader);
        
        //assert that the current thread now has the original class loader
        assertSame(originalClassLoader, Thread.currentThread().getContextClassLoader());
    }
}
