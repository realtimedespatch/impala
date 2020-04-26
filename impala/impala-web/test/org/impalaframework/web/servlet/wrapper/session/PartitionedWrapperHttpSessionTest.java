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

package org.impalaframework.web.servlet.wrapper.session;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.util.Properties;

import javax.servlet.http.HttpSession;

import org.easymock.EasyMock;
import org.impalaframework.classloader.ModuleClassLoader;
import org.impalaframework.config.PropertySource;
import org.impalaframework.config.PropertySourceHolder;
import org.impalaframework.config.StaticPropertiesPropertySource;
import org.impalaframework.util.InstantiationUtils;
import org.impalaframework.util.serialize.ClassLoaderAwareSerializationStreamFactory;
import org.impalaframework.util.serialize.SerializationHelper;
import org.impalaframework.web.bootstrap.WebBootstrapProperties;
import org.impalaframework.web.servlet.qualifier.DefaultWebAttributeQualifier;
import org.impalaframework.web.servlet.qualifier.IdentityWebAttributeQualifier;
import org.impalaframework.web.servlet.qualifier.WebAttributeQualifier;
import org.impalaframework.web.servlet.wrapper.SerializableValueHolder;
import org.impalaframework.web.servlet.wrapper.ValueHolder;
import org.springframework.util.ClassUtils;

import junit.framework.TestCase;

public class PartitionedWrapperHttpSessionTest extends TestCase {

    private HttpSession session;
    private String applicationId;
    private String moduleName;
    private WebAttributeQualifier webAttributeQualifier;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        applicationId = "myapp";
        moduleName = "mymodule";
        webAttributeQualifier = new IdentityWebAttributeQualifier();
    }

    public void testGetAttributeNoWrapping() {
        //test the case where the classloader is compatible
        
        session = createMock(HttpSession.class);
        ValueHolder valueHolder = new ValueHolder();
        expect(session.getAttribute("myAttribute")).andReturn(valueHolder);
        
        replay(session);

        StateProtectingHttpSession wrappedSession = new StateProtectingHttpSession(session, webAttributeQualifier, applicationId, moduleName, ClassUtils.getDefaultClassLoader());
        assertSame(valueHolder, wrappedSession.getAttribute("myAttribute"));
        verify(session);
    }
    
    public void testInvalidate() {
        
        session = createMock(HttpSession.class);
        session.invalidate();
        
        replay(session);

        StateProtectingHttpSession wrappedSession = new StateProtectingHttpSession(session, webAttributeQualifier, applicationId, moduleName, ClassUtils.getDefaultClassLoader());

        assertTrue(wrappedSession.isValid());
        wrappedSession.invalidate();
        assertFalse(wrappedSession.isValid());
        
        verify(session);
    }
    
    public void testGetQualifiedAttributeName() {
        //test the case where the classloader is compatible
        
        webAttributeQualifier = new DefaultWebAttributeQualifier();
        
        session = createMock(HttpSession.class);
        ValueHolder valueHolder = new ValueHolder();
        expect(session.getAttribute("application_myapp_module_mymodule:myAttribute")).andReturn(valueHolder);
        
        replay(session);

        StateProtectingHttpSession wrappedSession = new StateProtectingHttpSession(session, webAttributeQualifier, applicationId, moduleName, ClassUtils.getDefaultClassLoader());
        assertSame(valueHolder, wrappedSession.getAttribute("myAttribute"));
        verify(session);
    }    
    
    public void testGetAttributeWrappingSerializableValueHolder() {
        //test the case where the classloader is compatible
        
        session = createMock(HttpSession.class);
        SerializableValueHolder valueHolder = new SerializableValueHolder();
        SerializationHelper helper = new SerializationHelper(new ClassLoaderAwareSerializationStreamFactory(newModuleClassLoader()));
        
        Object clonedObject = helper.clone(valueHolder);
        expect(session.getAttribute("myAttribute")).andReturn(clonedObject);
        session.setAttribute(eq("myAttribute"), EasyMock.anyObject());
        
        replay(session);

        StateProtectingHttpSession wrappedSession = new StateProtectingHttpSession(session, webAttributeQualifier, applicationId, moduleName, newModuleClassLoader());
        assertFalse(valueHolder == wrappedSession.getAttribute("myAttribute"));
        verify(session);
    }
    
    public void testGetAttributeWrappingWithFailedSerialization() {
        //test the case where the classloader is compatible
        
        session = createMock(HttpSession.class);
        SerializableValueHolder valueHolder = new SerializableValueHolder();
        SerializationHelper helper = new SerializationHelper(new ClassLoaderAwareSerializationStreamFactory(newModuleClassLoader()));
        
        Object clonedObject = helper.clone(valueHolder);
        expect(session.getAttribute("myAttribute")).andReturn(clonedObject);
        session.removeAttribute("myAttribute");
        
        replay(session);

        StateProtectingHttpSession wrappedSession = cloneFailingSession();
        assertFalse(valueHolder == wrappedSession.getAttribute("myAttribute"));
        verify(session);
    }
    
    
    public void testGetAttributeWrappingWithSessionNoPreserved() {
        Properties properties = new Properties();
        properties.setProperty(WebBootstrapProperties.PRESERVE_SESSION_ON_RELOAD_FAILURE, "false");
        PropertySource source = new StaticPropertiesPropertySource(properties);
        
        PropertySourceHolder.getInstance().setPropertySource(source);
        
        session = createMock(HttpSession.class);
        SerializableValueHolder valueHolder = new SerializableValueHolder();
        SerializationHelper helper = new SerializationHelper(new ClassLoaderAwareSerializationStreamFactory(newModuleClassLoader()));
        
        Object clonedObject = helper.clone(valueHolder);
        expect(session.getAttribute("myAttribute")).andReturn(clonedObject);
        
        //note the call to invalidate
        session.invalidate();
        
        replay(session);

        StateProtectingHttpSession wrappedSession = cloneFailingSession();
        assertFalse(valueHolder == wrappedSession.getAttribute("myAttribute"));
        verify(session);
    }
    
    
    private StateProtectingHttpSession cloneFailingSession() {
        StateProtectingHttpSession wrappedSession = new StateProtectingHttpSession(session, webAttributeQualifier, applicationId, moduleName, newModuleClassLoader()){

            @Override
            Object clone(Object attribute, SerializationHelper helper) {
                throw new RuntimeException();
            }
            
        };
        return wrappedSession;
    }

    public void testGetAttributeWrappingValueHolder() {
        //test the case where the classloader is incompatible
        //in this case the object is removed from the session and getAttribute returns null
        
        session = createMock(HttpSession.class);
    
        Object attribute = InstantiationUtils.instantiate(ValueHolder.class.getName(), newModuleClassLoader());
        expect(session.getAttribute("myAttribute")).andReturn(attribute);
        session.removeAttribute("myAttribute");
        
        replay(session);

        StateProtectingHttpSession wrappedSession = new StateProtectingHttpSession(session, webAttributeQualifier, applicationId, moduleName, newModuleClassLoader());
        assertNull(wrappedSession.getAttribute("myAttribute"));
        verify(session);
    }


    private ModuleClassLoader newModuleClassLoader() {
        ModuleClassLoader moduleClassLoader = new ModuleClassLoader(new File[]{ new File("../impala-web/bin"), new File("../impala-web/testbin")});
        return moduleClassLoader;
    }

}
