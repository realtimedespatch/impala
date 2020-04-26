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

package org.impalaframework.web.resolver;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.impalaframework.file.ExtensionFileFilter;
import org.impalaframework.resolver.ModuleLocationResolver;
import org.impalaframework.util.ResourceUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.support.ServletContextResource;

/**
 * Implementation of {@link ModuleLocationResolver} designed for use when modules are deployed as JARs in 
 * <code>WEB-INF/modules</code>. The scheme depends on the application version, specified by default in 
 * <i>impala.properties</i>
 * 
 * For a module name 'mymodule', and <i>application.version</i> = 1.0, the module 'mymodule' will be found in 
 * <code>WEB-INF/modules/mymodule-1.0.jar</code>
 * 
 * @author Phil Zoio
 */
public class ServletContextModuleLocationResolver implements ModuleLocationResolver, ServletContextAware, InitializingBean {

    private static final Log logger = LogFactory.getLog(ServletContextModuleLocationResolver.class);
    
    private String applicationVersion;

    private String relativeModuleRootLocation = "/WEB-INF/modules";

    private ServletContext servletContext;
    
    public ServletContextModuleLocationResolver() {
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(relativeModuleRootLocation, "relativeModuleRootLocation cannot be null");
    }

    public Resource getRootDirectory() {
        return new ServletContextResource(servletContext, relativeModuleRootLocation);
    }
    
    /**
     * Returns the single resource corresponding with the supplied module name. Expects resource to be in the directory
     * WEB-INF/modules, with the name 'module_name-application_version.jar'
     */
    public List<Resource> getApplicationModuleClassLocations(String moduleName) {
        String applicationVersionString = StringUtils.hasText(applicationVersion) ? "-" + applicationVersion : "";
        String folderResourceName = relativeModuleRootLocation + "/" + moduleName;
		String fullJarResourceName = folderResourceName + applicationVersionString + ".jar";
        Resource folderResource = new ServletContextResource(servletContext, folderResourceName);
        Resource jarResource = new ServletContextResource(servletContext, fullJarResourceName);
        return Arrays.asList(folderResource, 
        		jarResource);
    }
    
    /**
     * Returns the module-specific libraries corresponding with the supplied
     * module name. Expects libraries to be in the directory
     * WEB-INF/modules/lib/module_name
     */
    public List<Resource> getApplicationModuleLibraryLocations(String moduleName) {
        String parent = relativeModuleRootLocation + "/lib/" + moduleName;
        Resource parentResource = new ServletContextResource(servletContext, parent);
        try {
            File file = parentResource.getFile();
            if (file.exists()) {
                File[] files = file.listFiles(new ExtensionFileFilter(".jar"));
                if (files != null && files.length > 0) {
                    return Arrays.asList(ResourceUtils.getResources(files));
                }
            }
        }
        catch (IOException e) {
            logger.warn("Unable to get file corresponding with ");
        }
        return null;
    }

    public List<Resource> getModuleTestClassLocations(String moduleName) {
        throw new UnsupportedOperationException();
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public void setRelativeModuleRootLocation(String relativeModuleRootLocation) {
        this.relativeModuleRootLocation = relativeModuleRootLocation;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
