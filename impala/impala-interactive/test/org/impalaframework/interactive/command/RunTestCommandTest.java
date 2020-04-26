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
import org.impalaframework.exception.NoServiceException;
import org.impalaframework.facade.Impala;
import org.impalaframework.resolver.ModuleLocationResolver;
import org.impalaframework.resolver.StandaloneModuleLocationResolver;
import org.impalaframework.util.PathUtils;

import junit.framework.TestCase;

public class RunTestCommandTest extends TestCase {

    private RunTestCommand runTestCommand;

    private CommandState commandState;

    private RerunTestCommand rerunTestCommand;

    @Override
    protected void setUp() throws Exception {
        Impala.clear();
        GlobalCommandState.getInstance().reset();
        super.setUp();
        ModuleLocationResolver moduleLocationResolver = new StandaloneModuleLocationResolver();
        runTestCommand = new RunTestCommand(moduleLocationResolver);
        rerunTestCommand = new RerunTestCommand(moduleLocationResolver);
        commandState = new CommandState();
        GlobalCommandState.getInstance().reset();
        Impala.clear();
    }

    public final void testNotSet() {
        assertFalse(runTestCommand.execute(commandState));
        assertFalse(rerunTestCommand.execute(commandState));
    }

    public final void testNoModuleDefinition() {
        GlobalCommandState.getInstance().addValue(CommandStateConstants.TEST_CLASS, Test1.class);
        try {
            runTestCommand.execute(commandState);
            fail();
        }
        catch (NoServiceException e) {
        }
    }
    
    //this does not work when run as part of a suite
    public void notestWithModuleDefinition() {
        String currentDirectoryName = PathUtils.getCurrentDirectoryName();
        
        if ("impala-interactive".equals(currentDirectoryName)) {
        
            //no test method set, so this returns false
            assertFalse(rerunTestCommand.execute(commandState));
    
            Impala.init(new Test1());
            GlobalCommandState.getInstance().addValue(CommandStateConstants.TEST_CLASS, Test1.class);
            assertTrue(runTestCommand.execute(commandState));
    
            //returns true, because test name set
            assertTrue(rerunTestCommand.execute(commandState));
        }
    }

}
