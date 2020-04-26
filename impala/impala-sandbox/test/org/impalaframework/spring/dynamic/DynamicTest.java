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

package org.impalaframework.spring.dynamic;

import java.io.File;

import org.impalaframework.spring.classloader.CustomClassLoaderFactory;
import org.impalaframework.spring.dynamic.impl.Person;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

import junit.framework.TestCase;

public class DynamicTest extends TestCase {

	public void testBean() throws Exception {

		// create the class loader
		File dynamicClassLocation = new File("../dynamic-spring-support/bin");
		DynamicClassLoader classLoader = new DynamicClassLoader();
		CustomClassLoaderFactory factory = new CustomClassLoaderFactory();

		factory.setLocation(dynamicClassLocation);
		classLoader.setFactory(factory);

		// create the bean factory
		DynamicBeanFactory beanFactory = new DynamicBeanFactory();
		beanFactory.setBeanClassLoader(classLoader);

		// create the scope, and register with the bean factory
		DynamicScope dynamicScope = new DynamicScope();
		dynamicScope.setClassLoader(classLoader);
		beanFactory.registerScope("dynamic", dynamicScope);

		// create the application context, and set the class loader
		GenericApplicationContext context = new GenericApplicationContext(beanFactory);
		context.setClassLoader(classLoader);

		// create the bean definition reader
		XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(context);
		xmlReader.loadBeanDefinitions(new ClassPathResource("dynamic/dynamic-context.xml", classLoader));

		// refresh the application context - now we're ready to go
		context.refresh();

		assertNotNull(context.getBean("communicationMethod"));
		assertNotNull(context.getBean("communicationMethodImpl"));

		Person ti = (Person) context.getBean("person");

		while (true) {
			ti.act();
			Thread.sleep(500);
		}
	}
}
