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

package org.impalaframework.web.servlet.wrapper.context;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.net.MalformedURLException;

import javax.servlet.ServletContext;

import org.impalaframework.web.servlet.qualifier.DefaultWebAttributeQualifier;
import org.springframework.util.ClassUtils;

import junit.framework.TestCase;

public class BaseWrapperServletContextTest extends TestCase {

    private ServletContext servletContext;
    private DefaultWebAttributeQualifier webAttributeQualifier;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        webAttributeQualifier = new DefaultWebAttributeQualifier();
    }

    public void testGetResourceOnClassPath() throws MalformedURLException {
        servletContext = createMock(ServletContext.class);
        PartitionedServletContext wrapperContext = new PartitionedServletContext(servletContext, "", "mymodule", webAttributeQualifier, ClassUtils.getDefaultClassLoader());
        
        replay(servletContext);
        
        //in both cases the item is found on the classpath, so no call to underlying servletContext is made
        assertNotNull(wrapperContext.getResource("parentTestContext.xml"));
        assertNotNull(wrapperContext.getResource("/parentTestContext.xml"));
        
        verify(servletContext);
    }
    

    public void testGetResourceNotClassPath() throws MalformedURLException {
        servletContext = createMock(ServletContext.class);
        PartitionedServletContext wrapperContext = new PartitionedServletContext(servletContext, "", "mymodule", webAttributeQualifier, ClassUtils.getDefaultClassLoader());
        
        expect(servletContext.getResource("nopresent.xml")).andReturn(null);
        expect(servletContext.getResource("/nopresent.xml")).andReturn(null);
        
        replay(servletContext);
        
        //in both cases the item is found on the classpath, so no call to underlying servletContext is made
        wrapperContext.getResource("nopresent.xml");
        wrapperContext.getResource("/nopresent.xml");
        
        verify(servletContext);
    }
    
    public void testGetResourceAsStreamOnClassPath() throws MalformedURLException {
        servletContext = createMock(ServletContext.class);
        PartitionedServletContext wrapperContext = new PartitionedServletContext(servletContext, "", "mymodule", webAttributeQualifier, ClassUtils.getDefaultClassLoader());
        
        replay(servletContext);
        
        //in both cases the item is found on the classpath, so no call to underlying servletContext is made
        assertNotNull(wrapperContext.getResourceAsStream("parentTestContext.xml"));
        assertNotNull(wrapperContext.getResourceAsStream("/parentTestContext.xml"));
        
        verify(servletContext);
    }

    public void testGetResourceAsStreamNotClassPath() throws MalformedURLException {
        servletContext = createMock(ServletContext.class);
        PartitionedServletContext wrapperContext = new PartitionedServletContext(servletContext, "", "mymodule", webAttributeQualifier, ClassUtils.getDefaultClassLoader());
        
        expect(servletContext.getResource("nopresent.xml")).andReturn(null);
        expect(servletContext.getResource("/nopresent.xml")).andReturn(null);
        
        replay(servletContext);
        
        //in both cases the item is found on the classpath, so no call to underlying servletContext is made
        wrapperContext.getResourceAsStream("nopresent.xml");
        wrapperContext.getResourceAsStream("/nopresent.xml");
        
        verify(servletContext);
    }

}
