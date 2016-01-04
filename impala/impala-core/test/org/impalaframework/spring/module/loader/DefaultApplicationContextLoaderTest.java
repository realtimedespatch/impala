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

package org.impalaframework.spring.module.loader;

import java.io.File;

import junit.framework.TestCase;

import org.impalaframework.classloader.CustomClassLoaderFactory;
import org.impalaframework.exception.NoServiceException;
import org.impalaframework.facade.ModuleManagementFacade;
import org.impalaframework.file.FileMonitor;
import org.impalaframework.module.ModuleDefinition;
import org.impalaframework.module.ModuleDefinitionSource;
import org.impalaframework.module.RootModuleDefinition;
import org.impalaframework.module.definition.ModuleTypes;
import org.impalaframework.module.definition.SimpleModuleDefinition;
import org.impalaframework.module.holder.DefaultModuleStateHolder;
import org.impalaframework.module.loader.ModuleLoaderRegistry;
import org.impalaframework.module.loader.ModuleTestUtils;
import org.impalaframework.module.monitor.ModuleChangeMonitor;
import org.impalaframework.module.monitor.ModuleContentChangeListener;
import org.impalaframework.module.operation.ModuleOperation;
import org.impalaframework.module.operation.ModuleOperationConstants;
import org.impalaframework.module.operation.ModuleOperationInput;
import org.impalaframework.module.source.SimpleModuleDefinitionSource;
import org.impalaframework.module.spi.Application;
import org.impalaframework.module.spi.ApplicationManager;
import org.impalaframework.resolver.StandaloneModuleLocationResolver;
import org.impalaframework.spring.module.ModuleDefinitionPostProcessor;
import org.impalaframework.spring.module.SpringModuleUtils;
import org.impalaframework.spring.service.registry.config.ServiceRegistryPostProcessor;
import org.impalaframework.util.ObjectUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;

/**
 * @author Phil Zoio
 */
public class DefaultApplicationContextLoaderTest extends TestCase {

    private static final String plugin1 = "sample-module1";

    private static final String plugin2 = "sample-module2";

    private static final String plugin3 = "sample-module3";
    
    private static final String rootProjectName = "impala-core";

    private DefaultModuleStateHolder moduleStateHolder;

    private ModuleManagementFacade facade;

    private Application application;

    public void setUp() {   
        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("META-INF/impala-bootstrap.xml");
        Object bean = appContext.getBean("moduleManagementFacade");
        facade = ObjectUtils.cast(bean, ModuleManagementFacade.class);
        
        StandaloneModuleLocationResolver resolver = new StandaloneModuleLocationResolver();

        ModuleLoaderRegistry registry = facade.getModuleLoaderRegistry();
        ApplicationModuleLoader rootModuleLoader = new ApplicationModuleLoader();

        CustomClassLoaderFactory classLoaderFactory = new CustomClassLoaderFactory();
        classLoaderFactory.setModuleLocationResolver(resolver);
        
        registry.addItem(ModuleTypes.ROOT, rootModuleLoader) ;
        ApplicationModuleLoader applicationModuleLoader = new ApplicationModuleLoader();

        registry.addItem(ModuleTypes.APPLICATION, applicationModuleLoader);

        ApplicationManager applicationManager = facade.getApplicationManager();
        application = applicationManager.getCurrentApplication();
        moduleStateHolder = (DefaultModuleStateHolder) application.getModuleStateHolder();
        
        appContext.close();
    }

    public void testResourceBasedValue() {
        ModuleDefinitionSource source = new SimpleModuleDefinitionSource(rootProjectName, new String[] { "parentTestContext.xml" }, new String[] { plugin1, plugin2 });
        ModuleDefinition p2 = source.getModuleDefinition().getChildModuleDefinition(plugin2);
        new SimpleModuleDefinition(p2, plugin3);
        addModule(source);

        ClassLoader originalClassLoader = this.getClass().getClassLoader();
        
        ConfigurableApplicationContext parent = SpringModuleUtils.getRootSpringContext(moduleStateHolder);

        // the implementing FileMonitorBean3 will find the monitor.properties
        // file
        FileMonitor bean3 = (FileMonitor) parent.getBean("bean3");
        assertEquals(333L, bean3.lastModified((File) null));
    
        //module3's class loader is set as the context class loader
        assertEquals(333L, bean3.lastModified(new File("./")));
        
        //now override this
        Thread.currentThread().setContextClassLoader(originalClassLoader);

        //still, using bean class loader, so finds 333L again
        assertEquals(333L, bean3.lastModified(new File("./")));
    }

