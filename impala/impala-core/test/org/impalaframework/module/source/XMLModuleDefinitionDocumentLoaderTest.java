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

import org.impalaframework.exception.ConfigurationException;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;

import junit.framework.TestCase;

public class XMLModuleDefinitionDocumentLoaderTest extends TestCase {

    public final void testLoadDocument() {
        XMLModulelDefinitionDocumentLoader builder = new XMLModulelDefinitionDocumentLoader();
        Document document = builder.loadDocument(new ClassPathResource("xmlspec/moduledefinition.xml"));
        assertNotNull(document);
    }
    
    public final void testNotPresentLoadDocument() {
        XMLModulelDefinitionDocumentLoader builder = new XMLModulelDefinitionDocumentLoader();
        try {
            builder.loadDocument(new ClassPathResource("xmlspec/notpresent.xml"));
            fail();
        }
        catch (ConfigurationException e) {
            assertEquals("Unable to load XML module definition document from resource class path resource [xmlspec/notpresent.xml]", e.getMessage());
        }
    }
    
    public final void testBadlyFormedLoadDocument() {
        XMLModulelDefinitionDocumentLoader builder = new XMLModulelDefinitionDocumentLoader();
        try {
            builder.loadDocument(new ClassPathResource("xmlspec/badlyformedspec.xml"));
            fail();
        }
        catch (ConfigurationException e) {
            assertEquals("Unable to load XML module definition document from resource class path resource [xmlspec/badlyformedspec.xml]", e.getMessage());
        }
    }

}
