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

import javax.servlet.ServletContext;

import org.easymock.EasyMock;
import org.impalaframework.module.definition.SimpleModuleDefinition;
import org.impalaframework.util.ReflectionUtils;
import org.impalaframework.web.servlet.qualifier.DefaultWebAttributeQualifier;
import org.springframework.util.ClassUtils;

import junit.framework.TestCase;

public class BaseServletContextWrapperTest extends TestCase {
    
    private PartitionedServletContextWrapper wrapper;
    private ServletContext servletContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        wrapper = new PartitionedServletContextWrapper();
        wrapper.setWebAttributeQualifier(new DefaultWebAttributeQualifier());
        servletContext = EasyMock.createMock(ServletContext.class);
    }

    public void testIdentityWraper() {
        assertSame(servletContext, wrapper.wrapServletContext(servletContext, "", null, null));
    }

    public void testPartitionedContext() {
        wrapper.setEnablePartitionedServletContext(true);
        final ServletContext wrappedContext = wrapper.wrapServletContext(servletContext, "id", new SimpleModuleDefinition("mymodule"), ClassUtils.getDefaultClassLoader());
        assertTrue(wrappedContext instanceof PartitionedServletContext);
        PartitionedServletContext wrapperContext = (PartitionedServletContext) wrappedContext;
        assertEquals("id", ReflectionUtils.invokeMethod(wrapperContext, "getApplicationId"));
    }

}
