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

package org.impalaframework.module.monitor;

import java.util.ArrayList;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @author Phil Zoio
 */
public class BaseModuleChangeListenerTest extends TestCase {

    public final void testGetModifiedModules() {
        BaseModuleChangeListener listener = new BaseModuleChangeListener();
        ArrayList<ModuleChangeInfo> info = new ArrayList<ModuleChangeInfo>();
        ModuleChangeEvent event = new ModuleChangeEvent(info);
        assertTrue(listener.getModifiedModules(event).isEmpty());
        
        info.add(new ModuleChangeInfo("p1"));
        info.add(new ModuleChangeInfo("p2"));
        info.add(new ModuleChangeInfo("p2"));
        
        event = new ModuleChangeEvent(info);
        Set<String> modifiedModules = listener.getModifiedModules(event);
        assertEquals(2, modifiedModules.size());
        assertTrue(modifiedModules.contains("p1"));
    }

}
