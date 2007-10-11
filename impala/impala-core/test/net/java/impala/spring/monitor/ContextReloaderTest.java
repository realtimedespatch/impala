/*
 * Copyright 2007 the original author or authors.
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

package net.java.impala.spring.monitor;

import java.util.concurrent.ScheduledExecutorService;

import net.java.impala.monitor.FileMonitorImpl;

import junit.framework.TestCase;

public class ContextReloaderTest extends TestCase {

	public void testSetDefaultsIfNecessary() {
		ContextReloader cl = new ContextReloader();
		cl.setDefaultsIfNecessary();
		
		assertTrue(cl.getExecutor() instanceof ScheduledExecutorService);
		assertTrue(cl.getFileMonitor() instanceof FileMonitorImpl);
		assertEquals(10, cl.getInitialDelay());
		assertEquals(5, cl.getInterval());
	}

}
