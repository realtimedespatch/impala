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

package org.impalaframework.spring.module.loader;

import org.impalaframework.module.ModuleDefinition;
import org.impalaframework.module.spi.Application;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

/**
 * @author Phil Zoio
 */
public class ApplicationModuleLoader extends BaseSpringModuleLoader {

    public ApplicationModuleLoader() {
        super();
    }

    @Override
    public GenericApplicationContext newApplicationContext(Application application,
            ApplicationContext parent, ModuleDefinition moduleDefinition, ClassLoader classLoader) {
        //note that if parent is null, the module must be a sibling of root
        return super.newApplicationContext(application, parent, moduleDefinition, classLoader);
    }
    
}
