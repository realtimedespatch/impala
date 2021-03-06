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

package org.impalaframework.web.config;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import javax.servlet.ServletContext;

import junit.framework.TestCase;

public class ContextPathAwareSystemPropertySourceTest extends TestCase {

    private ServletContext servletContext;
    private ContextPathAwareSystemPropertySource source;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        servletContext = createMock(ServletContext.class);
        source = new ContextPathAwareSystemPropertySource(servletContext);
    }
    
    public void testGetMajorVersion1() {
        expect(servletContext.getMajorVersion()).andReturn(1);
        
        replay(servletContext);
    
        assertNull(source.getValue("name"));
        
        verify(servletContext);
    }
    
    public void testGetMinorVerion4() {
        expect(servletContext.getMajorVersion()).andReturn(2);
        expect(servletContext.getMinorVersion()).andReturn(4);
        
        replay(servletContext);
    
        assertNull(source.getValue("name"));
        
        verify(servletContext);
    }

    public void testGetMinorVerion5() {
        System.setProperty("path.name", "value");
        
        expect(servletContext.getMajorVersion()).andReturn(2);
        expect(servletContext.getMinorVersion()).andReturn(5);
        expect(servletContext.getContextPath()).andReturn("/path");
        
        replay(servletContext);
    
        assertEquals("value", source.getValue("name"));
        
        verify(servletContext);
    }

}
