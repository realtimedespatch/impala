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

package org.impalaframework.module.definition;

import static org.impalaframework.classloader.graph.GraphTestUtils.assertModules;
import static org.impalaframework.classloader.graph.GraphTestUtils.newDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.impalaframework.module.ModuleDefinition;

import junit.framework.TestCase;


public class DependencyManagerGraphTest extends TestCase {
    
    private SimpleRootModuleDefinition rootDefinition;
    private DependencyManager manager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void testGetDirectDependees() throws Exception {
        rootDefinition = definitionSet1();
        manager = new DependencyManager(rootDefinition);
        manager.unfreeze();
        Collection<ModuleDefinition> allModules = manager.getAllModules();
        assertModules("root,f,b,c,g,d,h,e", allModules);
        
        assertModules("root,b,c,d,e", manager.getOrderedModuleDependencies("e"));
        assertModules("f,root,g,c,h", manager.getOrderedModuleDependencies("h"));
        
        assertModules("c,h,e", manager.getOrderedModuleDependants("c"));
    }

    private SimpleRootModuleDefinition definitionSet1() {
        List<ModuleDefinition> definitions = new ArrayList<ModuleDefinition>();
        
        //root has no sibling or dependencies
        SimpleRootModuleDefinition root = new SimpleRootModuleDefinition(
                "root", 
                new String[] {"root.xml"}, 
                null, 
                null, 
                null, 
                null, null, true);
        
        //b is child of root
        ModuleDefinition b = newDefinition(definitions, root, "b", null);
        
        //d is a child of b
        ModuleDefinition d = newDefinition(definitions, b, "d", null);
        
        //c is also child of root
        newDefinition(definitions, root, "c", null);
        
        //e is child of d but depends on c
        newDefinition(definitions, d, "e", "c");
        
        //f is a sibling of root
        ModuleDefinition f = newDefinition(definitions, null, "f", null);
        root.addSibling(f);        

        //g is a child of f
        ModuleDefinition g = newDefinition(definitions, f, "g", null);
        
        newDefinition(definitions, g, "h", "c");
        
        return root;
    }
    
}
