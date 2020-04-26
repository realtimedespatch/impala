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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.impalaframework.command.framework.Command;
import org.impalaframework.command.framework.CommandLineInputCapturer;
import org.impalaframework.command.framework.CommandState;

import junit.framework.TestCase;

public class ManualInteractiveTestCommandTest extends TestCase {

    public void testInteractive() throws Exception {

        InteractiveTestCommand command = getCommand();
        Map<String, Command> commandMap = getCommandMap();
        Set<String> commandKeys = commandMap.keySet();
        
        for (String commandKey : commandKeys) {
            command.addCommand(commandKey, commandMap.get(commandKey));
        }

        // now need to capture
        CommandState commandState = new CommandState();

        CommandLineInputCapturer inputCapturer = getInputCapturer();
        commandState.setInputCapturer(inputCapturer);

        System.out.println("--------------------");

        commandState.capture(command);
        command.execute(commandState);
    }
    
    protected Map<String, Command> getCommandMap() {
        Map<String, Command> commands = new HashMap<String, Command>();
        commands.put("exit", new ExitCommand());
        commands.put("reload-all", new ReloadCommand());
        return commands;
    }

    protected InteractiveTestCommand getCommand() {
        InteractiveTestCommand command = new InteractiveTestCommand();
        return command;
    }

    protected CommandLineInputCapturer getInputCapturer() {
        return new CommandLineInputCapturer();
    }
}
