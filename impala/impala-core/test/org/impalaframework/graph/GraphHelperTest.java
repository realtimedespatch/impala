/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.impalaframework.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;


/**
 * Based on original source code from Apache Avalon: DirectedAcyclicGraphVerifierTestCase
 * GraphHelperTest.java does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @author Phil Zoio
 */
public class GraphHelperTest extends TestCase
{
    public GraphHelperTest( String name )
    {
        super( name );
    }

    public void testIsDAG()
    {
        try
        {
            Vertex root = new Vertex( "Root" );
            root.addDependency( new Vertex( "Child1" ) );
            root.addDependency( new Vertex( "Child2" ) );

            GraphHelper.verify( root );
        }
        catch ( CyclicDependencyException cde )
        {
            fail( "Incorrectly found a Cycle" );
        }

        try
        {
            Vertex root = new Vertex( "Root" );
            root.addDependency( new Vertex( "Child1" ) );
            root.addDependency( new Vertex( "Child2" ) );

            Vertex child3 = new Vertex( "Child3" );
            child3.addDependency( root );

            root.addDependency( child3 );

            GraphHelper.verify( root );

            fail( "Incorrectly missed the Cycle" );
        }
        catch ( CyclicDependencyException cde )
        {
            // Success!
        }
    }

    /**
     * This test waas written to test the algorithm used to search for cycles.
     *  It makes sure that cycles that start a ways into the dependency tree
     *  are handled correctly.
     */
    public void testCycleTest() throws Exception
    {
        Vertex component1 = new Vertex( "Component1" );
        Vertex component2 = new Vertex( "Component2" );
        Vertex component3 = new Vertex( "Component3" );
        Vertex component4 = new Vertex( "Component4" );
        Vertex component5 = new Vertex( "Component5" );
        
        List<Vertex> vertices = new ArrayList<Vertex>( 5 );
        vertices.add( component1 );
        vertices.add( component2 );
        vertices.add( component3 );
        vertices.add( component4 );
        vertices.add( component5 );
        
        component1.addDependency( component2 );
        component2.addDependency( component3 );
            
        component3.addDependency( component4 );
        component4.addDependency( component5 );
        component5.addDependency( component3 ); // Cycle
        
        try
        {
            GraphHelper.topologicalSort( vertices );
            fail( "Did not detect the expected cyclic dependency" );
        }
        catch ( CyclicDependencyException cde )
        {
            //Success!
        }
    }
        
    public void testSortDAG() throws Exception {
        Vertex a = new Vertex("a");
        Vertex b = new Vertex("b");
        Vertex c = new Vertex("c");
        Vertex d = new Vertex("d");
        Vertex e = new Vertex("e");
        Vertex f = new Vertex("f");
        Vertex g = new Vertex("g");

        List<Vertex> vertices = new ArrayList<Vertex>();
        vertices.add(a);
        vertices.add(b);
        vertices.add(c);
        vertices.add(d);
        vertices.add(e);
        vertices.add(f);
        vertices.add(g);
        
        b.addDependency(a);
        d.addDependency(b);
        e.addDependency(c);
        e.addDependency(d);
        f.addDependency(b);
        f.addDependency(e);
        g.addDependency(c);
        g.addDependency(d);
        g.addDependency(f);
        
        /*
a
b depends on a
c
d depends on b
e depends on c, d
f depends on b, e
g on c, d, f
         */
        
        Collections.shuffle(vertices);

        System.out.println("----- Before sorting ----");
        printVertices(vertices);

        GraphHelper.topologicalSort(vertices);
        
        System.out.println("----- After sorting ----");
        printVertices(vertices);

        System.out.println("----- e list ----");
        final List<Vertex> eList = GraphHelper.list(e);
        printVertices(eList);
        
    }

    private void printVertices(List<Vertex> vertices) {
        for (Vertex v : vertices) {
            System.out.println(v.getName());
        }
    }
    
    
    public void testVerifySortDAG() throws Exception
    {
        Vertex component1 = new Vertex( "Component1" );
        Vertex component2 = new Vertex( "Component2" );
        Vertex component3 = new Vertex( "Component3" );
        Vertex component4 = new Vertex( "Component4" );
        Vertex component5 = new Vertex( "Component5" );

        component1.addDependency( component2 );
        component1.addDependency( component3 );

        component3.addDependency( component4 );

        component5.addDependency( component2 );
        component5.addDependency( component4 );

        List<Vertex> vertices = new ArrayList<Vertex>( 5 );
        vertices.add( component1 );
        vertices.add( component2 );
        vertices.add( component3 );
        vertices.add( component4 );
        vertices.add( component5 );

        GraphHelper.topologicalSort( vertices );
        verifyGraphOrders( vertices );
        verifyListOrder( vertices );

        Collections.shuffle( vertices );
        GraphHelper.topologicalSort( vertices );
        verifyGraphOrders( vertices );
        verifyListOrder( vertices );

        component4.addDependency( component1 );
        Collections.shuffle( vertices );

        try
        {
            GraphHelper.topologicalSort( vertices );
            fail( "Did not detect the expected cyclic dependency" );
        }
        catch ( CyclicDependencyException cde )
        {
            //Success!
        }
    }
    
    private void verifyGraphOrders( List<Vertex> vertices )
    {
        for ( Iterator<Vertex> iter = vertices.iterator(); iter.hasNext(); )
        {
            Vertex v = iter.next();
            
            // Make sure that the orders of all dependencies are less than
            //  the order of v.
            for ( Iterator<Vertex> iter2 = v.getDependencies().iterator(); iter2.hasNext(); )
            {
                Vertex dv = (Vertex)iter2.next();
                assertTrue( "The order of " + dv.getName() + " (" + dv.getOrder() + ") should be "
                    + "less than the order of " + v.getName() + " (" + v.getOrder() + ")",
                    dv.getOrder() < v.getOrder() );
            }
        }
    }
    
    private void verifyListOrder( List<Vertex> vertices )
    {
        Vertex[] ary = new Vertex[vertices.size()];
        vertices.toArray( ary );
        for ( int i = 1; i < ary.length; i++ )
        {
            assertTrue( "The order of vertex #" + ( i - 1 ) + " (" + ary[i - 1].getOrder() + ") "
                + "should be <= the order of vertex #" + ( i ) + " (" + ary[i].getOrder() + ")",
                ary[i - 1].getOrder() <= ary[i].getOrder() );
        }
    }
}
