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

package org.impalaframework.command.basic;

import java.io.File;
import java.util.List;

import org.impalaframework.command.framework.Command;
import org.impalaframework.command.framework.CommandDefinition;
import org.impalaframework.command.framework.CommandInput;
import org.impalaframework.command.framework.CommandState;
import org.springframework.util.Assert;

public class SearchClassCommand implements Command {

    /**
     * result of search operation - either single String or null
     */
    private String className;

    /**
     * the directories on which the <code>ClassFindCommand</code> will operate
     */
    private List<File> classDirectories;

    public SearchClassCommand() {
    }

    public boolean execute(CommandState commandState) {

        Assert.notNull(classDirectories, "classDirectories cannot be null");

        while (className == null) {

            ClassFindCommand classFindCommand = newClassFindCommand();
            classFindCommand.setClassDirectories(classDirectories);

            CommandInput capturedInput = commandState.capture(classFindCommand);

            if (capturedInput.isGoBack()) {
                // skip the rest of this loop and start again
                continue;
            }

            classFindCommand.execute(commandState);

            List<String> foundClasses = classFindCommand.getFoundClasses();

            if (foundClasses.size() >= 2) {
                AlternativeInputCommand altInputCommand = new AlternativeInputCommand(foundClasses
                        .toArray(new String[foundClasses.size()]));

                CommandInput input = commandState.capture(altInputCommand);
                if (input.isGoBack()) {
                    // start again
                    continue;
                }

                altInputCommand.execute(commandState);
                className = altInputCommand.getSelectedAlternative();
            }
            else if (foundClasses.size() == 1) {
                className = foundClasses.get(0);
            }
            else {
                System.out.println("No class found in locations " + classDirectories);
            }
        }
        return true;
    }

    protected ClassFindCommand newClassFindCommand() {
        ClassFindCommand classFindCommand = new ClassFindCommand();
        return classFindCommand;
    }

    public void setClassDirectories(List<File> classDirectories) {
        this.classDirectories = classDirectories;
    }

    public CommandDefinition getCommandDefinition() {
        return CommandDefinition.EMPTY;
    }

    public String getClassName() {
        return className;
    }
}
