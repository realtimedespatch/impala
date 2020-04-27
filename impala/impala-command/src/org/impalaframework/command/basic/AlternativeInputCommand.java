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

import java.util.Map;

import org.impalaframework.command.framework.Command;
import org.impalaframework.command.framework.CommandDefinition;
import org.impalaframework.command.framework.CommandInfo;
import org.impalaframework.command.framework.CommandPropertyValue;
import org.impalaframework.command.framework.CommandState;
import org.springframework.util.Assert;


/**
 * Command which has job of capturing one or more potential inputs
 * @author Phil Zoio
 *
 */
public class AlternativeInputCommand implements Command {

    private String[] alternatives;

    private String selectedAlternative;

    public AlternativeInputCommand(String[] alternatives) {
        super();
        Assert.notNull(alternatives, "alternatives cannot be null");
        // no point executing this command unless we have two or more
        // alternatives
        Assert.isTrue(alternatives.length > 1, "alternatives cannot be empty");
        this.alternatives = alternatives;
    }

    public boolean execute(CommandState commandState) {
        Map<String, CommandPropertyValue> properties = commandState.getProperties();
        CommandPropertyValue classHolder = properties.get("selection");

        Assert.notNull(classHolder, "classHolder cannot be null");
        String selectedValue = classHolder.getValue();
        Assert.notNull(selectedValue, "selectedValue cannot be null");

        // this has been validated, so should not be negative
        selectedAlternative = alternatives[Integer.parseInt(selectedValue) - 1];
        return true;
    }

    public CommandDefinition getCommandDefinition() {

        CommandDefinition commandDefinition = new CommandDefinition();

        String[] extraLines = new String[alternatives.length];

        for (int i = 0; i < extraLines.length; i++) {
            extraLines[i] = (i + 1) + " " + alternatives[i];
        }

        CommandInfo ci1 = new CommandInfo("selection", "Selected value", "More than one alternative was found.\nPlease choose option by entering digit corresponding with selection",
                null,
                extraLines, true, false, false, false) {
            @Override
            public String validate(String input) {
                int selection = -1;
                try {
                    selection = Integer.parseInt(input);
                }
                catch (NumberFormatException e) {
                    return "Invalid Selection. Please select a number corresponding with one of the alternative choices";
                }

                if (selection <= 0 || selection > alternatives.length) {
                    return "Invalid Selection. Selected number does not correspond with one of the values."
                            + "\nPlease select a number between 1 and " + alternatives.length;
                }

                return null;
            }

        };
        commandDefinition.add(ci1);
        return commandDefinition;
    }

    public String getSelectedAlternative() {
        return selectedAlternative;
    }

}
