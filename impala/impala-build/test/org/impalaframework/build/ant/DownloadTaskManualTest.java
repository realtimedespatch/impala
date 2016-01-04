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
import java.util.Arrays;

/**
 * @author Phil Zoio
 */
public class DownloadTaskManualTest extends BaseDownloadTaskTest {

    public void setUp() {
        super.setUp();
        task.setDependencies(new File("resources/download-dependencies.txt"));
    }

    public void testExecute() {
        task.execute();
        File[] listFiles = downloadDir.listFiles();
        assertEquals(2, listFiles.length);
    }
    
    public void testExecuteWithSource() {
        task.setDownloadSources(true);
        task.execute();
        File[] listFiles = downloadDir.listFiles();
        assertEquals(4, listFiles.length);
    }
    
    public void testMoreDependencies() {
        task.setDownloadSources(true);
        task.setBaseSourceUrls("http://repo1.maven.org/maven2/,http://repository.jboss.com/maven2/,http://ibiblio.org/pub/packages/maven2/");
        task.setDependencies(new File("resources/more-dependencies.txt"));
        task.execute();
        File[] listFiles = downloadDir.listFiles();
        System.out.println(Arrays.toString(listFiles));
        
        System.out.println(downloadDir.getAbsolutePath());
        assertEquals(6, listFiles.length);
    }
    
    public void testExecuteWithMultipeRepos() throws MalformedURLException {
        File file = new File("C:\\Documents and Settings\\phil\\.m2\\repository");
        URL url = file.toURI().toURL();
        String urlString = url.toString();
        task.setBaseSourceUrls(urlString+",http://ibiblio.org/pub/packages/maven2/");
        task.setDownloadSources(true);
        task.execute();
        File[] listFiles = downloadDir.listFiles();
        assertEquals(2, listFiles.length);
    }

}
