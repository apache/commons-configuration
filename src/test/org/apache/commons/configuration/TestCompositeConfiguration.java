package org.apache.commons.configuration;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.File;
import java.util.Vector;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import junit.framework.TestCase;

/**
 * test loading multiple configurations
 *
 
 * @author <a href="mailto:epugh@upstate.com">Eric Pugh</a>
 * @version $Id: TestCompositeConfiguration.java,v 1.1 2003/12/23 15:09:05 epugh Exp $
 */
public class TestCompositeConfiguration extends TestCase
{
    protected BasePropertiesConfiguration conf1;
    protected BasePropertiesConfiguration conf2;
    protected DOM4JConfiguration dom4jConf;
    protected CompositeConfiguration cc;
    /** The File that we test with */
    private String testProperties = new File("conf/test.properties").getAbsolutePath();
    private String testProperties2 = new File("conf/test2.properties").getAbsolutePath();
    private String testPropertiesXML = new File("conf/test.xml").getAbsolutePath();

    public TestCompositeConfiguration(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        cc = new CompositeConfiguration();
        conf1 = new PropertiesConfiguration(testProperties);
        conf2 = new PropertiesConfiguration(testProperties2);
        dom4jConf = new DOM4JConfiguration(new File(testPropertiesXML));
    }

    public void testAddRemoveConfigurations() throws Exception
    {

        cc.addConfiguration(conf1);
        assertEquals(2, cc.getNumberOfConfigurations());
        cc.addConfiguration(conf1);
        assertEquals(2, cc.getNumberOfConfigurations());
        cc.addConfiguration(conf2);
        assertEquals(3, cc.getNumberOfConfigurations());
        cc.removeConfiguration(conf1);
        assertEquals(2, cc.getNumberOfConfigurations());
        cc.clear();
        assertEquals(1, cc.getNumberOfConfigurations());
    }

    public void testGetProperty() throws Exception
    {
        cc.addConfiguration(conf1);
        cc.addConfiguration(conf2);
        assertEquals("Make sure we get the property from conf1 first", "packagea", cc.getString("packages"));
        cc.clear();

        cc.addConfiguration(conf2);
        cc.addConfiguration(conf1);
        assertEquals("Make sure we get the property from conf1 first", "override.packages", cc.getString("packages"));
    }

    public void testCantRemoveMemoryConfig() throws Exception
    {
        cc.clear();
        assertEquals(1, cc.getNumberOfConfigurations());

        Configuration internal = cc.getConfiguration(0);
        cc.removeConfiguration(internal);

        assertEquals(1, cc.getNumberOfConfigurations());

    }

    public void testGetPropertyMissing() throws Exception
    {
        cc.addConfiguration(conf1);
        cc.addConfiguration(conf2);

        assertNull(cc.getString("bogus.property"));

        assertTrue("Should be false", !cc.getBoolean("test.missing.boolean", false));
        assertTrue("Should be true", cc.getBoolean("test.missing.boolean.true", true));

    }

    /**
     * Tests <code>Vector</code> parsing.
     */
    public void testMultipleTypesOfConfigs() throws Exception
    {
        cc.addConfiguration(conf1);
        cc.addConfiguration(dom4jConf);
        assertEquals("Make sure we get the property from conf1 first", 1, cc.getInt("test.short"));
        cc.clear();

        cc.addConfiguration(dom4jConf);
        cc.addConfiguration(conf1);
        assertEquals("Make sure we get the property from dom4j", 8, cc.getInt("test.short"));
    }

    /**
     * Tests <code>Vector</code> parsing.
     */
    public void testPropertyExistsInOnlyOneConfig() throws Exception
    {
        cc.addConfiguration(conf1);
        cc.addConfiguration(dom4jConf);
        assertEquals("value", cc.getString("element"));
    }

    /**
     * Tests getting a default when the key doesn't exist
     */
    public void testDefaultValueWhenKeyMissing() throws Exception
    {
        cc.addConfiguration(conf1);
        cc.addConfiguration(dom4jConf);
        assertEquals("default", cc.getString("bogus", "default"));
        assertTrue(1.4 == cc.getDouble("bogus", 1.4));
        assertTrue(1.4 == cc.getDouble("bogus", 1.4));
    }

    /**
     * Tests <code>Vector</code> parsing.
     */
    public void testGettingConfiguration() throws Exception
    {
        cc.addConfiguration(conf1);
        cc.addConfiguration(dom4jConf);
        assertEquals(PropertiesConfiguration.class, cc.getConfiguration(0).getClass());
        assertEquals(DOM4JConfiguration.class, cc.getConfiguration(1).getClass());
    }

