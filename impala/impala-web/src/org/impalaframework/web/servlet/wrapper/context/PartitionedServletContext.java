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

import java.util.Enumeration;

import javax.servlet.ServletContext;

import org.impalaframework.web.servlet.qualifier.WebAttributeQualifier;
import org.springframework.util.Assert;

/**
 * Extension of {@link BaseWrapperServletContext}. Basically used to
 * partition the servlet context so that only writes to the
 * {@link ServletContext} attributes using module-based prefix, as determined by
 * the {@link WebAttributeQualifier} implementation.
 * 
 * However, it does allow you to read shared attributes, that is, those without
 * the module-based prefix.
 * 
 * @author Phil Zoio
 */
public class PartitionedServletContext extends
        BaseWrapperServletContext {

    public PartitionedServletContext(
            ServletContext realContext,
            String applicationId, 
            String moduleName, 
            WebAttributeQualifier webAttributeQualifier, 
            ClassLoader moduleClassLoader) {
        super(realContext, applicationId, moduleName, webAttributeQualifier, moduleClassLoader);
    }
    
    /**
     * Retrieves only the attributes which have the module specific prefix
     * for this module. Note, however, that {@link #getAttribute(String)}
     * will also get shared values, but it cannot set or remove them, 
     * or iterate through them.
     */
    public Enumeration<String> getAttributeNames() {

        Enumeration<String> attributeNames = super.getAttributeNames();
        
        final String applicationId = getApplicationId();
        final String moduleName = getModuleName();
        return getWebAttributeQualifier().filterAttributeNames(attributeNames, applicationId, moduleName);
    }

    /**
     * Determines which key is used for writing values to the servlet context.
     * 
     */
    protected String getWriteKeyToUse(String name) {

        Assert.notNull(name, "name cannot be null");
        return getWebAttributeQualifier().getQualifiedAttributeName(name, getApplicationId(), getModuleName());
    }
    
}
