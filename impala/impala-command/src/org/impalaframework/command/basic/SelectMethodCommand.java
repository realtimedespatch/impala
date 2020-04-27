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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.impalaframework.command.framework.Command;
import org.impalaframework.command.framework.CommandDefinition;
import org.impalaframework.command.framework.CommandInput;
import org.impalaframework.command.framework.CommandState;
import org.springframework.util.Assert;


public class SelectMethodCommand implements Command {

    private Class<?> testClass;

    private String methodName;

    public SelectMethodCommand(Class<?> testClass) {
        super();
        Assert.notNull(testClass, "testClass cannot be null");
        this.testClass = testClass;
    }

    public boolean execute(CommandState commandState) {
        
        Method[] methods = testClass.getMethods();

        while (methodName == null) {

            List<String> foundMethods = new ArrayList<String>();
            for (Method method : methods) {
                if (method.getParameterTypes().length == 0 && method.getName().startsWith("test")) {
                    foundMethods.add(method.getName());
                }
            }

            if (foundMethods.size() >= 2) {
                AlternativeInputCommand altInputCommand = new AlternativeInputCommand(foundMethods
                        .toArray(new String[foundMethods.size()]));

                CommandInput input = commandState.capture(altInputCommand);
                if (input.isGoBack()) {
                    // start again
                    continue;
                }

                altInputCommand.execute(commandState);
                methodName = altInputCommand.getSelectedAlternative();
            }
            else if (foundMethods.size() == 1) {
                methodName = foundMethods.get(0);
            }
            else {
                return false;
            }

        }
        return true;
    }

    public CommandDefinition getCommandDefinition() {
        return CommandDefinition.EMPTY;
    }

    public String getMethodName() {
        return methodName;
    }

}
