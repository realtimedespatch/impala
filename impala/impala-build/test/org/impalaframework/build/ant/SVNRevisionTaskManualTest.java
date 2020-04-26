package org.impalaframework.build.ant;

import org.apache.tools.ant.Project;

import junit.framework.TestCase;

public class SVNRevisionTaskManualTest extends TestCase {

    public void testExecute() {
        SVNRevisionTask task = new SVNRevisionTask();
        task.setUrl("http://impala.googlecode.com/svn");

        Project project = new Project();
        task.setProject(project);
        
        task.execute();
        System.out.println(project.getProperty(SVNRevisionTask.DEFAULT_REVISION_PROPERTY));
    }
    
    public void testExecuteWithPassword() {
        SVNRevisionTask task = new SVNRevisionTask();
        task.setUser("philzoio@realsolve.co.uk");
        task.setPassword("t2k3j7e8");
        task.setUrl("https://impala.googlecode.com/svn");

        Project project = new Project();
        task.setProject(project);
        
        task.execute();
        System.out.println(project.getProperty(SVNRevisionTask.DEFAULT_REVISION_PROPERTY));
    }

}
