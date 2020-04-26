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

package org.impalaframework.web.facade;

import java.util.List;

import org.impalaframework.startup.ContextStarter;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class WebContextStarter implements ContextStarter {

    public ApplicationContext startContext(List<String> locations) {
        
        Assert.notNull(locations);
        
        final String[] locationArray = locations.toArray(new String[locations.size()]);
        for (int i = 0; i < locationArray.length; i++) {
            locationArray[i] = "classpath:" + locationArray[i];
        }
        
        final XmlWebApplicationContext context = new XmlWebApplicationContext(){
            @Override
            public String[] getConfigLocations() {
                return locationArray;
            }
        };
        context.setServletContext(new AttributeServletContext());
        context.refresh();
        return context;
    }
    
}
