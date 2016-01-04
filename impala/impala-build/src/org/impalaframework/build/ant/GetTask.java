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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Get;
import org.apache.tools.ant.taskdefs.Get.VerboseProgress;

/**
 * Used to perform downloads of individually identified files
 * @author Phil Zoio
 */
public class GetTask extends Task {

    private File dependencies;

    private String baseSourceUrls;

    private File toDir;

    private boolean downloadSources;

    private boolean failOnError;
    
    private boolean verbose;

    private List<Result> results = new ArrayList<Result>();

    @Override
    public void execute() throws BuildException {

        if (dependencies == null) {
            throw new BuildException(
                    "'dependencies' cannot be null. It should refer to a File containing a list of dependencies",
                    getLocation());
        }

        // check dependencies exists and is a file
        if (!dependencies.exists()) {
            if (failOnError)
                throw new BuildException("The location refered to by 'dependencies' does not exist", getLocation());
            else {
                log("Dependencies file " + dependencies + " does not exist", Project.MSG_INFO);
                return;
            }
        }

        if (!dependencies.isFile()) {
            if (failOnError)
                throw new BuildException("The location refered to by 'dependencies' is not a file", getLocation());
        }

        if (toDir == null) {
            throw new BuildException(
                    "'toDir' cannot be null. It should refer to a File pointing to download target location",
                    getLocation());
        }

        if (baseSourceUrls == null) {
            throw new BuildException(
                    "'baseSourceUrls' cannot be null. It should point to the base location from where artifacts are downloaded",
                    getLocation());
        }

        // check toDir exists and is a directory
        if (!toDir.exists()) {
            throw new BuildException("The location refered to by 'toDir' does not exist", getLocation());
        }

        if (!toDir.isDirectory()) {
            throw new BuildException("The location refered to by 'toDir' is not a directory", getLocation());
        }

        List<String> fileList = getFileList();

        String[] sourceUrls = baseSourceUrls.split(",");

        log("Using following locations to retrieve resources: ");
        log("-------------------------------------------------");
        for (int i = 0; i < sourceUrls.length; i++) {
            sourceUrls[i] = sourceUrls[i].trim();
            if (!sourceUrls[i].endsWith("/")) {
                sourceUrls[i] = sourceUrls[i] + "/";
            }
            log(sourceUrls[i]);
        }
        log("-------------------------------------------------");
        
        for (String file : fileList) {

            doDownload(file, sourceUrls);
        }

        printResults();

    }
    
    protected void doDownload(String file, String[] sourceUrls) {
        List<DownloadInfo> di = getDownloadInfos(file);

        for (DownloadInfo info : di) {

            log("Downloading: " + info.getUrlString());
            doDownload(sourceUrls, info.getUrlString(), info.getFile());
        }
    }

    protected List<DownloadInfo> getDownloadInfos(String urlString) {
        List<DownloadInfo> di = new LinkedList<DownloadInfo>();

        final String url = urlString;

        String fileName = url;

        int lastSlash = url.lastIndexOf("/");

        if (lastSlash > 0) {
            fileName = url.substring(lastSlash + 1);
        }

        DownloadInfo downloadInfo = new DownloadInfo(url, new File(toDir, fileName));

        di.add(downloadInfo);

        if (downloadSources) {
            int lastDot = url.lastIndexOf(".");

            if (lastDot > 0 && lastDot > lastSlash) {
                String sourceUrl = url.substring(0, lastDot) + "-sources" + url.substring(lastDot);

                int lastFileDot = fileName.lastIndexOf(".");
                String sourceFileName = fileName.substring(0, lastFileDot) + "-sources"
                        + fileName.substring(lastFileDot);

                di.add(new DownloadInfo(sourceUrl, new File(toDir, sourceFileName)));
            }
        }
        return di;
    }

