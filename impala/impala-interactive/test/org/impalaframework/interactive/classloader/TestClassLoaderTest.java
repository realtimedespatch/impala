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

package org.impalaframework.interactive.classloader;

import java.io.File;
import java.util.Map;

import junit.framework.TestCase;

public class TestClassLoaderTest extends TestCase {

    public void testLoadClassString() throws Exception {
        TestClassLoader tcl = new TestClassLoader(new File[] {new File("../impala-interactive/testbin")}, "org.impalaframework.interactive.testrun.AJUnitTest");

        // check that this class loader loads the named class
        Class<?> cls = Class.forName("org.impalaframework.interactive.testrun.AJUnitTest", false, tcl);
        assertSame(cls.getClassLoader(), tcl);

        Map<String, Class<?>> loadedClasses = tcl.getLoadedClasses();
        assertEquals(1, loadedClasses.size());
        assertNotNull(loadedClasses.get("org.impalaframework.interactive.testrun.AJUnitTest"));

        // but not a String
        cls = Class.forName("java.lang.String", false, tcl);
        assertNotSame(cls.getClassLoader(), tcl);

        cls = Class.forName("org.impalaframework.interactive.testrun.BaseTest", false, tcl);
        assertNotSame(cls.getClassLoader(), tcl);
    }

}
