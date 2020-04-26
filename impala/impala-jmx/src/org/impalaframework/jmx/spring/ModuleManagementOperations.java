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

package org.impalaframework.jmx.spring;

import org.impalaframework.module.operation.ModuleOperation;
import org.impalaframework.module.operation.ModuleOperationConstants;
import org.impalaframework.module.operation.ModuleOperationInput;
import org.impalaframework.module.operation.ModuleOperationRegistry;
import org.impalaframework.module.operation.ModuleOperationResult;
import org.impalaframework.module.spi.Application;
import org.impalaframework.module.spi.ApplicationManager;
import org.impalaframework.module.spi.TransitionResultSet;
import org.impalaframework.util.ExceptionUtils;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.Assert;

/**
 * Exposes JMX-based module reload operations.
 * @author Phil Zoio
 */
@ManagedResource(objectName = "impala:service=moduleManagementOperations", description = "MBean exposing configuration operations Impala application")
public class ModuleManagementOperations {

    private ModuleOperationRegistry moduleOperationRegistry;
    
    private ApplicationManager applicationManager;

    public void init() {
        Assert.notNull(moduleOperationRegistry, "moduleOperationRegistry cannot be null");
    }

    /**
     * Reloads a named module
     * @param moduleName the name of the module to reload. Uses the operation {@link ModuleOperationConstants#ReloadModuleNamedLikeOperation}.
     * If supplied module name does not exactly match a named module, then module will be loaded 
     */
    @ManagedOperation(description = "Operation to reload a module")
    @ManagedOperationParameters( { @ManagedOperationParameter(name = "Module name", description = "Name of module to reload") })
    public String reloadModule(String moduleName) {

        Assert.notNull(applicationManager, "applicationManager cannot be null");
        Assert.notNull(moduleOperationRegistry, "moduleOperationRegistry cannot be null");
        
        ModuleOperation operation = moduleOperationRegistry.getOperation(ModuleOperationConstants.ReloadModuleNamedLikeOperation);
        
        try {
            
            Application application = applicationManager.getCurrentApplication();
            ModuleOperationResult execute = operation.execute(application, new ModuleOperationInput(null, null, moduleName));
            
            TransitionResultSet transitionResultSet = execute.getTransitionResultSet();
            if (transitionResultSet.hasResults()) {
                if (transitionResultSet.isSuccess()) {
                    return "Successfully reloaded " + execute.getOutputParameters().get("moduleName");
                } else {
                    Throwable error = transitionResultSet.getFirstError();
                    return "One or more module operations failed: " + (error != null ? error.getMessage() : "error is null"); 
                }
            } else {
                return "Could not find module " + moduleName;
            }
        }
        catch (Throwable e) {
            return ExceptionUtils.getStackTrace(e);
        }
    }

    public void setModuleOperationRegistry(ModuleOperationRegistry moduleOperationRegistry) {
        this.moduleOperationRegistry = moduleOperationRegistry;
    }
    
    public void setApplicationManager(ApplicationManager applicationManager) {
        this.applicationManager = applicationManager;
    }

}
