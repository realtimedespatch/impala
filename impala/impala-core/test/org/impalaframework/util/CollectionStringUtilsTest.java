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

package org.impalaframework.util;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.impalaframework.exception.ExecutionException;

import junit.framework.TestCase;

public class CollectionStringUtilsTest extends TestCase {
    
    public void testEscape() throws Exception {
        compareTokenize("a:b:c:d", "a,b\nc,d");
        compareTokenize("a:b:c:,d", "a,b\nc,\\,d");
        compareTokenize("a:b:c:d\\", "a,b,c,d\\");
    }
    
    private void compareTokenize(String expected, String initial) {
        assertEquals(Arrays.asList(expected.split(":")), Arrays.asList(CollectionStringUtils.tokenize(initial, ",\n")));
    }
    
    public void testParseStringLongMap() {
        String input = " name1 = 1 \n name2=3, name3 = 0";
        Map<String, Long> map = CollectionStringUtils.parseStringLongMap(input);
        assertEquals(Long.valueOf(3), map.get("name2"));
    }

    public void testParsePropertiesFromString() {
        String input = " name1 = 1 \n name2= value 2, name3 = value3 ";
        
        Map<String, String> map = CollectionStringUtils.parsePropertiesFromString(input);
        assertEquals(3, map.size());
        assertEquals("1", map.get("name1"));
        assertEquals("value 2", map.get("name2"));
        assertEquals("value3", map.get("name3"));
        
        Map<String, String> mapWithMissingEquals = CollectionStringUtils.parsePropertiesFromString(" name1 no equals 1 \nname2= value2,name3 = value3");
        assertEquals(3, mapWithMissingEquals.size());
        assertTrue(mapWithMissingEquals.containsKey("name1 no equals 1"));
        assertTrue(mapWithMissingEquals.containsKey("name2"));
        assertTrue(mapWithMissingEquals.containsKey("name3"));
    }
    
    public void testParseMapFromString() {
        String input = " name1 = 1 \n name2= 2007-06-11 16:15:32, name3 = value3 ";
        
        Map<String, Object> map = CollectionStringUtils.parseMapFromString(input);
        assertEquals(3, map.size());
        assertEquals(new Long(1), map.get("name1"));
        assertTrue(map.get("name2") instanceof Date);
        assertEquals("value3", map.get("name3"));
    }
    
    public void testEscapeKeys() throws Exception {
        String input = "name1=value1,name\\=2=value2";
        Map<String, Object> map = CollectionStringUtils.parseMapFromString(input);
        assertEquals(2, map.size());
        Object actual = map.get("name=2");
        assertEquals("value2", actual);
    }

    public void testLong() throws Exception {

        String input = "1, 2,3  ";
        List<Long> list = CollectionStringUtils.parseLongList(input);
        
        assertEquals(3, list.size());
        assertEquals(new Long(1), list.get(0));
        assertEquals(new Long(2), list.get(1));
        assertEquals(new Long(3), list.get(2));
    } 
    
    public void testInvalidLong() throws Exception {

        String input = "not a number";
        try {
            CollectionStringUtils.parseLongList(input);
            fail();
        }
        catch (ExecutionException e) {
           assertEquals("Value 'not a number' is not a number", e.getMessage());
        }
    }
    
    public void testEmptyLong() throws Exception {

        List<Long> list = CollectionStringUtils.parseLongList("");
        assertTrue(list.isEmpty());
    }
    
    public void testParseStringList() throws Exception {
        String input = "value 1, 2007-06-11 16:15:32 \n 3 ";
        List<String> list = CollectionStringUtils.parseStringList(input);
        
        assertEquals(3, list.size());
        assertEquals("value 1", list.get(0));
        assertEquals("2007-06-11 16:15:32", list.get(1));
        assertEquals("3", list.get(2));
    }

    public void testParseObjectList() throws Exception {
        String input = "value 1, 2007-06-11 16:15:32 \n 3 ";
        List<Object> list = CollectionStringUtils.parseObjectList(input);
        assertEquals(3, list.size());
        assertEquals("value 1", list.get(0));
        assertTrue(list.get(1) instanceof Date);
        assertEquals(new Long(3), list.get(2));
    }
    
    public void testParseEmptyList() throws Exception {
        assertTrue(CollectionStringUtils.parseStringList("").isEmpty());
        assertTrue(CollectionStringUtils.parseObjectList("").isEmpty());
        assertTrue(CollectionStringUtils.parsePropertiesFromString("").isEmpty());
        assertTrue(CollectionStringUtils.parseMapFromString("").isEmpty());
    }

}
