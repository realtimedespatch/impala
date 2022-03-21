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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.impalaframework.exception.ExecutionException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;


public abstract class CollectionStringUtils {

    /**
     * Generates a list of Strings from the underlying list, assuming that they
     * are separated by a new line or comma. Trims each entry in list. Note that
     * the character '\\' can be used to escape delimiters, so that these can
     * appear in values.
     * @param listString the input String
     * @return a list of Strings, values trimmed, in the input order
     */
    public static List<String> parseStringList(String listString) {

        String delimiters = ",\n";
        return parseList(listString, delimiters, stringConverter);
    }    
    
    /**
     * Generates a list of Long from the underlying list, assuming that they
     * are separated by a new line or comma. Uses
     * {@link Long#valueOf(String)} to convert String values to list.
     * Note that the character '\\' can be used to escape delimiters, so
     * that these can appear in values.
     * @param listString the input String
     * @return a list of Strings, values trimmed, in the input order
     */
    public static List<Long> parseLongList(String listString) {

        String delimiters = ",\n";
        return parseList(listString, delimiters, longConverter);
    }
    
    /**
     * Generates a list of Objects from the underlying list, assuming that they
     * are separated by a new line or comma. Uses
     * {@link ParseUtils#parseObject(String)} to convert String values to list values.
     * Note that the character '\\' can be used to escape delimiters, so
     * that these can appear in values.
     * @param listString the input String
     * @return a list of Strings, values trimmed, in the input order
     */
    public static List<Object> parseObjectList(String listString) {

        String delimiters = ",\n";
        return parseList(listString, delimiters, objectConverter);
    }

    /**
     * Generates map of String to String name value pairs, assuming that
     * individual pairings are separated by a new line or comma, and that name
     * to value pairs are separated by equals. Note that the character '\\' can
     * be used to escape delimiters, so that these can appear in keys or values
     * (in the case of the the pair separator), or keys (in the case of the name
     * to value separator).
     * @param mapString the input String
     * @return a String to String map. Uses {@link LinkedHashMap} so that the
     * ordering of the key value pairs is maintained
     */
    public static Map<String,String> parsePropertiesFromString(String mapString) {
        return parsePropertiesFromString(mapString, ",\n");
    }

    /**
     * As in {@link #parsePropertiesFromString(String)}, but with allowing the
     * user to specify the delimiters between pairings. Note that the character '\\' can
     * be used to escape delimiters, so that these can appear in keys or values
     * (in the case of the the pair separator), or keys (in the case of the name
     * to value separator).
     */
    public static Map<String, String> parsePropertiesFromString(String mapString, String delimiters) {
        return parseMap(mapString, delimiters, stringConverter);
    }
    
    /**
     * Generates map of String to Object name value pairs, assuming that
     * individual pairings are separated by a new line or comma, and that name
     * to value pairs are separated by equals. Note that the character '\\' can
     * be used to escape delimiters, so that these can appear in keys or values
     * (in the case of the the pair separator), or keys (in the case of the name
     * to value separator).
     * @param mapString the input String
     * @return a String to Object map. Uses
     * {@link ParseUtils#parseObject(String)} to convert String values to map
     * values. Uses {@link LinkedHashMap} so that the ordering of the key value
     * pairs is maintained
     */
    public static Map<String,Object> parseMapFromString(String mapString) {
        return parseMapFromString(mapString, ",\n");
    }
    
    /**
     * As in {@link #parseMapFromString(String)}, but with allowing the user to
     * specify the delimiters between pairings. Note that the character '\\' can
     * be used to escape delimiters, so that these can appear in keys or values
     * (in the case of the the pair separator), or keys (in the case of the name
     * to value separator).
     */
    public static Map<String, Object> parseMapFromString(String mapString, String delimiters) {
        return parseMap(mapString, delimiters, objectConverter);
    }

	/**
	 * As in {@link #parseMapFromString(String)}, but with assumption that keys are
	 * {@link String}s and values are {@link Long} instances.
	 */
    public static Map<String, Long> parseStringLongMap(String mapString) {
        return parseStringLongMap(mapString, ",\n");
    }

