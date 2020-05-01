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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.impalaframework.exception.InvalidStateException;
import org.impalaframework.radixtree.ConcurrentRadixTree;
import org.impalaframework.radixtree.TreeNode;
import org.springframework.util.Assert;

/**
 * Holds a {@link ConcurrentRadixTree}, used to resolve URL prefixes to modules.
 * Also holds a mapping of modules to contributed prefix keys, so that if module is unloaded, 
 * relevant keys can be removed.
 * @author Phil Zoio
 */
public class PrefixTreeHolder {
    
    private ConcurrentRadixTree<ModuleNameWithPath> trie = new ConcurrentRadixTree<ModuleNameWithPath>();
    
    private Map<String,List<String>> contributions = new ConcurrentHashMap<String, List<String>>();
    
    /**
     * Used to add prefix key to module name mapping. This will be called zero to n times 
     * for each module which is interested in receiving URLs
     */
    public void add(String moduleName, String key, String contextPath, String servletPath) {
        //FIXME add concurrency control
        
        Assert.notNull(moduleName, "moduleName cannot be null");
        Assert.notNull(key, "key cannot be null");
        
        if (this.trie.contains(key)) {
            ModuleNameWithPath value = trie.findContainedValue(key);
            throw new InvalidStateException("Module '" + moduleName + "' cannot use key '" + key + "', as it is already being used by module '" + value.getModuleName() + "'");
        }

        this.trie.insert(key, new ModuleNameWithPath(moduleName, contextPath, servletPath));
        List<String> list = this.contributions.get(moduleName);
        if (list == null) {
            list = new LinkedList<String>();
            this.contributions.put(moduleName, list);
        }
        
        list.add(key);
    }

    public boolean remove(String moduleName, String prefix) {
        List<String> list = contributions.get(moduleName);
        if (list != null && list.contains(prefix)) {
            list.remove(prefix);
            trie.delete(prefix);
            
            if (list.isEmpty()) {
                contributions.remove(moduleName);
            }
            return true;
        }
        return false;
    }
    
    /**
     * Called when module is unloaded to remove all the prefix keys to module name associations
     * contributed by the module concerned
     */
    public int unloadForModule(String moduleName) {
        
        int unloaded = 0;
        
        List<String> list = contributions.get(moduleName);
        if (list != null) {
            for (String key : list) {
                trie.delete(key);
                unloaded++;
            }
            contributions.remove(moduleName);
        }
        
        return unloaded;
    }

    public TreeNode<ModuleNameWithPath> getModuleForURI(String requestURI) {
        return trie.findContainedNode(requestURI);
    }

    public Map<String, List<String>> getContributions() {
        return Collections.unmodifiableMap(contributions);
    }

    ConcurrentRadixTree<ModuleNameWithPath> getTrie() {
        return trie;
    }
    
}
