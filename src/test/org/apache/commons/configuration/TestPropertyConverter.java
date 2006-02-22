/*
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.configuration;

import java.util.List;
import java.util.Iterator;

import junit.framework.TestCase;

/**
 * Test class for PropertyConverter.
 *
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 */
public class TestPropertyConverter extends TestCase
{
    public void testSplit()
    {
        String s = "abc, xyz , 123";
        List list = PropertyConverter.split(s, ',');

        assertEquals("size", 3, list.size());
        assertEquals("1st token for '" + s + "'", "abc", list.get(0));
        assertEquals("2nd token for '" + s + "'", "xyz", list.get(1));
        assertEquals("3rd token for '" + s + "'", "123", list.get(2));
    }

    public void testSplitWithEscapedSeparator()
    {
        String s = "abc\\,xyz, 123";
        List list = PropertyConverter.split(s, ',');

        assertEquals("size", 2, list.size());
        assertEquals("1st token for '" + s + "'", "abc,xyz", list.get(0));
        assertEquals("2nd token for '" + s + "'", "123", list.get(1));
    }

    public void testSplitEmptyValues()
    {
        String s = ",,";
        List list = PropertyConverter.split(s, ',');

        assertEquals("size", 3, list.size());
        assertEquals("1st token for '" + s + "'", "", list.get(0));
        assertEquals("2nd token for '" + s + "'", "", list.get(1));
        assertEquals("3rd token for '" + s + "'", "", list.get(2));
    }

    public void testSplitWithEndingSlash()
    {
        String s = "abc, xyz\\";
        List list = PropertyConverter.split(s, ',');

        assertEquals("size", 2, list.size());
        assertEquals("1st token for '" + s + "'", "abc", list.get(0));
        assertEquals("2nd token for '" + s + "'", "xyz\\", list.get(1));
    }

    public void testSplitNull()
    {
        List list = PropertyConverter.split(null, ',');
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    public void testToIterator()
    {
        int[] array = new int[]{1, 2, 3};

        Iterator it = PropertyConverter.toIterator(array, ',');

        assertEquals("1st element", new Integer(1), it.next());
        assertEquals("2nd element", new Integer(2), it.next());
        assertEquals("3rd element", new Integer(3), it.next());
    }

    /**
     * Tests the interpolation features.
     */
    public void testInterpolateString()
    {
        PropertiesConfiguration config = new PropertiesConfiguration();
        config.addProperty("animal", "quick brown fox");
        config.addProperty("target", "lazy dog");
        assertEquals("Wrong interpolation",
                "The quick brown fox jumps over the lazy dog.",
                PropertyConverter.interpolate(
                        "The ${animal} jumps over the ${target}.", config));
    }

    /**
     * Tests interpolation of an object. Here nothing should be substituted.
     */
    public void testInterpolateObject()
    {
        assertEquals("Object was not correctly interpolated", new Integer(42),
                PropertyConverter.interpolate(new Integer(42),
                        new PropertiesConfiguration()));
    }

    /**
     * Tests complex interpolation where the variables' values contain in turn
     * other variables.
     */
    public void testInterpolateRecursive()
    {
        PropertiesConfiguration config = new PropertiesConfiguration();
        config.addProperty("animal", "${animal_attr} fox");
        config.addProperty("target", "${target_attr} dog");
        config.addProperty("animal_attr", "quick brown");
        config.addProperty("target_attr", "lazy");
        assertEquals("Wrong complex interpolation",
                "The quick brown fox jumps over the lazy dog.",
                PropertyConverter.interpolate(
                        "The ${animal} jumps over the ${target}.", config));
    }

    /**
     * Tests an interpolation that leads to a cycle. This should throw an
     * exception.
     */
    public void testCyclicInterpolation()
    {
        PropertiesConfiguration config = new PropertiesConfiguration();
        config.addProperty("animal", "${animal_attr} ${species}");
        config.addProperty("animal_attr", "quick brown");
        config.addProperty("species", "${animal}");
        try
        {
            PropertyConverter.interpolate("This is a ${animal}", config);
            fail("Cyclic interpolation was not detected!");
        }
        catch (IllegalStateException iex)
        {
            // ok
        }
    }

    /**
     * Tests interpolation if a variable is unknown. Then the variable won't be
     * substituted.
     */
    public void testInterpolationUnknownVariable()
    {
        PropertiesConfiguration config = new PropertiesConfiguration();
        config.addProperty("animal", "quick brown fox");
        assertEquals("Wrong interpolation",
                "The quick brown fox jumps over ${target}.", PropertyConverter
                        .interpolate("The ${animal} jumps over ${target}.",
                                config));
    }
}
