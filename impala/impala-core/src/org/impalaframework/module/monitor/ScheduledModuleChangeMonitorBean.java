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

package org.impalaframework.module.monitor;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Extends {@link ScheduledModuleChangeMonitor}, adding implementations of the
 * Spring-specific life cycle methods {@link InitializingBean} and
 * {@link DisposableBean}
 * 
 * @author Phil Zoio
 */
public class ScheduledModuleChangeMonitorBean extends ScheduledModuleChangeMonitor implements InitializingBean, DisposableBean {

    public void afterPropertiesSet() throws Exception {
        this.start();
    }

    public void destroy() throws Exception {
        this.stop();
    }
}
