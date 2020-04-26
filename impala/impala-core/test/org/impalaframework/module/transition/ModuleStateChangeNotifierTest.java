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

package org.impalaframework.module.transition;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.List;

import org.impalaframework.module.definition.SimpleModuleDefinition;
import org.impalaframework.module.spi.ModuleStateChange;
import org.impalaframework.module.spi.ModuleStateChangeListener;
import org.impalaframework.module.spi.ModuleStateHolder;
import org.impalaframework.module.spi.Transition;
import org.impalaframework.module.spi.TransitionResult;

import junit.framework.TestCase;

public class ModuleStateChangeNotifierTest extends TestCase {

    private DefaultModuleStateChangeNotifier notifier;

    private ModuleStateChange change;

    private ModuleStateChangeListener listener1;

    private ModuleStateChangeListener listener2;

    private ModuleStateChangeListener listener3;

    private ModuleStateHolder moduleStateHolder;

    private ModuleStateChangeListener listener4;

    private ModuleStateChangeListener listener5;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        notifier = new DefaultModuleStateChangeNotifier();
        change = new ModuleStateChange(Transition.UNLOADED_TO_LOADED, new SimpleModuleDefinition("myModule"));
        List<ModuleStateChangeListener> listeners = new ArrayList<ModuleStateChangeListener>();
        listener1 = createMock(ModuleStateChangeListener.class);
        listener2 = createMock(ModuleStateChangeListener.class);
        listener3 = createMock(ModuleStateChangeListener.class);
        listener4 = createMock(ModuleStateChangeListener.class);
        listener5 = createMock(ModuleStateChangeListener.class);
        moduleStateHolder = createMock(ModuleStateHolder.class);
        listeners.add(listener1);
        listeners.add(listener2);
        listeners.add(listener3);
        listeners.add(listener4);
        listeners.add(listener5);
        notifier.setListeners(listeners);
    }

    public final void testNotify1() {
        notifier = new DefaultModuleStateChangeNotifier();
        assertNotNull(notifier.getListeners());
        assertEquals(0, notifier.getListeners().size());

        ModuleStateChangeListener listener1 = createMock(ModuleStateChangeListener.class);
        notifier.addListener(listener1);
        assertEquals(1, notifier.getListeners().size());
        
        assertTrue(notifier.removeListener(listener1));
        assertEquals(0, notifier.getListeners().size());
        
        //the second time returns false, as this listener has been removed
        assertFalse(notifier.removeListener(listener1));
        assertEquals(0, notifier.getListeners().size());
        
        //add the first one back again
        notifier.addListener(listener1);
        assertEquals(1, notifier.getListeners().size());

        List<ModuleStateChangeListener> listeners = new ArrayList<ModuleStateChangeListener>();
        listeners.add(createMock(ModuleStateChangeListener.class));
        listeners.add(createMock(ModuleStateChangeListener.class));
        notifier.setListeners(listeners);
        assertEquals(2, notifier.getListeners().size());
    }

    public void testNotify() throws Exception {

        TransitionResult transitionResult = new TransitionResult(change);
        
        //listener1 returns the same module name as current name, so is notified
        expect(listener1.getModuleName()).andReturn("myModule");
        expect(listener1.getTransition()).andReturn(null);
        listener1.moduleStateChanged(moduleStateHolder, transitionResult);

        //listener2 returns null for module name, so is notified
        expect(listener2.getModuleName()).andReturn(null);
        expect(listener2.getTransition()).andReturn(null);
        listener2.moduleStateChanged(moduleStateHolder, transitionResult);

        //listener3 returns a different module name as current name, so is not notified
        expect(listener3.getModuleName()).andReturn("anotherModule");
        
        //listener4 returns name of matching transition
        expect(listener4.getModuleName()).andReturn(null);
        expect(listener4.getTransition()).andReturn(Transition.UNLOADED_TO_LOADED);
        listener4.moduleStateChanged(moduleStateHolder, transitionResult);
        
        //listener5 returns name of non-matching transition
        expect(listener5.getModuleName()).andReturn(null);
        expect(listener5.getTransition()).andReturn(Transition.LOADED_TO_UNLOADED);


        replay(listener1);
        replay(listener2);
        replay(listener3);
        replay(listener4);
        replay(listener5);
        replay(moduleStateHolder);
        notifier.notify(moduleStateHolder, transitionResult);

        verify(listener1);
        verify(listener2);
        verify(listener3);
        verify(listener4);
        verify(listener5);
        verify(moduleStateHolder);
    }

}
