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

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

/**
 * @author Phil Zoio
 */
public class GetTaskResultTest extends TestCase {

    public void testResultConstructor() throws MalformedURLException {
        construct(false, 0, null, "");
        construct(false, 1, null, "");
        construct(false, 2, new URL("http://location"), "");

        construct(true, -1, null, "result must be between 0 and 3 (inclusive)");
        construct(true, 4, null, "result must be between 0 and 3 (inclusive)");
        construct(true, 3, null, "success location required for successful result");
    }

    public void testToString() throws MalformedURLException {
        toString(0, null, "archive not modified");
        toString(1, null, "archive could not be downloaded from any location");
        toString(2, new URL("http://location"), "archive COPIED from \nhttp://location");
        toString(3, new URL("http://location"), "archive DOWNLOADED from \nhttp://location");
    }

    private void construct(boolean fail, int result, URL successLocation, String expected) {
        try {
            new Result("archive", result, successLocation);
            if (fail)
                fail("Success not expected");
        }
        catch (IllegalArgumentException e) {
            if (fail)
                assertEquals(expected, e.getMessage());
            else
                fail("Failure not expected");
        }
    }

    private void toString(int result, URL successLocation, String expected) {
        assertEquals(expected, new Result("archive", result, successLocation).toString());
    }
}
