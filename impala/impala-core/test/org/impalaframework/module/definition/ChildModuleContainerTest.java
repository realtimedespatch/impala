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

package org.impalaframework.module.definition;

import org.impalaframework.module.ModuleContainer;
import org.impalaframework.module.ModuleDefinition;

import junit.framework.TestCase;

/**
 * @author Phil Zoio
 */
public class ChildModuleContainerTest extends TestCase {

    public void testChildContainer() {
        final ModuleDefinition[] strings = new ModuleDefinition[] { new SimpleModuleDefinition("p1"), new SimpleModuleDefinition("p2") };
        ModuleContainer container = new ModuleContainerImpl(strings);

        assertTrue(container.hasChildModuleDefinition("p1"));
        assertTrue(container.hasChildModuleDefinition("p2"));
        assertFalse(container.hasChildModuleDefinition("p3"));

        assertEquals(2, container.getChildModuleNames().size());
        
        //fail("To implement add() and remove() tests");
    }

}
