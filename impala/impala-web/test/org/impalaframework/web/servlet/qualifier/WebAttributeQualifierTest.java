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

package org.impalaframework.web.servlet.qualifier;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.impalaframework.util.CollectionStringUtils;
import org.impalaframework.web.facade.AttributeServletContext;

import junit.framework.TestCase;

public class WebAttributeQualifierTest extends TestCase {

    private WebAttributeQualifier qualifier;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        qualifier = new DefaultWebAttributeQualifier();
    }
    
    public void testGetQualifiedAttributeName() throws Exception {
        assertEquals("application_id_module_mod:attribute", qualifier.getQualifiedAttributeName("attribute", "id", "mod"));
        assertEquals("application_id_module_mod:attribute", qualifier.getQualifiedAttributeName("application_id_module_mod:attribute", "id", "mod"));
        assertEquals("application__module_:attribute", qualifier.getQualifiedAttributeName("attribute", "", ""));
        assertEquals("attribute", qualifier.getQualifiedAttributeName("shared:attribute", "", ""));
    }
    
    @SuppressWarnings("unchecked")
    public void testGetAttributeNames() throws Exception {
        AttributeServletContext realContext = new AttributeServletContext();
        realContext.setAttribute("application__module_mymodule:mykey", "value1");
        realContext.setAttribute("mykey", "value2");
        realContext.setAttribute("application__module_mymodule:anotherkey", "value3");
        realContext.setAttribute("anotherkey", "value2");
        
        Enumeration<String> names = (Enumeration<String>) realContext.getAttributeNames();
		Enumeration<String> attributeNames = qualifier.filterAttributeNames(names, "", "mymodule");
        List<String> actual = Collections.list(attributeNames);
        List<String> expected = CollectionStringUtils.parseStringList("application__module_mymodule:mykey,application__module_mymodule:anotherkey");
        
        Collections.sort(actual);
        Collections.sort(expected);
        
        System.out.println(expected);
        System.out.println(actual);
        
		assertEquals(expected, actual);
    }
    
}
