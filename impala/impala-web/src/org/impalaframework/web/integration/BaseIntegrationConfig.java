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

package org.impalaframework.web.integration;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.ServletContext;

import org.springframework.util.Assert;

/**
 * Base implementation class, shared by <code>IntegrationServletConfig</code> and <code>IntegrationFilterConfig</code>.
 * @author Phil Zoio
 */
public abstract class BaseIntegrationConfig {
    
    private Map<String,String> initParameterMap;
    private ServletContext servletContext;
    private String name;
    
    public BaseIntegrationConfig(Map<String, String> initParameterMap, ServletContext servletContext, String servletName) {
        super();
        Assert.notNull(initParameterMap, "initParameterMap cannot be null");
        Assert.notNull(servletContext, "servletContext cannot be null");
        Assert.notNull(servletName, "servletName cannot be null");
        this.initParameterMap = initParameterMap;
        this.servletContext = servletContext;
        this.name = servletName;
    }

    public String getInitParameter(String name) {
        return initParameterMap.get(name);
    }

    public Enumeration<?> getInitParameterNames() {
        Hashtable<String, String> hashtable = new Hashtable<String, String>(this.initParameterMap);
        return hashtable.keys();
    }

    public ServletContext getServletContext() {
        return this.servletContext;
    }

    public String getName() {
        return this.name;
    }

}
