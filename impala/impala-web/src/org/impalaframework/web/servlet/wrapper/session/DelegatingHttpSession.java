/*
t * Copyright 2007-2010 the original author or authors.
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

package org.impalaframework.web.servlet.wrapper.session;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.springframework.util.Assert;

/**
 * No-op implementation of {@link HttpSession} whose sole job is to pass
 * on method calls to a delegate.
 * 
 * @author Phil Zoio
 */
public class DelegatingHttpSession implements HttpSession {

    private HttpSession realSession;

    public DelegatingHttpSession(HttpSession realSession) {
        super();
        Assert.notNull(realSession, "realSession cannot be null");
        this.realSession = realSession;
    }

    public Object getAttribute(String name) {
        try {
			return realSession.getAttribute(name);
		} catch (IllegalStateException e) {
			return null;
		}
    }

    @SuppressWarnings("unchecked")
    public Enumeration<String> getAttributeNames() {
        try {
			return realSession.getAttributeNames();
		} catch (IllegalStateException e) {
			return null;
		}
    }

    public long getCreationTime() {
        try {
			return realSession.getCreationTime();
		} catch (IllegalStateException e) {
			return 0L;
		}
    }

    public String getId() {
        return realSession.getId();
    }

    public long getLastAccessedTime() {
        try {
            return realSession.getLastAccessedTime();
		} catch (IllegalStateException e) {
			return 0L;
		}
    }

    public int getMaxInactiveInterval() {
        return realSession.getMaxInactiveInterval();
    }

    public ServletContext getServletContext() {
        return realSession.getServletContext();
    }

    @SuppressWarnings("deprecation")
    public javax.servlet.http.HttpSessionContext getSessionContext() {
        return realSession.getSessionContext();
    }

    @SuppressWarnings("deprecation")
    public Object getValue(String name) {
        try {
			return realSession.getValue(name);
		} catch (IllegalStateException e) {
			return null;
		}
    }

    @SuppressWarnings("deprecation")
    public String[] getValueNames() {
        try {
			return realSession.getValueNames();
		} catch (IllegalStateException e) {
			return new String[0];
		}
    }

    public void invalidate() {
        realSession.invalidate();
    }

    public boolean isNew() {
        try {
			return realSession.isNew();
		} catch (IllegalStateException e) {
			return false;
		}
    }

    @SuppressWarnings("deprecation")
    public void putValue(String name, Object value) {
        realSession.putValue(name, value);
    }

    public void removeAttribute(String name) {
        realSession.removeAttribute(name);
    }

    @SuppressWarnings("deprecation")
    public void removeValue(String name) {
        realSession.removeValue(name);
    }

    public void setAttribute(String name, Object value) {
        realSession.setAttribute(name, value);
    }

    public void setMaxInactiveInterval(int interval) {
        realSession.setMaxInactiveInterval(interval);
    }

    protected HttpSession getRealSession() {
        return realSession;
    }

}
