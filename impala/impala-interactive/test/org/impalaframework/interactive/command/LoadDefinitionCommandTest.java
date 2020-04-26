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
import org.impalaframework.facade.Impala;

import junit.framework.TestCase;

public class LoadDefinitionCommandTest extends TestCase {

    private LoadDefinitionFromClassCommand command;

    private CommandState commandState;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Impala.clear();
        GlobalCommandState.getInstance().reset();
        command = new LoadDefinitionFromClassCommand();
        commandState = new CommandState();
    }

    public void testClassNotSet() {
        assertFalse(command.execute(commandState));
    }
    
    public void testClassIsSet() {
        GlobalCommandState.getInstance().addValue(CommandStateConstants.TEST_CLASS, Test1.class);
        assertTrue(command.execute(commandState));
        assertNotNull(GlobalCommandState.getInstance().getValue(CommandStateConstants.TEST_CLASS_NAME));
    }

}
