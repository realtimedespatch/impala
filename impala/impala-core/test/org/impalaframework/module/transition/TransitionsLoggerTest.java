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

import static org.impalaframework.module.spi.TransitionResultSetTest.newFailedTransitionResultSet;
import static org.impalaframework.module.spi.TransitionResultSetTest.newSuccessTransitionResultSet;

import org.impalaframework.module.spi.TransitionResultSet;

import junit.framework.TestCase;

public class TransitionsLoggerTest extends TestCase {

    private TransitionsLogger logger;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        logger = new TransitionsLogger();
    }
    
    public void testSuccess() {
        TransitionResultSet resultSet = newSuccessTransitionResultSet();
        System.out.println(logger.getTransitionString(resultSet, true));
    }
    
    public void testFail() {
        TransitionResultSet resultSet = newFailedTransitionResultSet();
        System.out.println(logger.getTransitionString(resultSet, true));
    }
}
