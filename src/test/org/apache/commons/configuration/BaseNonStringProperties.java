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

import junit.framework.TestCase;

/**
 * test if non-string properties are handled correctly
 *
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @version $Id: BaseNonStringProperties.java,v 1.1 2003/12/23 15:09:05 epugh Exp $
 */
public abstract class BaseNonStringProperties extends TestCase
{

    protected NonStringTestHolder nonStringTestHolder =
        new NonStringTestHolder();
    public abstract void setUp() throws Exception;

    public Configuration conf = null;

    public BaseNonStringProperties(String s) throws Exception
    {
        super(s);

    }

    public void testBoolean() throws Exception
    {
        nonStringTestHolder.testBoolean();
    }

    public void testBooleanDefaultValue() throws Exception
    {
        nonStringTestHolder.testBooleanDefaultValue();
    }

    public void testBooleanArrayValue() throws Exception
    {
        boolean booleanValue = conf.getBoolean("test.boolean.array");
        assertEquals(false, booleanValue);
        assertEquals(2, conf.getVector("test.boolean.array").size());
    }

    public void testByte() throws Exception
    {
        nonStringTestHolder.testByte();
    }

    public void testByteArrayValue() throws Exception
    {
        byte testValue = 20;
        byte byteValue = conf.getByte("test.byte.array");
        assertEquals(testValue, byteValue);
        assertEquals(2, conf.getVector("test.byte.array").size());
    }

    public void testDouble() throws Exception
    {
        nonStringTestHolder.testDouble();
    }

    public void testDoubleDefaultValue() throws Exception
    {
        nonStringTestHolder.testDoubleDefaultValue();
    }

    public void testDoubleArrayValue() throws Exception
    {
        double testValue = 20.35;
        double doubleValue = conf.getDouble("test.double.array");
        assertEquals(testValue, doubleValue, 0.01);
        assertEquals(2, conf.getVector("test.double.array").size());
    }

    public void testFloat() throws Exception
    {
        nonStringTestHolder.testFloat();
    }

    public void testFloatDefaultValue() throws Exception
    {
        nonStringTestHolder.testFloatDefaultValue();

    }

    public void testFloatArrayValue() throws Exception
    {
        float testValue = (float) 30.35;
        float floatValue = conf.getFloat("test.float.array");
        assertEquals(testValue, floatValue, 0.01);
        assertEquals(2, conf.getVector("test.float.array").size());
    }

    public void testInteger() throws Exception
    {
        nonStringTestHolder.testInteger();
    }

    public void testIntegerDefaultValue() throws Exception
    {
        nonStringTestHolder.testIntegerDefaultValue();
    }

    public void testIntegerArrayValue() throws Exception
    {
        int intValue = conf.getInt("test.integer.array");
        assertEquals(20, intValue);
        assertEquals(2, conf.getVector("test.integer.array").size());
    }

    public void testLong() throws Exception
    {
        nonStringTestHolder.testLong();
    }
    public void testLongDefaultValue() throws Exception
    {
        nonStringTestHolder.testLongDefaultValue();
    }
    public void testLongArrayValue() throws Exception
    {
        long longValue = conf.getLong("test.long.array");
        assertEquals(2000000, longValue);
        assertEquals(2, conf.getVector("test.long.array").size());
    }

    public void testShort() throws Exception
    {
        nonStringTestHolder.testShort();
    }

    public void testShortDefaultValue() throws Exception
    {
        nonStringTestHolder.testShortDefaultValue();
    }
    public void testShortArrayValue() throws Exception
    {
        short shortValue = conf.getShort("test.short.array");
        assertEquals(2, shortValue);
        assertEquals(2, conf.getVector("test.short.array").size());
    }

    public void testVectorMissing() throws Exception
    {
        nonStringTestHolder.testVectorMissing();
    }

    public void testSubset() throws Exception
    {
        nonStringTestHolder.testSubset();
    }
    public void testIsEmpty() throws Exception
    {
        nonStringTestHolder.testIsEmpty();
    }
}
