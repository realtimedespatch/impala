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

package org.impalaframework.spring.module;

import org.impalaframework.module.ModuleDefinition;
import org.impalaframework.module.definition.ModuleDefinitionAware;
import org.impalaframework.module.definition.SimpleRootModuleDefinition;

import junit.framework.TestCase;

public class ModuleDefinitionPostProcessorTest extends TestCase {

    public final void testPostProcessBeforeInitialization() {
        SimpleRootModuleDefinition rootDefinition = new SimpleRootModuleDefinition("project1", "context.xml");
        ModuleDefinitionPostProcessor postProcessor = new ModuleDefinitionPostProcessor(rootDefinition);
        TestSpecAware testAware = new TestSpecAware();
        postProcessor.postProcessBeforeInitialization(testAware, null);
        assertSame(rootDefinition, testAware.getModuleDefinition());
        
        assertSame(testAware, postProcessor.postProcessAfterInitialization(testAware, null));
    }
}

class TestSpecAware implements ModuleDefinitionAware {

    private ModuleDefinition moduleDefinition;

    public ModuleDefinition getModuleDefinition() {
        return moduleDefinition;
    }

    public void setModuleDefinition(ModuleDefinition moduleDefinition) {
        this.moduleDefinition = moduleDefinition;
    }

}
