package org.apache.commons.configuration;

/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

import org.apache.cactus.ServletTestCase;
import java.io.File;

public class TestJNDIAndCompositeConfiguration extends ServletTestCase
{
    private String testProperties =
        new File("conf/test.properties").getAbsolutePath();

    private CompositeConfiguration cc;
    private PropertiesConfiguration conf1;
    private JNDIConfiguration jndiConf;
    public TestJNDIAndCompositeConfiguration(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        jndiConf = new JNDIConfiguration();
        jndiConf.setPrefix("java:comp/env");

        cc = new CompositeConfiguration();
        conf1 = new PropertiesConfiguration(testProperties);

        cc.addConfiguration(jndiConf);
        cc.addConfiguration(conf1);

    }

    public void testSimpleGet() throws Exception
    {
        String s = cc.getString("test.overwrite");
        assertEquals("80", s);

        cc.clear();
        cc.addConfiguration(conf1);
        cc.addConfiguration(jndiConf);
        assertEquals("1", cc.getString("test.overwrite"));

    }

    /**
     * Tests setting values.  These are set in memory mode only!
     */
    public void testClearingProperty() throws Exception
    {

        cc.clearProperty("test.short");
        assertTrue(
            "Make sure test.short is gone!",
            !cc.containsKey("test.short"));
    }

    /**
     * Tests adding values.  Make sure they override any other properties!
     */
    public void testAddingProperty() throws Exception
    {

        cc.addProperty("test.short", "88");
        assertEquals(
            "Make sure test.short is overridden!",
            "88",
            cc.getString("test.short"));
    }

    /**
     * Tests setting values.  These are set in memory mode only!
     */
    public void testSettingMissingProperty() throws Exception
    {
        cc.setProperty("my.new.property", "supernew");
        assertEquals("supernew", cc.getString("my.new.property"));
    }
}
