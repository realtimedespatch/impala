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

package org.impalaframework.spring;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.impalaframework.exception.ConfigurationException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public abstract class ExtendedPropertyPlaceholderConfigurer extends PropertySourcesPlaceholderConfigurer {
    
    private static final Log logger = LogFactory.getLog(ExtendedPropertyPlaceholderConfigurer.class);

    private String[] fileLocations;

    public static final String CLASSPATH_PREFIX = "classpath:";

    protected abstract String getAlternativeFolderLocation();
    
    @Override
    public void setLocation(Resource location) {
        throw new ConfigurationException("Use 'fileLocation' property instead");
    }

    @Override
    public void setLocations(Resource[] locations) {
        throw new ConfigurationException("Use 'fileLocations' property instead");
    }

    /**
     * Injected property. Defines file location relative to base property folder
     */
    public void setFileLocation(String fileLocation) {
        this.fileLocations = new String[] { fileLocation };
    }

    /**
     * Injected property. Defines file locations relative to base property
     * folder
     */
    public void setFileLocations(String[] fileLocations) {
        this.fileLocations = fileLocations;
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (fileLocations == null) {
            Assert.notNull(fileLocations, "Property 'fileLocations' cannot be null");
        }

        internalSetLocations();
        super.postProcessBeanFactory(beanFactory);
    }

    protected void internalSetLocations() {

        String folderLocation = getAlternativeFolderLocation();

        if (folderLocation != null) {
            File folderFile = new File(folderLocation);

            if (!folderFile.exists()) {
                logger.warn("Property folder " + folderFile + " does not exist - cannot override any properties");
            }
            if (!folderFile.isDirectory()) {
                logger.warn("Property folder " + folderFile + " is not a directory - cannot override any properties");
            }
        }

        List<Resource> locationList = new ArrayList<Resource>();

        for (String fileLocation : fileLocations) {
            locationList.addAll(getLocations(folderLocation, fileLocation));
        }
        
        super.setLocations(locationList.toArray(new Resource[locationList.size()]));
    }

    protected List<Resource> getLocations(String folderLocation, String suppliedFileLocation) {
        List<Resource> resources = new ArrayList<Resource>();
        String fileLocation = suppliedFileLocation;

        boolean classPathLocation = false;

        // strip off classpath
        if (fileLocation.startsWith(CLASSPATH_PREFIX)) {
            classPathLocation = true;
            fileLocation = fileLocation.substring(CLASSPATH_PREFIX.length());
        }

        Resource classPathResource = getClassPathResource(suppliedFileLocation, fileLocation);

        if (classPathResource.exists()) {
            // find the classpath resource
            resources.add(classPathResource);
        }

        if (classPathLocation)
            return resources;

        if (null != folderLocation) {
            addFileResources(folderLocation, resources, fileLocation);
        }
        return resources;
    }

    protected void addFileResources(String folderLocation, List<Resource> resources, String fileLocation) {
        File file = new File(folderLocation + File.separator + fileLocation);
        if (file.exists()) {
            logger.info("Overriding deltas for properties for location " + fileLocation + " from "
                    + file.getAbsolutePath());
            resources.add(new FileSystemResource(file));
        }
    }

    protected Resource getClassPathResource(String suppliedFileLocation, String fileLocation) {
        logger.info("Loading properties for location " + suppliedFileLocation + " from classpath");
        return new ClassPathResource(fileLocation);
    }
    
}
