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

package org.impalaframework.module.source;

import java.util.Arrays;

import org.impalaframework.module.ModuleDefinition;
import org.impalaframework.module.RootModuleDefinition;
import org.impalaframework.module.definition.ModuleTypes;
import org.impalaframework.module.definition.SimpleModuleDefinition;
import org.impalaframework.module.definition.SimpleRootModuleDefinition;
import org.springframework.core.io.ClassPathResource;

import junit.framework.TestCase;

public class XMLModuleDefinitionSourceTest extends TestCase {
    
    private static final String module1 = "sample-module1";

    private static final String module2 = "sample-module2";

    private static final String module3 = "sample-module3";

    private static final String module4 = "sample-module4";

    private static final String module5 = "sample-module5";

    private static final String module6 = "sample-module6";
    
    private String rootModuleName = "project1";

    private XMLModuleDefinitionSource builder;

    private ModuleDefinition definition1;

    private ModuleDefinition definition2;

    private ModuleDefinition definition3;

    private ModuleDefinition definition4;

    private RootModuleDefinition root;

    private SimpleModuleDefinition definition5;

    private SimpleModuleDefinition definition6;
    
    @Override
    protected void setUp() throws Exception {
        builder = new XMLModuleDefinitionSource();
        root = new SimpleRootModuleDefinition(rootModuleName, new String[] { "parentTestContext.xml", "extra-context.xml" });

        definition1 = new SimpleModuleDefinition(root, module1);
        definition2 = new SimpleModuleDefinition(root, module2);
        definition3 = new SimpleModuleDefinition(definition2, module3);
        definition4 = new SimpleModuleDefinition(root, module4);
        definition5 = new SimpleModuleDefinition(null, module5);
        definition6 = new SimpleModuleDefinition(definition5, module6, ModuleTypes.APPLICATION, null, new String[] {"sample-module3","sample-module4","sample-module5"}, null, null, null, null, true);
        
    }
    
    public final void testGetParentOnlyDefinition() {
        builder.setResource(new ClassPathResource("xmlspec/parent-only-spec.xml"));
        RootModuleDefinition actual = builder.getModuleDefinition();
        assertEquals(0, actual.getChildModuleDefinitions().size());

        RootModuleDefinition expected = new SimpleRootModuleDefinition(rootModuleName, new String[] { "parentTestContext.xml", "extra-context.xml" });
        assertEquals(expected, actual);
    }
    
    public final void testGetParentDefinition() {
        builder.setResource(new ClassPathResource("xmlspec/moduledefinition.xml"));
        RootModuleDefinition actual = builder.getModuleDefinition();
        assertEquals(3, actual.getChildModuleDefinitions().size());

        assertEquals(root, actual);
        
        assertEquals(definition1, actual.findChildDefinition(module1, true));
        assertEquals(definition2, actual.findChildDefinition(module2, true));
        assertEquals(definition3, actual.findChildDefinition(module3, true));
        assertEquals(definition4, actual.findChildDefinition(module4, true));
    }
    
    public void testGetGraphDefinition() throws Exception {
        builder.setResource(new ClassPathResource("xmlspec/graphdefinition.xml"));
        RootModuleDefinition actual = builder.getModuleDefinition();
        assertEquals("project1", actual.getName());
        assertEquals(1, actual.getChildModuleDefinitions().size());
        assertEquals(1, actual.getSiblings().size());
        
        assertEquals(definition5, actual.findChildDefinition(module5, true));
        assertEquals(definition6, actual.findChildDefinition(module6, true));
        assertEquals(definition5, actual.findChildDefinition(module6, true).getParentDefinition());
        
        assertEquals(Arrays.asList("sample-module3,sample-module4,sample-module5".split(",")), definition6.getDependentModuleNames(false));

    }

}
