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

import org.impalaframework.util.CollectionStringUtils;

import junit.framework.TestCase;

public class IdentityWebAttributeQualifierTest extends TestCase {

    private IdentityWebAttributeQualifier qualifier;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        qualifier = new IdentityWebAttributeQualifier();
    }
    
    public void testFilterAttributeNames() {
        final Enumeration<String> enumeration = Collections.enumeration(CollectionStringUtils.parseStringList("one,two"));
        assertSame(enumeration, qualifier.filterAttributeNames(enumeration, null, null));
    }

    public void testGetQualifiedAttributeName() {
        final String name = "myname";
        assertSame(name, qualifier.getQualifiedAttributeName(name, null, null));
    }

    public void testGetQualifierPrefix() {
        assertEquals("", qualifier.getQualifierPrefix(null, null));
    }

}
