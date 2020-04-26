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

package org.impalaframework.interactive.command;

import org.impalaframework.command.framework.CommandState;
import org.impalaframework.command.framework.GlobalCommandState;

import junit.framework.TestCase;

public class LoadTestClassContextCommandTest extends TestCase {

    private LoadTestClassContextCommand command;
    private CommandState commandState;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        command = new LoadTestClassContextCommand();
        commandState = new CommandState();
        GlobalCommandState.getInstance().reset();
    }
    
    public final void testNoTestClassSet() {
        assertFalse(command.execute(commandState));
    }
    
    public final void testWithTestClassSet() {
        GlobalCommandState.getInstance().addValue(CommandStateConstants.TEST_CLASS_NAME, Test1.class.getName());
        assertFalse(command.execute(commandState));
        assertNotNull(GlobalCommandState.getInstance().getValue(CommandStateConstants.MODULE_DEFINITION_SOURCE));
        assertNotNull(GlobalCommandState.getInstance().getValue(CommandStateConstants.TEST_CLASS));
    }

}
