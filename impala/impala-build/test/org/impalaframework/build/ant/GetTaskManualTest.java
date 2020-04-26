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
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.tools.ant.Project;

import junit.framework.TestCase;

/**
 * @author Phil Zoio
 */
public class GetTaskManualTest extends TestCase {

    private File downloadDir;

    private GetTask task;

    public void setUp() throws MalformedURLException {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        downloadDir = new File(tmpDir, "downloads");
        downloadDir.mkdir();

        task = new GetTask();
        task.setBaseSourceUrls("http://ibiblio.org/pub/packages/maven2/,http://repository.jboss.com/maven2/");
        task.setToDir(downloadDir);
        task.setDependencies(new File("resources/test-dependencies.txt"));
        task.setProject(new Project());
    }

    public void tearDown() {
        File[] listFiles = downloadDir.listFiles();
        if (listFiles != null) {
            for (int i = 0; i < listFiles.length; i++) {
                System.out.println("Deleting " + listFiles[i]);
                listFiles[i].delete();
            }
        }
        downloadDir.delete();
    }

    public void testExecute() {
        task.execute();
        File[] listFiles = downloadDir.listFiles();
        assertEquals(1, listFiles.length);
    }

    public void testLocalExecute() throws Exception {
        File file = new File("C:\\Documents and Settings\\phil\\.m2\\repository");
        URL url = file.toURI().toURL();
        String urlString = url.toString();
        task.setBaseSourceUrls(urlString);
        task.execute();
        File[] listFiles = downloadDir.listFiles();
        assertEquals(1, listFiles.length);
    }

    public void testExecuteWithSource() {
        task.setDownloadSources(true);
        task.execute();
        File[] listFiles = downloadDir.listFiles();
        assertEquals(2, listFiles.length);
    }

}
