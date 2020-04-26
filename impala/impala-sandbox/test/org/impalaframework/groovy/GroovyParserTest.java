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

package org.impalaframework.groovy;

import java.util.List;

import groovy.lang.GroovyObject;
import groovy.lang.GroovyShell;
import junit.framework.TestCase;

public class GroovyParserTest extends TestCase {

	public void testParse() {
		GroovyObject go = GroovyParser.parse("class Test { \n String doStuff() { return \"Do stuff\" } }");
		String result = (String) go.invokeMethod("doStuff", null);
		assertEquals("Do stuff", result);
		
		//GroovyEngine ge = new GroovyEngine();
		GroovyShell sh = new GroovyShell();
		List<?> evaluate = (List<?>) sh.evaluate("[1, 2, 3]");
		System.out.println(evaluate);
	}

}
