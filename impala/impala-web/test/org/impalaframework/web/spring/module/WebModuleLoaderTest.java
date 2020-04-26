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

package org.impalaframework.web.spring.module;

import java.io.IOException;

import org.impalaframework.module.definition.SimpleModuleDefinition;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;

import junit.framework.TestCase;

public class WebModuleLoaderTest extends TestCase {

    public final void testGetSpringConfigResources() throws IOException {
        WebModuleLoader moduleLoader = new WebModuleLoader();
        Resource[] springConfigResources = moduleLoader.getSpringConfigResources("id", new SimpleModuleDefinition(null, "plugin1", new String[]{"parentTestContext.xml"}), ClassUtils.getDefaultClassLoader());
    
        assertEquals(1, springConfigResources.length);
        Resource resource = springConfigResources[0];
        assertTrue(resource instanceof ClassPathResource);
        assertEquals("class path resource [parentTestContext.xml]", resource.getDescription());
    }

}
