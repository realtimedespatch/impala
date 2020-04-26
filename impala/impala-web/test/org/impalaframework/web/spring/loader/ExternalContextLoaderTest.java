package org.impalaframework.web.spring.loader;

import java.util.Arrays;

import junit.framework.TestCase;

public class ExternalContextLoaderTest extends TestCase {

    private ExternalContextLoader loader;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.clearProperty(ExternalContextLoader.EXTERNAL_CONFIG_LOCATIONS_PARAM);
        loader = new ExternalContextLoader();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        System.clearProperty(ExternalContextLoader.EXTERNAL_CONFIG_LOCATIONS_PARAM);
    }
    
    public void testCustomize() {
        String[] locations = new String[]{"location1", "location2"};
        String[] expandedLocations = loader.getExpandedLocations(locations);
        assertSame(locations, expandedLocations);
        assertTrue(Arrays.equals(locations, expandedLocations));
        
        System.setProperty(ExternalContextLoader.EXTERNAL_CONFIG_LOCATIONS_PARAM, "location3, location4");
        String[] expected = new String[]{"location1", "location2", "location3", "location4"};
        assertTrue(Arrays.equals(expected, loader.getExpandedLocations(locations)));
    }

}
