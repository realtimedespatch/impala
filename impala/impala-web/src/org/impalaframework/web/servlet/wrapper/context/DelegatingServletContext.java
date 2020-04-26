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

package org.impalaframework.web.servlet.wrapper.context;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.impalaframework.util.ReflectionUtils;
import org.springframework.util.Assert;

/**
 * An implementation of {@link ServletContext} which simply delegates to
 * the underlying {@link ServletContext} in it. Subclasses can override
 * specific methods.
 * 
 * @author Phil Zoio
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class DelegatingServletContext implements ServletContext {

    private static final Log logger = LogFactory.getLog(DelegatingServletContext.class);
    
    private ServletContext realContext;

    public DelegatingServletContext(ServletContext realContext) {
        super();
        Assert.notNull(realContext, "realContext cannot be null");
        this.realContext = realContext;
    }

    public ServletContext getContext(String uriPath) {
        return realContext.getContext(uriPath);
    }
    
    public String getContextPath() {
        //attempt to invoke by reflection as this is new in the Servlet 2.5 API
        return (String) ReflectionUtils.invokeMethod(realContext, "getContextPath", new Object[0]);
    }


    public String getInitParameter(String name) {
        return realContext.getInitParameter(name);
    }

    public Enumeration<String> getInitParameterNames() {
        return realContext.getInitParameterNames();
    }

    public int getMajorVersion() {
        return realContext.getMajorVersion();
    }

    public String getMimeType(String file) {
        return realContext.getMimeType(file);
    }

    public int getMinorVersion() {
        return realContext.getMinorVersion();
    }

    public RequestDispatcher getNamedDispatcher(String name) {
        return realContext.getNamedDispatcher(name);
    }

    public String getRealPath(String path) {
        return realContext.getRealPath(path);
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        return realContext.getRequestDispatcher(path);
    }

    public URL getResource(String path) throws MalformedURLException {
        return realContext.getResource(path);
    }

    public InputStream getResourceAsStream(String path) {
        return realContext.getResourceAsStream(path);
    }

    public Set getResourcePaths(String path) {
        return realContext.getResourcePaths(path);
    }

    public String getServerInfo() {
        return realContext.getServerInfo();
    }

    @SuppressWarnings("deprecation")
    public Servlet getServlet(String name) throws ServletException {
        return realContext.getServlet(name);
    }

    public String getServletContextName() {
        return realContext.getServletContextName();
    }

    @SuppressWarnings("deprecation")
    public Enumeration getServletNames() {
        return realContext.getServletNames();
    }

    @SuppressWarnings("deprecation")
    public Enumeration getServlets() {
        return realContext.getServlets();
    }

    public void log(String message) {
        realContext.log(message);
    }

    @SuppressWarnings("deprecation")
    public void log(Exception exception, String message) {
        realContext.log(exception, message);
    }

    public void log(String message, Throwable throwable) {
        realContext.log(message, throwable);
    }

    public Enumeration<String> getAttributeNames() {
        return realContext.getAttributeNames();
    }
    
    public Object getAttribute(String name) {
        Object value = realContext.getAttribute(name);
        if (logger.isTraceEnabled()) {
            logger.trace("Getting attribute for name: " + name + " - " + value);
        }
        return value;
    }

    public void removeAttribute(String name) {
        if (logger.isTraceEnabled()) {
            logger.trace("Removing attribute: " + name);
        }
        realContext.removeAttribute(name);
    }

    public void setAttribute(String name, Object value) {
        if (logger.isTraceEnabled()) {
            logger.trace("Setting attribute for name: " + name + " - " + value);
        }
        realContext.setAttribute(name, value);
    }

    public ServletContext getRealContext() {
        return realContext;
    }
    
}
