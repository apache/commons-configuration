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

import java.util.Iterator;

import junit.framework.Assert;

/**
 * Pulling out the calls to do the tests so both JUnit and Cactus tests 
 * can share.
 * 
 * @author Eric Pugh
 * @version $Id: NonStringTestHolder.java,v 1.2 2003/12/24 14:28:21 epugh Exp $
 */
public class NonStringTestHolder
{
    private Configuration configuration;
    public void testBoolean() throws Exception
    {
        boolean booleanValue = configuration.getBoolean("test.boolean");
        Assert.assertEquals(true, booleanValue);
        Assert.assertEquals(1, configuration.getList("test.boolean").size());
    }

    public void testBooleanDefaultValue() throws Exception
    {
        boolean booleanValue =
            configuration.getBoolean("test.boolean.missing", true);
        Assert.assertEquals(true, booleanValue);

        Boolean booleanObject =
            configuration.getBoolean("test.boolean.missing", new Boolean(true));
        Assert.assertEquals(new Boolean(true), booleanObject);
    }

    public void testByte() throws Exception
    {
        byte testValue = 10;
        byte byteValue = configuration.getByte("test.byte");
        Assert.assertEquals(testValue, byteValue);
        Assert.assertEquals(1, configuration.getList("test.byte").size());
    }

    public void testDouble() throws Exception
    {
        double testValue = 10.25;
        double doubleValue = configuration.getDouble("test.double");
        Assert.assertEquals(testValue, doubleValue, 0.01);
        Assert.assertEquals(1, configuration.getList("test.double").size());
    }

    public void testDoubleDefaultValue() throws Exception
    {
        double testValue = 10.25;
        double doubleValue =
            configuration.getDouble("test.double.missing", 10.25);

        Assert.assertEquals(testValue, doubleValue, 0.01);
    }

    public void testFloat() throws Exception
    {
        float testValue = (float) 20.25;
        float floatValue = configuration.getFloat("test.float");
        Assert.assertEquals(testValue, floatValue, 0.01);
        Assert.assertEquals(1, configuration.getList("test.float").size());
    }

    public void testFloatDefaultValue() throws Exception
    {
        float testValue = (float) 20.25;
        float floatValue =
            configuration.getFloat("test.float.missing", testValue);
        Assert.assertEquals(testValue, floatValue, 0.01);

    }

    public void testInteger() throws Exception
    {
        int intValue = configuration.getInt("test.integer");
        Assert.assertEquals(10, intValue);
        Assert.assertEquals(1, configuration.getList("test.integer").size());
    }

    public void testIntegerDefaultValue() throws Exception
    {
        int intValue = configuration.getInt("test.integer.missing", 10);
        Assert.assertEquals(10, intValue);
    }

    public void testLong() throws Exception
    {
        long longValue = configuration.getLong("test.long");
        Assert.assertEquals(1000000, longValue);
        Assert.assertEquals(1, configuration.getList("test.long").size());
    }
    public void testLongDefaultValue() throws Exception
    {
        long longValue = configuration.getLong("test.long.missing", 1000000);
        Assert.assertEquals(1000000, longValue);
    }

    public void testShort() throws Exception
    {
        short shortValue = configuration.getShort("test.short");
        Assert.assertEquals(1, shortValue);
        Assert.assertEquals(1, configuration.getList("test.short").size());
    }

    public void testShortDefaultValue() throws Exception
    {
        short shortValue =
            configuration.getShort("test.short.missing", (short) 1);
        Assert.assertEquals(1, shortValue);
    }

    public void testListMissing() throws Exception
    {

        Assert.assertEquals(
            0,
            configuration.getList("missing.list").size());
    }

    public void testSubset() throws Exception
    {
        String KEY_VALUE = "test.short";
        Configuration subset = configuration.subset(KEY_VALUE);
		boolean foundKeyValue = false;
        for (Iterator i = subset.getKeys(); i.hasNext();)
        {
            String key = (String) i.next();
            if (!key.equals(KEY_VALUE))
            {

                Assert.assertTrue(
                    "Key is:" + key,
                    !key.startsWith("test.short"));
            }
            else {
            	foundKeyValue=true;
            }
        }
        Assert.assertTrue("Make sure test.short did show up.  It is valid.",foundKeyValue);
    }

    public void testIsEmpty() throws Exception
    {
        Assert.assertTrue(!configuration.isEmpty());

    }
    /**
     * @return
     */
    public Configuration getConfiguration()
    {
        return configuration;
    }

    /**
     * @param configuration
     */
    public void setConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

}