    private void doDownload(String[] sourceUrls, String url, File toFile) {

        log("Retrieving new resource if available for " + url);
        toFile.getParentFile().mkdirs();

        boolean succeeded = false;
        
        for (int i = 0; i < sourceUrls.length && succeeded == false; i++) {
            
            final String urlString = sourceUrls[i] + url;
            try {
                
                URL srcUrl = new URL(urlString);

                if ("file".equals(srcUrl.getProtocol())) {

                    //log("Copying if applicable " + srcUrl + " to file: " + toFile, Project.MSG_DEBUG);                    

                    Copy copy = new Copy();
                    copy.setProject(getProject());
                    
                    copy.init();
                    copy.setFile(new File(srcUrl.getFile()));
                    copy.setTofile(toFile);
                    copy.setPreserveLastModified(true);
                    copy.setTaskName("copy");
                    copy.execute();

                    results.add(new Result(url, Result.COPIED, srcUrl));
                    succeeded = true;
                }
                else {
                    
                    Get get = new Get();
                    get.setProject(getProject());
                    get.setIgnoreErrors(false);
                    
                    //log("Downloading if applicable from " + srcUrl + " to file: " + toFile);               
                    get.init();
                    get.setSrc(srcUrl);
                    get.setUseTimestamp(true);
                    get.setDest(toFile);
                    get.setDescription(getDescription());
                    get.setLocation(getLocation());
                    get.setOwningTarget(getOwningTarget());
                    get.setVerbose(this.verbose);
                    get.setTaskName("get");
                    try {
                        final boolean got = get.doGet(Project.MSG_INFO, new VerboseProgress(System.out));

                        final int result = got 
                                          ? Result.DOWNLOADED : 
                                          Result.NOT_MODIFIED;
                        
                        results.add(new Result(url, result, srcUrl));
                        succeeded = true;
                        
                        //log("Resolved from " + srcUrl + ". Actual download performed: " + got);        
                    }
                    catch (Exception e) {
                        //log("", Project.MSG_ERR);
                    }
                    
                }
                
            }
            catch (MalformedURLException e) {
                log("Unable to form valid url using " + url, Project.MSG_ERR);
            }
            catch (Exception e) {
                log("Unable to download " + url + " from url '" + urlString + "': " + e.getMessage(), Project.MSG_DEBUG);
            }
        }

        if (!succeeded) {
            results.add(new Result(url, Result.FAILED, null));
        }
    }

    private List<String> getFileList() {
        List<String> fileList = new ArrayList<String>();

        FileReader fileReader = null;
        try {
            fileReader = new FileReader(dependencies);
        }
        catch (FileNotFoundException e) {
            throw new BuildException("Could not find file " + dependencies.getAbsolutePath(), getLocation());
        }
        BufferedReader reader = new BufferedReader(fileReader);

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() > 0 && !line.startsWith("#"))
                    fileList.add(line);
            }
        }
        catch (IOException e) {
            throw new BuildException("Could not read file " + dependencies.getAbsolutePath(), getLocation());
        } finally {
        	
        	if (reader != null) {
        		try {
					reader.close();
				} catch (IOException e) {
				}
        	}
        	
        }
        return fileList;
    }

    private void printResults() {
        StringBuffer buffer = new StringBuffer();

        String lineSeparator = System.getProperty("line.separator");
        buffer.append("******************************************************");
        buffer.append(lineSeparator);
        buffer.append(lineSeparator);
        buffer.append("               RESULTS OF DOWNLOAD OPERATION                   ");
        buffer.append(lineSeparator);
        buffer.append(lineSeparator);

        if (results.size() > 0) {
            for (Result result : results) {
                buffer.append(result.toString()).append(lineSeparator).append(lineSeparator);
            }
        }
        else {
            buffer.append("No files to download");
        }

        buffer.append(lineSeparator);
        buffer.append("******************************************************");
        log(buffer.toString(), Project.MSG_INFO);
    }

    public void setBaseSourceUrls(String baseSourceUrl) {
        this.baseSourceUrls = baseSourceUrl;
    }

    public void setDependencies(File dependencies) {
        this.dependencies = dependencies;
    }

    public void setToDir(File toDir) {
        this.toDir = toDir;
    }

    public void setDownloadSources(boolean downloadSources) {
        this.downloadSources = downloadSources;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }
    
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /* ************************ protected getters *********************** */

    protected File getDependencies() {
        return dependencies;
    }

    protected boolean isDownloadSources() {
        return downloadSources;
    }

    protected boolean isFailOnError() {
        return failOnError;
    }

    protected File getToDir() {
        return toDir;
    }
    
    protected List<Result> getResults() {
        return Collections.unmodifiableList(results);
    }

}

class Result {

    static final int NOT_MODIFIED = 0;

    static final int FAILED = 1;

    static final int COPIED = 2;

    static final int DOWNLOADED = 3;

    public Result(String archive, int result, URL srcUrl) {
        super();
        if (archive == null)
            throw new IllegalArgumentException("archive cannot be null");
        if (NOT_MODIFIED > result || DOWNLOADED < result)
            throw new IllegalArgumentException("result must be between 0 and 3 (inclusive)");
        if (COPIED <= result && srcUrl == null)
            throw new IllegalArgumentException("success location required for successful result");

        this.archive = archive;
        this.result = result;
        this.successLocation = srcUrl;
    }

    private String archive;

    private URL successLocation;

    private int result;

    public String toString() {
        switch (result) {
        case NOT_MODIFIED:
            return archive + " not modified";

        case FAILED:
            return archive + " could not be downloaded from any location";

        case COPIED:
            return archive + " COPIED from \n" + successLocation;
            
        case DOWNLOADED:
            return archive + " DOWNLOADED from \n" + successLocation;
        default:
            throw new IllegalStateException("Should not get here");
        }
    }
}
