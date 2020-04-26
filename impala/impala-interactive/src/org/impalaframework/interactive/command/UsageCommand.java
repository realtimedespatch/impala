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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.impalaframework.command.framework.Command;
import org.impalaframework.command.framework.CommandDefinition;
import org.impalaframework.command.framework.CommandState;
import org.springframework.util.Assert;

public class UsageCommand implements Command {

    private Map<String, Command> commands;

    private Map<String, String> aliases;

    private Map<String, List<String>> aliasLookups;

    public UsageCommand(Map<String, Command> commands, Map<String, String> aliases) {
        Assert.notNull(commands, "commands cannot be null");
        this.commands = commands;

        if (aliases != null) {
            this.aliases = aliases;
            
            //use TreeMap so that aliases can be sorted
            aliasLookups = new TreeMap<String, List<String>>();
            Collection<String> aliasKeys = this.aliases.keySet();
            for (String alias : aliasKeys) {
                String command = aliases.get(alias);
                List<String> list = aliasLookups.get(command);
                if (list == null) {
                    list = new ArrayList<String>();
                    aliasLookups.put(command, list);
                }
                list.add(alias);
            }
        }
        else {
            aliases = emptyStringStringMap();
            aliasLookups = emptyStringListMap();
        }
    }

    public boolean execute(CommandState commandState) {
        Set<String> commandNames = commands.keySet();
        for (String commandName : commandNames) {

            String description = "";

            Command command = commands.get(commandName);

            if (command != null) {
                CommandDefinition commandDefinition = command.getCommandDefinition();
                if (commandDefinition != null) {
                    description = commandDefinition.getDescription();
                }
            }
            
            String aliasText = null;
            List<String> aliases = aliasLookups.get(commandName);
            if (aliases != null) {
                StringBuffer buffer = new StringBuffer(" (aliases: ");
                for (String alias : aliases) {
                    buffer.append(alias);
                    buffer.append(", ");
                }
                buffer.delete(buffer.length()-2, buffer.length());
                buffer.append("): ");
                aliasText = buffer.toString();
            } else {
                aliasText = ": ";
            }

            System.out.println(commandName + aliasText + description);
            
        }
        return true;
    }

    public CommandDefinition getCommandDefinition() {
        return new CommandDefinition("Shows usage of commands");
    }

    private Map<String, String> emptyStringStringMap() {
        return Collections.<String, String> emptyMap();
    }
    private Map<String, List<String>> emptyStringListMap() {
        return Collections.<String,  List<String>> emptyMap();
    }

}