    public void testLoadUnloadModules() {

        ModuleDefinitionSource source = new SimpleModuleDefinitionSource(rootProjectName, new String[] { "parentTestContext.xml" }, new String[] { plugin1, plugin2 });

        addModule(source);
        ModuleDefinition root = source.getModuleDefinition();

        ConfigurableApplicationContext parent = SpringModuleUtils.getRootSpringContext(moduleStateHolder);
        assertNotNull(parent);
        assertEquals(3, moduleStateHolder.getRuntimeModules().size());

        FileMonitor bean1 = (FileMonitor) parent.getBean("bean1");
        assertEquals(999L, bean1.lastModified((File) null));

        FileMonitor bean2 = (FileMonitor) parent.getBean("bean2");
        assertEquals(100L, bean2.lastModified((File) null));

        // shutdown module and check behaviour has gone
        removeModule(plugin2);

        try {
            bean2.lastModified((File) null);
            fail();
        }
        catch (NoServiceException e) {
        }

        // bean 2 still works
        assertEquals(999L, bean1.lastModified((File) null));

        removeModule(plugin1);

        try {
            bean1.lastModified((File) null);
            fail();
        }
        catch (NoServiceException e) {
        }

        // now reload the module, and see that behaviour returns
        
        addModule(new SimpleModuleDefinition(plugin2));
        bean2 = (FileMonitor) parent.getBean("bean2");
        assertEquals(100L, bean2.lastModified((File) null));

        addModule(new SimpleModuleDefinition(plugin1));
        bean1 = (FileMonitor) parent.getBean("bean1");
        assertEquals(999L, bean1.lastModified((File) null));

        FileMonitor bean3 = (FileMonitor) parent.getBean("bean3");
        try {
            bean3.lastModified((File) null);
            fail();
        }
        catch (NoServiceException e) {
        }

        ModuleDefinition p2 = root.getChildModuleDefinition(plugin2);
        addModule(new SimpleModuleDefinition(p2, plugin3));
        assertEquals(333L, bean3.lastModified((File) null));

        final ConfigurableApplicationContext applicationPlugin3 = SpringModuleUtils.getModuleSpringContext(moduleStateHolder, plugin3);
        applicationPlugin3.close();

        try {
            bean3.lastModified((File) null);
            fail();
        }
        catch (NoServiceException e) {
        }
    }

    public void testLoadAll() {

        ModuleDefinitionSource source = new SimpleModuleDefinitionSource(rootProjectName, new String[] { "parentTestContext.xml" }, new String[] { plugin1, plugin2 });
        final ModuleDefinition p2 = source.getModuleDefinition().getChildModuleDefinition(plugin2);
        new SimpleModuleDefinition(p2, plugin3);

        addModule(source);

        ConfigurableApplicationContext parent = SpringModuleUtils.getRootSpringContext(moduleStateHolder);
        assertNotNull(parent);
        ModuleTestUtils.checkHasPostProcessor(true, parent, ServiceRegistryPostProcessor.class);
        ModuleTestUtils.checkHasPostProcessor(true, parent, ModuleDefinitionPostProcessor.class);

        FileMonitor bean3 = (FileMonitor) parent.getBean("bean3");
        bean3.lastModified((File) null);

        // check that all three modules have loaded
        assertEquals(4, moduleStateHolder.getRuntimeModules().size());
    }

    private void addModule(ModuleDefinitionSource source) {
        RootModuleDefinition moduleDefinition = source.getModuleDefinition();
        addModule(moduleDefinition);
    }

    private void addModule(ModuleDefinition moduleDefinition) {
        ModuleOperation operation = facade.getModuleOperationRegistry().getOperation(ModuleOperationConstants.AddModuleOperation);
        ModuleOperationInput moduleOperationInput = new ModuleOperationInput(null, moduleDefinition, null);
        operation.execute(application, moduleOperationInput);
    }

    private void removeModule(String moduleName) {
        ModuleOperation operation = facade.getModuleOperationRegistry().getOperation(ModuleOperationConstants.RemoveModuleOperation);
        ModuleOperationInput moduleOperationInput = new ModuleOperationInput(null, null, moduleName);
        operation.execute(application, moduleOperationInput).isSuccess();
    }

    class RecordingModuleChangeMonitor implements ModuleChangeMonitor {

        private int started = 0;

        private int stopped = 0;

        public void addModificationListener(ModuleContentChangeListener listener) {
        }

        public void setResourcesToMonitor(String moduleName, Resource[] resources) {
        }

        public void start() {
            started++;
        }

        public void stop() {
            stopped++;
        }

        public int getStarted() {
            return started;
        }

        public int getStopped() {
            return stopped;
        }

    }

}
