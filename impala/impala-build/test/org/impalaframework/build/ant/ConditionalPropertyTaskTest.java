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

package org.impalaframework.build.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Sequential;

import junit.framework.TestCase;

public class ConditionalPropertyTaskTest extends TestCase {

    private IfPropertyTask task;
    private Project project;
    private Sequential sequential;
    private DummyTask dummy;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        task = new IfPropertyTask();
        project = new Project();
        task.setProject(project);
        sequential = new Sequential();
        sequential.setProject(project);
        dummy = new DummyTask();
        dummy.setProject(project);
        sequential.addTask(dummy);
    }

    public void testBuildExceptons() {
        try {
            task.execute();
            fail();
        } catch (BuildException e) {
            assertEquals("Property 'property' has not been specified.", e.getMessage());
        }

        task.setProperty("myproperty");
        task.addTask(dummy);
        task.execute();
        assertFalse(dummy.isExecuted());
    }
    
    public void testPropertyPresent() throws Exception {
        task.setProperty("myproperty");
        task.addTask(dummy);
        project.setProperty("myproperty", "anything");

        task.execute();
        assertTrue(dummy.isExecuted());
    }

    public void testDoExecute() throws Exception {
        IfPropertyTask ifTask = new IfPropertyTask();
        assertTrue(ifTask.shouldExecute(true));
        assertFalse(ifTask.shouldExecute(false));
        
        UnlessPropertyTask unlessTask = new UnlessPropertyTask();
        assertTrue(unlessTask.shouldExecute(false));
        assertFalse(unlessTask.shouldExecute(true));
    }
    
    public class DummyTask extends Task {

        private boolean executed;

        @Override
        public void execute() throws BuildException {
            this.executed = true;
        }

        public boolean isExecuted() {
            return executed;
        }

    }

}
