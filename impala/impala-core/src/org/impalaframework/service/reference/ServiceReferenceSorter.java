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

package org.impalaframework.service.reference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.impalaframework.service.ServiceRegistryEntry;
import org.springframework.util.Assert;

/**
 * Sorts {@link ServiceRegistryEntry} instance using the attribute 'service.ranking'. 
 * @author Phil Zoio
 */
public class ServiceReferenceSorter {
    
    private Comparator<ServiceRegistryEntry> comparator = new ServiceReferenceComparator();
    
    /**
     * Sorts and returns new list based on original {@link Collection}
     */
    public List<ServiceRegistryEntry> sort(Collection<ServiceRegistryEntry> entries) {
        return sort(new ArrayList<ServiceRegistryEntry>(entries), true);
    }    
    
    /**
     * Sorts and returns new list
     */
    public List<ServiceRegistryEntry> sort(List<ServiceRegistryEntry> entries) {
        return sort(entries, false);
    }
    
    /**
     * Sorts, returns existing list if reuseList is true, otherwise returns new list
     */
    public List<ServiceRegistryEntry> sort(List<ServiceRegistryEntry> entries, boolean reuseList) {
        Assert.notNull(entries);
        //no point sorting if size < 2
        if (entries.size() < 2) {
            return entries;
        }
        List<ServiceRegistryEntry> list = reuseList ? entries : new ArrayList<ServiceRegistryEntry>(entries);
        Collections.sort(list, comparator);
        return list;
    }

    static class ServiceReferenceComparator implements Comparator<ServiceRegistryEntry> {

        public int compare(ServiceRegistryEntry o1, ServiceRegistryEntry o2) {
            int o2ranking = getServiceRanking(o2);
            int o1ranking = getServiceRanking(o1);
            
            if (o2ranking > o1ranking) {
            	return 1;
            }
            if (o2ranking < o1ranking) {
            	return -1;
            }
            
            return 0;
        }

        /**
         * Computes service ranking. If not specified or cannot be determined, then set to default of zero.
         * For number types, then will return value up to Integer.MAX_VALUE.
         * For String types, will return integer value if string can be converted to an int using Integer.valueOf(String).
         */
        int getServiceRanking(ServiceRegistryEntry entry) {
            Map<String, ?> attributes = entry.getAttributes();
            
            Integer ranking;
            
            Object rankingObject = attributes.get("service.ranking");
            if (rankingObject instanceof Integer) {
                ranking = (Integer) rankingObject;
            } else if  (rankingObject instanceof Number) {
                Long longVal = ((Number) rankingObject).longValue();
                if (longVal >= Integer.MAX_VALUE) {
                    ranking = Integer.MAX_VALUE;
                }
                else {
                    ranking = longVal.intValue();
                }
            } else if (rankingObject instanceof String) {
                try {
                    ranking = Integer.valueOf(rankingObject.toString());
                }
                catch (NumberFormatException e) {
                    ranking = 0;
                }
            } else {
                ranking = 0;
            }
            return ranking;
        }
        
    }
    
}
