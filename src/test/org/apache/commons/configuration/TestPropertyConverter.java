package org.apache.commons.configuration;

import java.util.List;
import java.util.Iterator;

import junit.framework.TestCase;

/**
 * @author Emmanuel Bourg
 * @version $Revision: 1.1 $, $Date: 2004/12/14 17:03:51 $
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
}
