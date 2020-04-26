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

package org.impalaframework.web.spring;

import java.io.File;
import java.util.List;

import javax.servlet.ServletContext;

import org.impalaframework.spring.SystemPropertyBasedPlaceholderConfigurer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;

public class WebappPropertyPlaceholderConfigurer extends SystemPropertyBasedPlaceholderConfigurer {

    public static final String WEBAPP_CONFIG_PROPERTY_NAME = "webappName";

    private static final Log logger = LogFactory.getLog(WebappPropertyPlaceholderConfigurer.class);

    private String webContextName;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (applicationContext instanceof WebApplicationContext) {
            WebApplicationContext webContext = (WebApplicationContext) applicationContext;
            ServletContext servletContext = webContext.getServletContext();
            webContextName = getWebContextName(servletContext);
        }
    }

    protected void addFileResources(String folderLocation, List<Resource> resources, String fileLocation) {

        super.addFileResources(folderLocation, resources, fileLocation);

        if (webContextName != null) {
            File file = new File(folderLocation + File.separator + webContextName + File.separator + fileLocation);

            if (file.exists()) {
                logger.info("Overriding deltas for properties for location " + fileLocation + " from "
                        + file.getAbsolutePath());
                resources.add(new FileSystemResource(file));
            }
        }
    }

    public String getWebContextName(ServletContext servletContext) {
        Assert.notNull(servletContext, "servletContext cannot be null");
        String webContextName = servletContext.getInitParameter(WEBAPP_CONFIG_PROPERTY_NAME);

        if (webContextName == null) {
            logger.warn("web.xml for " + servletContext.getServletContextName()
                    + " does not define the context parameter (using the element 'context-param' with name "
                    + WEBAPP_CONFIG_PROPERTY_NAME + ")");
        }
        return webContextName;
    }
}
