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

import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.impalaframework.radixtree.RadixTree;
import org.impalaframework.radixtree.TreeNode;
import org.impalaframework.web.servlet.ModuleHttpServletRequest;
import org.impalaframework.web.servlet.wrapper.RequestModuleMapping;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletContextAware;

/**
 * Uses an internally held {@link RadixTree}, held using a {@link PrefixTreeHolder}, to map requests to modules.
 * The {@link PrefixTreeHolder} is bound to the {@link ServletContext} using the key {@link #PREFIX_HOLDER_KEY}.
 * @author Phil Zoio
 */
public class UrlPrefixRequestModuleMapper implements RequestModuleMapper, ServletContextAware, InitializingBean, DisposableBean {

    private Log logger = LogFactory.getLog(UrlPrefixRequestModuleMapper.class);
    
    private ServletContext servletContext;
    
    private PrefixTreeHolder prefixTreeHolder;
    
    /**
     * The key used to hold the application-wide {@link PrefixTreeHolder} instance in the {@link ServletContext}
     */
    public static final String PREFIX_HOLDER_KEY = UrlPrefixRequestModuleMapper.class.getName() + ".PREFIX_HOLDER";

    static final String EXISTING_REQUEST_MODULE_MAPPING = UrlPrefixRequestModuleMapper.class.getName() + ".existing_request_module_mapping";
    
    public UrlPrefixRequestModuleMapper() {
        super();
        this.prefixTreeHolder = new PrefixTreeHolder();
    }

    public RequestModuleMapping getModuleForRequest(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        
        if (request instanceof ModuleHttpServletRequest) {
            //deal with forwards or includes
            final RequestModuleMapping requestModuleMapping = (RequestModuleMapping) request.getAttribute(EXISTING_REQUEST_MODULE_MAPPING);
            if (requestModuleMapping != null) {
                if (!useExternalForwardingOrIncludes(request)) {
                    ModuleHttpServletRequest moduleRequest = (ModuleHttpServletRequest) request;
                    moduleRequest.setReuse();
                    return requestModuleMapping;
                }
            }   
        }
        
        final String subpath;
        if (contextPath != null) {
            subpath = requestURI.substring(contextPath.length());
        } else {
            subpath = requestURI;
        }
        
        TreeNode<ModuleNameWithPath> modulePrefixNode = getModuleForURI(subpath);
        
        if (logger.isDebugEnabled()) {
            logger.debug("Module for URI " + requestURI + ": " + modulePrefixNode);
        }
        
        if (modulePrefixNode == null) {
            return null;
        }
        
        ModuleNameWithPath value = modulePrefixNode.getValue();
        final RequestModuleMapping newMapping = new RequestModuleMapping(modulePrefixNode.getKey(), value.getModuleName(), value.getContextPath(), value.getServletPath());
        
        request.setAttribute(EXISTING_REQUEST_MODULE_MAPPING, newMapping);
        return newMapping;
    }

    private boolean useExternalForwardingOrIncludes(HttpServletRequest request) {
        boolean useExternal = false;
        final Object externalRequestOrForward = request.getAttribute(ModuleIntegrationUtils.EXTERNAL_REQUEST_INCLUDES_OR_FORWARDS);
        if (externalRequestOrForward != null && Boolean.parseBoolean(externalRequestOrForward.toString())) {
            useExternal = true;
        }
        return useExternal;
    }

    TreeNode<ModuleNameWithPath> getModuleForURI(String requestURI) {
        TreeNode<ModuleNameWithPath> moduleForURI = prefixTreeHolder.getModuleForURI(requestURI);
        return moduleForURI;
    }

    public void init(ServletConfig servletConfig) {
    }

    public void init(FilterConfig filterConfig) {
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void afterPropertiesSet() throws Exception {
        servletContext.setAttribute(PREFIX_HOLDER_KEY, prefixTreeHolder);
    }

    public void destroy() throws Exception {
        servletContext.removeAttribute(PREFIX_HOLDER_KEY);
    }

    PrefixTreeHolder getPrefixTreeHolder() {
        return prefixTreeHolder;
    }

}
