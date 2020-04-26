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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import junit.framework.TestCase;

/**
 * @author Phil Zoio
 */
public class GetTaskTest extends TestCase {

    private File downloadDir;

    private GetTask task;

    public void setUp() {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        downloadDir = new File(tmpDir, "downloads");
        downloadDir.mkdir();

        File file = new File("../impala-core/resources/test-dependencies.txt");
        File toDir = downloadDir;
        String baseUrl = "http://ibiblio.org/pub/packages/maven2/";

        task = new GetTask();
        task.setBaseSourceUrls(baseUrl);
        task.setToDir(toDir);
        task.setDependencies(file);
        task.setProject(new Project());
        task.setFailOnError(true);
    }

    public void testDuffDependencies() {
        task.setFailOnError(false);
        task.setDependencies(null);
        doFail("");
        
        task.setDependencies(new File("a file that does not exist"));
        task.execute();

        // can't be a directory
        task.setDependencies(new File("src"));
        doFail("");
    }

    public void testDuffToDir() {
        task.setToDir(null);
        doFail("");

        task.setToDir(new File("a file that does not exist"));
        doFail("");

        // can't be a file
        task.setDependencies(new File(".classpath"));
        doFail("");
    }

    public void testDuffBaseUrl() {
        task.setBaseSourceUrls(null);
        doFail("");
    }

    private void doFail(String expected) {
        try {
            task.execute();
            fail();
        }
        catch (BuildException e) {
            assertTrue(e.getMessage().contains(expected));
        }
    }

}
