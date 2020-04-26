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

package org.impalaframework.web.spring.module;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.impalaframework.module.ModuleDefinition;
import org.impalaframework.module.definition.ModuleDefinitionWalker;
import org.impalaframework.module.definition.ToStringCallback;
import org.impalaframework.web.module.WebModuleTypes;
import org.springframework.util.Assert;

public class WebPlaceholderModuleDefinition implements ModuleDefinition {

    private static final long serialVersionUID = 1L;

    private ModuleDefinition parent;

    private String state;

    private String name;

    private boolean frozen;
    
    private boolean reloadable;
    
    /* ********************* constructor ******************** */

    public WebPlaceholderModuleDefinition(ModuleDefinition parent, String name) {
        Assert.notNull(parent, "parent cannot be null");
        Assert.notNull(name, "name cannot be null");
        this.parent = parent;
        this.name = name;
        this.parent.addChildModuleDefinition(this);
        this.reloadable = true;
    }

    /* ********************* read-only methods ******************** */
    
    public ModuleDefinition findChildDefinition(String moduleName, boolean exactMatch) {
        return null;
    }

    public List<String> getConfigLocations() {
        return Collections.emptyList();
    }

    public String getName() {
        return name;
    }

    public String getRuntimeFramework() {
        return "spring";
    }
    
    public String getType() {
        return WebModuleTypes.WEB_PLACEHOLDER;
    }

    public ModuleDefinition getParentDefinition() {
        return this.parent;
    }

    public ModuleDefinition getChildModuleDefinition(String moduleName) {
        return null;
    }

    public Collection<String> getChildModuleNames() {
        return Collections.emptyList();
    }

    public Collection<ModuleDefinition> getChildModuleDefinitions() {
        return Collections.emptyList();
    }

    public boolean hasChildModuleDefinition(String moduleName) {
        return false;
    }
    
    public String getState() {
        return state;
    }
    
    public List<String> getDependentModuleNames(boolean optional) {
        return Collections.emptyList();
    }
    
    public Collection<String> getCapabilities() {
        return Collections.emptyList();
    }
    
    public boolean isFrozen() {
        return this.frozen;
    }
    
    public boolean isReloadable() {
    	return reloadable;
    }
    
    @Override
    public void setNonReloadable() {
    	this.reloadable = false;
    }

    /* ********************* mutation methods ******************** */

    public void addChildModuleDefinition(ModuleDefinition moduleDefinition) {
        throw new UnsupportedOperationException("Cannot add module '" + moduleDefinition.getName()
                + "' to web placeholder module definitionSource '" + this.getName() + "', as this cannot contain other modules");
    }

    public void setState(String state) {
        this.state = state;
    }

    public ModuleDefinition removeChildModuleDefinition(String moduleName) {
        return null;
    }

    public Map<String, String> getAttributes() {
        return null;
    }

    public void setParentDefinition(ModuleDefinition parent) {
        this.parent = parent;
    }

    public void unfreeze() {
        this.frozen = false;
    }

    public void freeze() {
        this.frozen = true;
    }

    /* ********************* object override methods ******************** */

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((name == null) ? 0 : name.hashCode());
        result = PRIME * result + ((parent == null) ? 0 : parent.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final WebPlaceholderModuleDefinition other = (WebPlaceholderModuleDefinition) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        }
        else if (!name.equals(other.name))
            return false;
        if (parent == null) {
            if (other.parent != null)
                return false;
        }
        else if (!parent.equals(other.parent))
            return false;
        return true;
    }

    @Override
    public String toString() {
        ToStringCallback callback = new ToStringCallback();
        ModuleDefinitionWalker.walkModuleDefinition(this, callback);
        return callback.toString();
    }

    public void toString(StringBuffer buffer) {
        buffer.append("name=" + name);
        buffer.append(", type=" + getType());
    }
}
