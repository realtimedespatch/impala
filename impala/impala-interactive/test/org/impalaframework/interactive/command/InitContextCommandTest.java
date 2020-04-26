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

import org.impalaframework.command.framework.GlobalCommandState;
import org.impalaframework.exception.NoServiceException;
import org.impalaframework.facade.Impala;

import junit.framework.TestCase;

public class InitContextCommandTest extends TestCase {

    private InitContextCommand command;

    public void setUp() {
        GlobalCommandState.getInstance().reset();
        Impala.clear();
        command = new InitContextCommand();
    }

    public final void testExecuteNoModuleDefinitionSource() {
        command.execute(null);
        try {
            Impala.getRootRuntimeModule();
            fail();
        }
        catch (NoServiceException e) {
            assertEquals("No root application has been loaded", e.getMessage());
        }
    }

    public final void testExecuteWithModuleDefinitionSource() {
        GlobalCommandState.getInstance().addValue(CommandStateConstants.MODULE_DEFINITION_SOURCE, new Test1());
        command.execute(null);
        assertNotNull(Impala.getRootRuntimeModule());

    }
}
