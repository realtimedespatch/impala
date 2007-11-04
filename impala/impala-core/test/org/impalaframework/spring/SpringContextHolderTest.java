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

package org.impalaframework.spring;

import java.io.File;

import junit.framework.TestCase;

import org.impalaframework.file.monitor.FileMonitor;
import org.impalaframework.plugin.loader.ApplicationPluginLoader;
import org.impalaframework.plugin.loader.ParentPluginLoader;
import org.impalaframework.plugin.loader.PluginLoaderRegistry;
import org.impalaframework.plugin.plugin.NoServiceException;
import org.impalaframework.plugin.plugin.PluginSpec;
import org.impalaframework.plugin.plugin.PluginSpecBuilder;
import org.impalaframework.plugin.plugin.PluginTypes;
import org.impalaframework.plugin.plugin.SimplePluginSpec;
import org.impalaframework.plugin.plugin.SimplePluginSpecBuilder;
import org.impalaframework.plugin.util.ApplicationContextLoader;
import org.impalaframework.plugin.util.RegistryBasedApplicationContextLoader;
import org.impalaframework.resolver.ClassLocationResolver;
import org.impalaframework.resolver.PropertyClassLocationResolver;
import org.impalaframework.spring.SpringContextHolder;
import org.springframework.context.ApplicationContext;

/**
 * @author Phil Zoio
 */
public class SpringContextHolderTest extends TestCase {

	private SpringContextHolder holder;

	private static final String plugin1 = "impala-sample-dynamic-plugin1";

	private static final String plugin2 = "impala-sample-dynamic-plugin2";

	private static final String plugin3 = "impala-sample-dynamic-plugin3";

	public void setUp() {
		System.setProperty("impala.parent.project", "impala-sample-dynamic");
		
		PluginLoaderRegistry registry = new PluginLoaderRegistry();
		ClassLocationResolver resolver = new PropertyClassLocationResolver();
		registry.setPluginLoader(PluginTypes.ROOT, new ParentPluginLoader(resolver));
		registry.setPluginLoader(PluginTypes.APPLICATION, new ApplicationPluginLoader(resolver));
		
		ApplicationContextLoader loader = new RegistryBasedApplicationContextLoader(registry);
		holder = new SpringContextHolder(loader);
	}

	public void testFindPlugin() {
		PluginSpecBuilder spec = new SimplePluginSpecBuilder("parentTestContext.xml", new String[] { plugin1, plugin2 });
		PluginSpec p2 = spec.getParentSpec().getPlugin(plugin2);
		new SimplePluginSpec(p2, plugin3);

		holder.setSpringContextSpec(spec.getParentSpec());

		assertNotNull(holder.getPlugin(plugin1));
		assertNotNull(holder.getPlugin(plugin2));
		assertNotNull(holder.getPlugin(plugin3));
		
		assertNull(holder.getPlugin("plugin3"));
		assertNotNull(holder.findPluginLike("plugin3"));
	}


	
	public void testSpringContextHolder() {

		PluginSpecBuilder spec = new SimplePluginSpecBuilder("parentTestContext.xml", new String[] { plugin1, plugin2 });
		holder.loadParentContext(spec.getParentSpec());
		assertTrue(holder.hasPlugin(plugin1));
		assertTrue(holder.hasPlugin(plugin2));

		ApplicationContext parent = holder.getContext();
		assertNotNull(parent);
		assertEquals(3, holder.getPlugins().size());

		FileMonitor bean1 = (FileMonitor) parent.getBean("bean1");
		assertEquals(999L, bean1.lastModified((File)null));

		FileMonitor bean2 = (FileMonitor) parent.getBean("bean2");
		assertEquals(100L, bean2.lastModified((File)null));

		// shutdown plugin and check behaviour has gone
		holder.removePlugin(spec.getParentSpec().getPlugin(plugin2));
		assertFalse(holder.hasPlugin(plugin2));

		try {
			bean2.lastModified((File)null);
			fail();
		}
		catch (NoServiceException e) {
		}

		// bean 2 still works
		assertEquals(999L, bean1.lastModified((File)null));

		holder.removePlugin(spec.getParentSpec().getPlugin(plugin1));
		assertFalse(holder.hasPlugin(plugin2));

		try {
			bean1.lastModified((File)null);
			fail();
		}
		catch (NoServiceException e) {
		}

		// now reload the plugin, and see that behaviour returns
		holder.addPlugin(new SimplePluginSpec(plugin2));
		bean2 = (FileMonitor) parent.getBean("bean2");
		assertEquals(100L, bean2.lastModified((File)null));

		holder.addPlugin(new SimplePluginSpec(plugin1));
		bean1 = (FileMonitor) parent.getBean("bean1");
		assertEquals(999L, bean1.lastModified((File)null));

		assertTrue(holder.hasPlugin(plugin1));
		assertTrue(holder.hasPlugin(plugin2));

		// shut parent context and see that NoServiceException comes
		holder.shutParentConext();

		try {
			bean1.lastModified((File)null);
			fail();
		}
		catch (NoServiceException e) {
		}

		try {
			bean2.lastModified((File)null);
			fail();
		}
		catch (NoServiceException e) {
		}
	}

}