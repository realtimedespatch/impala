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

import org.impalaframework.command.framework.CommandPropertyValue;
import org.impalaframework.command.framework.CommandState;
import org.impalaframework.command.framework.GlobalCommandState;
import org.impalaframework.resolver.StandaloneModuleLocationResolver;

import junit.framework.TestCase;

public class ChangeDirectoryCommandTest extends TestCase {

    private ChangeDirectoryCommand command;
    private CommandState commandState;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        commandState = new CommandState();
        command = new ChangeDirectoryCommand(new StandaloneModuleLocationResolver());
        GlobalCommandState.getInstance().reset();
    }

    public final void testChangeDirectoryCommandNull() {
        try {
            command.execute(commandState);
        }
        catch (IllegalArgumentException e) {
            assertEquals("[Assertion failed] - this argument is required; it must not null", e.getMessage());
        }
        assertNull(GlobalCommandState.getInstance().getValue(CommandStateConstants.DIRECTORY_NAME));
    }

    public final void testChangeDirectoryCommand() {
        commandState.addProperty(ChangeDirectoryCommand.DIRECTORY_NAME, new CommandPropertyValue("impala-core"));

        command.execute(commandState);
        assertEquals("impala-core", GlobalCommandState.getInstance().getValue(CommandStateConstants.DIRECTORY_NAME));
    }
    
    public final void testChangeDirectoryCommandDuffValue() {
        commandState.addProperty(ChangeDirectoryCommand.DIRECTORY_NAME, new CommandPropertyValue("duff-value"));

        command.execute(commandState);
        assertNull(GlobalCommandState.getInstance().getValue(CommandStateConstants.DIRECTORY_NAME));
    }

}
