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

package org.impalaframework.classloader;

import java.io.File;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base implementation of <code>URLClassLoader</code> which will attempt to
 * load from the named class locations BEFORE attempting to load using the
 * parent class location.
 * 
 * @see BaseURLClassLoader
 * @author Phil Zoio
 */
public abstract class CustomClassLoader extends BaseURLClassLoader implements ModularClassLoader {

    private static final Log logger = LogFactory.getLog(CustomClassLoader.class);

    public CustomClassLoader(File[] locations) {
        super(locations);
    }

    public CustomClassLoader(ClassLoader parent, File[] locations) {
        super(parent, locations);
    }
    
    public CustomClassLoader(URL[] locations) {
        super(locations);
    }

    public CustomClassLoader(ClassLoader parent, URL[] locations) {
        super(parent, locations);
    }
    
    protected abstract boolean loadCustomClassFirst();
    
    /**
     * Calls {@link #loadClass(String, boolean)} with resolve set to false
     */
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }
    
    /**
     * Classes are always considered to be reloadable
     */
    @SuppressWarnings("rawtypes") 
    public boolean isClassReloadable(Class clazz) {
        return true;
    }
    
    /**
     * Attempts to load the class by calling the following superclass methods,
     * in order:
     * <ul>
     * <li><code>getAlreadyLoadedClass()</code>, to return a cached class.</li>
     * <li><code>loadCustomClass()</code>, to attempt to load the class from
     * a custom location.</li>
     * <li><code>loadParentClass()</code></li>
     * </ul>
     * @param name of class to load
     * @exception the <code>ClassNotFoundException</code> if the class could
     * not be loaded
     */
    public Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {

        Class<?> toReturn = null;
        if (toReturn == null) {
            toReturn = getAlreadyLoadedClass(className);
        }
        
        if (loadCustomClassFirst()) {   
            if (toReturn == null) {
                toReturn = loadCustomClass(className);
            }
            if (toReturn == null) {
                toReturn = loadParentClass(className);
            }
        } else {
            if (toReturn == null) {
                toReturn = loadParentClass(className);
            }   
            if (toReturn == null) {
                toReturn = loadCustomClass(className);
            }
        }
        
        if (toReturn == null) {
            if (logger.isDebugEnabled())
                logger.debug("Class not found: " + className);
            throw new ClassNotFoundException(className);
        } else {
            if (resolve) {
                resolveClass(toReturn);
            }
        }
        return toReturn;
    }

    public boolean hasVisibilityOf(ClassLoader classLoader) {
        
        final ClassLoader parent = classLoader;
        
        @SuppressWarnings("resource")
		ClassLoader child = this;
        
        if (parent == child) return true;
        
        ClassLoader parentOfChild = null;
        while ((parentOfChild = child.getParent()) != null) {
            if (parent == parentOfChild) {
                return true;
            }
            child = parentOfChild;
        }
        return false;
    }

}
