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

package org.impalaframework.build.ant;

import java.io.File;
import java.util.List;

import org.apache.tools.ant.BuildException;

import junit.framework.TestCase;

/**
 * @author Phil Zoio
 */
public class AntForeachTaskTest extends TestCase {

    private AntForeachTask task;

    public void testGetSubdirectories() {
        task = new AntForeachTask();
        File root = new File("../");
        task.setDir(root);
        task.setProjects("impala,impala-core");
        List<File> subdirectories = task.getSubdirectories();
        assertEquals(2, subdirectories.size());
        assertTrue(subdirectories.contains(new File(root, "impala")));
    }

    public void testCheckArgs() {
        task = new AntForeachTask();
        doFail("'values' property not specified");
        task.setProjects("value1,value2");
        doFail("'dir' property not specified");
        task.setDir(new File("a file that does not exit"));
        doFail("'dir' does not exist");
        task.setDir(new File(".classpath"));
        doFail("'dir' must be a directory");
    }
    
    private void doFail(String expected) {
        try {
            task.checkArgs();
            fail();
        }
        catch (BuildException e) {
            System.out.println("Message: " + e.getMessage());
            assertTrue(e.getMessage().contains(expected));
        }
    }

}
