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

import java.net.URL;
import java.util.List;

import org.impalaframework.command.framework.CommandDefinition;
import org.impalaframework.command.framework.CommandInfo;
import org.impalaframework.command.framework.CommandPropertyValue;
import org.impalaframework.command.framework.CommandState;
import org.impalaframework.command.framework.GlobalCommandState;
import org.impalaframework.command.framework.TextParsingCommand;
import org.impalaframework.exception.NoServiceException;
import org.impalaframework.facade.Impala;
import org.impalaframework.module.source.IncrementalModuleDefinitionSource;
import org.impalaframework.module.source.ModuleResourceUtils;
import org.impalaframework.resolver.ModuleLocationResolver;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public class ChangeDirectoryCommand implements TextParsingCommand {

    static final String DIRECTORY_NAME = ChangeDirectoryCommand.class.getName() + ".directoryName";

    private ModuleLocationResolver moduleLocationResolver;

    public ChangeDirectoryCommand() {
        super();
        this.moduleLocationResolver = Impala.getFacade().getModuleManagementFacade().getModuleLocationResolver();
    }
    
    public ChangeDirectoryCommand(ModuleLocationResolver moduleLocationResolver) {
        super();
        Assert.notNull(moduleLocationResolver, "moduleLocationResolver cannot be null");
        this.moduleLocationResolver = moduleLocationResolver;
    }

    public boolean execute(CommandState commandState) {
        CommandPropertyValue suppliedValue = commandState.getProperties().get(DIRECTORY_NAME);
        
        //we can call this because the getCommandDefinition contract demands this
        Assert.notNull(suppliedValue, "direct name property cannot be null");

        String candidateValue = suppliedValue.getValue();

        List<Resource> locations = moduleLocationResolver.getApplicationModuleClassLocations(candidateValue);

        boolean exists = false;

        for (Resource resource : locations) {
            if (!exists && resource.exists()) {
                exists = true;
            }
        }

        if (exists == false) {
            System.out.println("No module locations corresponding to '" + candidateValue + "' exist");
            return false;
        }
        else {
            try {               
                Impala.getRuntimeModule(candidateValue);
            } catch (NoServiceException e) {                
                URL moduleProperties = ModuleResourceUtils.loadModuleResource(moduleLocationResolver, candidateValue, "module.properties");
                
                if (moduleProperties == null) {
                    System.out.println("Cannot change to directory '" + candidateValue + "' as it corresponds to a module which has not been loaded," +
                            "and no module.properties can be found in the classpath for this module .");
                    return false;
                } else {
                    try {
                        
                        IncrementalModuleDefinitionSource definitionSource = new IncrementalModuleDefinitionSource(
                                Impala.getFacade().getModuleManagementFacade().getModuleLocationResolver(),
                                Impala.getFacade().getModuleManagementFacade().getTypeReaderRegistry(),
                                Impala.getRootModuleDefinition(), 
                                candidateValue);
                        Impala.init(definitionSource);
                        
                    } catch (Exception ee) {
                        ee.printStackTrace();
                        return false;
                    }
                }
            }
        }
        
        GlobalCommandState.getInstance().addValue(CommandStateConstants.DIRECTORY_NAME, candidateValue);
        GlobalCommandState.getInstance().clearValue(CommandStateConstants.TEST_CLASS);
        GlobalCommandState.getInstance().clearValue(CommandStateConstants.TEST_CLASS_NAME);
        GlobalCommandState.getInstance().clearValue(CommandStateConstants.TEST_METHOD_NAME);
        GlobalCommandState.getInstance().clearValue(CommandStateConstants.MODULE_DEFINITION_SOURCE);
        
        System.out.println("Current directory set to " + candidateValue);
        return true;
    }

    public CommandDefinition getCommandDefinition() {

        // note that globalOverrides is true, so that the user will not be
        // prompted for the module name if it already there
        CommandInfo info = new CommandInfo(DIRECTORY_NAME, "Directory name",
                "Please enter directory to use as current working directory.", null, null, false, false, true, false);

        CommandDefinition definition = new CommandDefinition("Changes working directory");
        definition.add(info);
        return definition;
    }

    public void extractText(String[] text, CommandState commandState) {

        if (text.length > 0) {
            commandState.addProperty(DIRECTORY_NAME, new CommandPropertyValue(text[0]));
        }

    }

}