	/**
	 * As in {@link #parseMapFromString(String)}, but with assumption that keys are
	 * {@link String}s and values are {@link Long} instances.
	 */
    public static Map<String, Long> parseStringLongMap(String mapString, String delimiters) {
        return parseMap(mapString, delimiters, longConverter);
    }
    
    private static <T extends Object> Map<String,T> parseMap(
    		String mapString,
            String delimiters, 
            ValueConverter<T> valueConverter) {
        Assert.notNull(mapString, "map string cannot be null");
        Assert.notNull(delimiters, "delimiters cannot be null");
        
        String[] pairings = tokenize(mapString, delimiters);
        Map<String,T> map = new LinkedHashMap<String,T>();
        for (String pair : pairings) {
            
            StringBuffer keyBuffer = new StringBuffer();
            char[] pairChars = pair.toCharArray();
            
            boolean foundEquals = false;
            int i = 0;
            while (i < pairChars.length) {
                char c = pairChars[i];
                if (c == '=') {
                    foundEquals = true;
                    break;
                }
                if (c == '\\') {
                    i++;
                    c = pairChars[i];
                }
                keyBuffer.append(c);
                i++;
            }
            
            int equalsIndex = i;
            
            if (equalsIndex > 0) {
                T value = foundEquals ? valueConverter.convertValue(pair.substring(equalsIndex+1).trim()) : null;
                map.put(keyBuffer.toString().trim(), value);
            } else {
                if (StringUtils.hasText(pair)) {
                    map.put(pair.trim(), null);
                }
            }
        }
        
        return map;
    }

    private static <T extends Object> List<T> parseList(String listString, 
            String delimiters, ValueConverter<T> valueConverter) {

        Assert.notNull(listString, "listString string cannot be null");
        Assert.notNull(delimiters, "delimiters cannot be null");
        
        String[] pairings = tokenize(listString, delimiters); 
        List<T> list = new ArrayList<T>(pairings.length);  
        for (String string : pairings) {
            if (StringUtils.hasText(string)) {
                list.add(valueConverter.convertValue(string.trim()));
            }
        }
        
        return list;
    }

    static String[] tokenize(String listString, String delimiters) {
        char[] delimiterChars = delimiters.toCharArray();
        
        char[] listChars = listString.toCharArray();
        int i = 0;
        
        List<String> stringList = new LinkedList<String>();
        
        StringBuffer buffer = new StringBuffer();
        while (i < listChars.length) {
            
            char c = listChars[i];

            boolean advance = false;
            for (char d : delimiterChars) {
                if (c == d) {
                    stringList.add(buffer.toString());
                    buffer = new StringBuffer();
                    advance = true;
                    break;
                }
            }
            
            if (!advance) {
                if (c == '\\') {
                    if (i < listChars.length-1) {
                        char next = listChars[i+1];
                        
                        //if one of next chars is delimiter chars, 
                        //then advance to next char
                        for (char d : delimiterChars) {
                            if (d == next) {
                                i++;
                                c = next;
                                break;
                            }
                        }
                    }
                }
                
                buffer.append(c);
            }
            
            i++;
        }
        
        //add last buffer to String
        stringList.add(buffer.toString());
        
        String[] pairings = 
            stringList.toArray(EMPTY_STRING);
            //StringUtils.tokenizeToStringArray(listString, delimiters);
        return pairings;
    }
    
    private static interface ValueConverter<T extends Object> {
        T convertValue(String text);
    }
    
    private static ValueConverter<String> stringConverter = new ValueConverter<String>() {

        public String convertValue(String text) {
            return text;
        }
        
    };
    
    private static ValueConverter<Long> longConverter = new ValueConverter<Long>() {

        public Long convertValue(String text) {
            Long value;
            try {
                value = Long.valueOf(text);
            }
            catch (NumberFormatException e) {
                throw new ExecutionException("Value '" + text + "' is not a number");
            }            
            return value;
        }
        
    };
    
    private static ValueConverter<Object> objectConverter = new ValueConverter<Object>() {

        public Object convertValue(String text) {
            return ParseUtils.parseObject(text);
        }
        
    };

    private static final String[] EMPTY_STRING = new String[0];
    
}