    /**
     * Tests setting values.  These are set in memory mode only!
     */
    public void testClearingProperty() throws Exception
    {

        cc.addConfiguration(conf1);
        cc.addConfiguration(dom4jConf);
        cc.clearProperty("test.short");
        assertTrue("Make sure test.short is gone!", !cc.containsKey("test.short"));
    }

    /**
     * Tests adding values.  Make sure they _DON'T_ override any other properties but add to the
     * existing properties  and keep sequence
     */
    public void testAddingProperty() throws Exception
    {

        cc.addConfiguration(conf1);
        cc.addConfiguration(dom4jConf);

        String [] values = cc.getStringArray("test.short");

        assertEquals("Number of values before add is wrong!", 2, values.length);
        assertEquals("First Value before add is wrong", "1", values[0]);
        assertEquals("Second Value is wrong", "8", values[1]);

        cc.addProperty("test.short", "88");

        values = cc.getStringArray("test.short");

        assertEquals("Number of values is wrong!", 3, values.length);
        assertEquals("First Value is wrong", "1", values[0]);
        assertEquals("Second Value is wrong", "8", values[1]);
        assertEquals("Third Value is wrong", "88", values[2]);
    }

    /**
     * Tests setting values.  These are set in memory mode only!
     */
    public void testSettingMissingProperty() throws Exception
    {
        cc.addConfiguration(conf1);
        cc.addConfiguration(dom4jConf);
        cc.setProperty("my.new.property", "supernew");
        assertEquals("supernew", cc.getString("my.new.property"));

    }

    /**
     * Tests retrieving subsets of configuraitions
     */
    public void testGettingSubset() throws Exception
    {
        cc.addConfiguration(conf1);
        cc.addConfiguration(dom4jConf);

        Configuration subset = null;
        subset = cc.subset("test.short");
        assertNotNull(subset);
        assertTrue("Shouldn't be empty", !subset.isEmpty());
        assertEquals("Make sure the initial loaded configs subset overrides" + "any later add configs subset", "1", subset.getString("test.short"));

        cc.setProperty("test.short", "43");
        subset = cc.subset("test.short");
        assertEquals("Make sure the initial loaded configs subset overrides" + "any later add configs subset", "43", subset.getString("test.short"));

    }

    /**
      * Tests <code>Vector</code> parsing.
      */
    public void testVector() throws Exception
    {
        cc.addConfiguration(conf1);
        cc.addConfiguration(dom4jConf);

        Vector packages = cc.getVector("packages");
        // we should get 3 packages here
        assertEquals(3, packages.size());

        Vector defaultVector = new Vector();
        defaultVector.add("1");
        defaultVector.add("2");

        packages = cc.getVector("packages.which.dont.exist", defaultVector);
        // we should get 2 packages here
        assertEquals(2, packages.size());
    }

    /**
      * Tests <code>String</code> array parsing.
      */
    public void testStringArray() throws Exception
    {
        cc.addConfiguration(conf1);
        cc.addConfiguration(dom4jConf);

        String[] packages = cc.getStringArray("packages");
        // we should get 3 packages here
        assertEquals(3, packages.length);

        packages = cc.getStringArray("packages.which.dont.exist");
        // we should get 0 packages here
        assertEquals(0, packages.length);
    }
    
    /**
      * Tests <code>getKeys</code> preserves the order
      */
    public void testGetKeysPreservesOrder() throws Exception
    {
        cc.addConfiguration(conf1);
        List orderedList = new ArrayList();
        for (Iterator keys = conf1.getKeys();keys.hasNext();){
            orderedList.add(keys.next());
        }
        List iteratedList = new ArrayList();
        for (Iterator keys = cc.getKeys();keys.hasNext();){
            iteratedList.add(keys.next());
        }
        assertEquals(orderedList.size(),iteratedList.size());
        for (int i =0;i<orderedList.size();i++){
            assertEquals(orderedList.get(i),iteratedList.get(i));
        }        
    }    

    /**
      * Tests <code>getKeys(String key)</code> preserves the order
      */
    public void testGetKeys2PreservesOrder() throws Exception
    {
        cc.addConfiguration(conf1);
        List orderedList = new ArrayList();
        for (Iterator keys = conf1.getKeys("test");keys.hasNext();){
            orderedList.add(keys.next());
        }
        List iteratedList = new ArrayList();
        for (Iterator keys = cc.getKeys("test");keys.hasNext();){
            iteratedList.add(keys.next());
        }
        assertEquals(orderedList.size(),iteratedList.size());
        for (int i =0;i<orderedList.size();i++){
            assertEquals(orderedList.get(i),iteratedList.get(i));
        }        
    }        
}
