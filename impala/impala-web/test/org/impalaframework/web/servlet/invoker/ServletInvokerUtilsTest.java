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

import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

public class ServletInvokerUtilsTest extends TestCase {

    private HttpServiceInvoker invoker;
    private HttpServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private Filter filter;
    private FilterChain filterChain;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        invoker = createMock(HttpServiceInvoker.class);
        servlet = createMock(HttpServlet.class);
        request = createMock(HttpServletRequest.class);
        response = createMock(HttpServletResponse.class);
        filter = createMock(Filter.class);
        filterChain = createMock(FilterChain.class);
    }
    
    public void testWithInvoker() throws Exception {
        invoker.invoke(request, response, null);
        
        replayMocks();
        
        ServletInvokerUtils.invoke(invoker, request, response, null);
        
        verifyMocks();
    }
    
    public void testWithServlet() throws Exception {
        servlet.service(request, response);
        
        replayMocks();
        
        ServletInvokerUtils.invoke(servlet, request, response, null);
        
        verifyMocks();
    }
    
    public void testWithFilter() throws Exception {
        filter.doFilter(request, response, filterChain);
        
        replayMocks();
        
        ServletInvokerUtils.invoke(filter, request, response, filterChain);
        
        verifyMocks();
    }

    public void testWithNull() throws Exception {
        
        replayMocks();
        
        ServletInvokerUtils.invoke(null, request, response, filterChain);
        
        verifyMocks();
    }
    
    public void testWithAnotherType() throws Exception {
        
        replayMocks();
        
        ServletInvokerUtils.invoke("Just a string - not a valid arg", request, response, filterChain);
        
        verifyMocks();
    }
    
    private void replayMocks() {
        replay(invoker);
        replay(servlet);
        replay(filter);
        replay(request);
        replay(response);
        replay(filterChain);
    }

    private void verifyMocks() {
        verify(invoker);
        verify(servlet);
        verify(filter);
        verify(request);
        verify(response);
        verify(filterChain);
    }


}
