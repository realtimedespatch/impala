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

import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

import junit.framework.TestCase;

/**
 * @author Phil Zoio
 */
public class ExpandableTest extends TestCase {

	public void test() throws InterruptedException {
		GenericApplicationContext context = new GenericApplicationContext();

		XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(context);
		xmlReader.loadBeanDefinitions(new ClassPathResource("expandable/core-context.xml"));

		context.refresh();

		RootModuleContextMonitor monitor = new RootModuleContextMonitor(xmlReader, new ClassPathResource(
				"expandable/spring-locations.txt"), new ScheduledThreadPoolExecutor(1));
		monitor.setupMonitor();

		context.getBean("coreBean");

		while (true) {
			try {
				Thread.sleep(1000);
				Object bean = context.getBean("extraBean");
				System.out.println("Found bean named 'extraBean' in context: " + bean.getClass().getName());
			}
			catch (NoSuchBeanDefinitionException e) {
				System.out.println("No bean named 'extraBean' found in context");
			}
		}
	}
}
