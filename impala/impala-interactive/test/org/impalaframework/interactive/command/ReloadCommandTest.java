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

import org.impalaframework.command.framework.CommandDefinition;
import org.impalaframework.command.framework.CommandState;
import org.impalaframework.command.framework.GlobalCommandState;
import org.impalaframework.exception.NoServiceException;

import junit.framework.TestCase;

public class ReloadCommandTest extends TestCase {

    private TestReloadCommand command;

    @Override
    protected void setUp() throws Exception {
        GlobalCommandState.getInstance().reset();
        command = new TestReloadCommand();
    }
    
    public final void testExecute() {
        //no module definition loaded, so this won't work
        assertFalse(command.execute(null));
        
        //now load up the module definition properly
        GlobalCommandState.getInstance().addValue(CommandStateConstants.MODULE_DEFINITION_SOURCE, new Test1());

        CommandState commandState = new CommandState();
        //this will cause NoServiceException, because Impala.init() has not been called
        try {
            command.execute(commandState);
        }
        catch (NoServiceException e) {
            e.printStackTrace();
            assertEquals("The application has not been initialised. Has Impala.init(ModuleDefinitionSource) been called?", e.getMessage());
        }
        
        assertTrue(new InitContextCommand().execute(null));
        
        assertTrue(command.execute(commandState));
        assertTrue(command.isRootReloaded());
        assertFalse(command.isModuleReloaded());
    }
    
    public final void testModuleReload() {
        //now load up the module definition properly
        GlobalCommandState.getInstance().addValue(CommandStateConstants.MODULE_DEFINITION_SOURCE, new Test1());

        CommandState commandState = new CommandState();
        assertTrue(new InitContextCommand().execute(null));

        command.extractText(new String[]{Test1.plugin1}, commandState);
        command.execute(commandState);      
        assertFalse(command.isRootReloaded());
        assertTrue(command.isModuleReloaded());
    }

    public final void testGetCommandDefinition() {
        CommandDefinition commandDefinition = command.getCommandDefinition();
        assertTrue(commandDefinition.getCommandInfos().isEmpty());
    }

}

class TestReloadCommand extends ReloadCommand {

    private boolean moduleReloaded;
    private boolean rootReloaded;
    
    @Override
    void reloadModule(String moduleToReload, CommandState commandState) {
        moduleReloaded = true;
    }

    @Override
    void reloadRootModule() {
        rootReloaded = true;
    }

    public boolean isModuleReloaded() {
        return moduleReloaded;
    }

    public boolean isRootReloaded() {
        return rootReloaded;
    }
    
    public void reset() {
        moduleReloaded = false;
        rootReloaded = false;
    }
    
}
