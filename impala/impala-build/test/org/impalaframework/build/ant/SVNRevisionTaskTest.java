package org.impalaframework.build.ant;

import junit.framework.TestCase;

public class SVNRevisionTaskTest extends TestCase {
    
    public void testExtract() throws Exception {
        SVNRevisionTask task = new SVNRevisionTask();
        assertEquals("2160", task.extractRevision("html><head><title>Revision 2160: /</title></head>\n<h2>Revision 2160: /</h2>\n</html>"));
    }
    
}
