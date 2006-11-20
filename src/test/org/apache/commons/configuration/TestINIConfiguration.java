/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.commons.configuration;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

/**
 * Test class for INIConfiguration.
 *
 * @author trevor.miller
 * @version $Id: TestHierarchicalConfiguration.java 439648 2006-09-02 20:42:10Z
 * oheger $
 */
public class TestINIConfiguration extends TestCase
{
	/** Constant for the content of an ini file. */
	private static final String INI_DATA = "[section1]\r\nvar1 = foo\r\n"
			+ "var2 = 451\r\n\r\n[section2]\r\nvar1 = 123.45\r\nvar2 = bar\r\n\r\n"
			+ "[section3]\r\nvar1 = true\r\n\r\n";

	/**
     * Test of save method, of class
     * org.apache.commons.configuration.INIConfiguration.
     */
	public void testSave() throws Exception
	{
		Writer writer = new StringWriter();
		INIConfiguration instance = new INIConfiguration();
		instance.addProperty("section1.var1", "foo");
		instance.addProperty("section1.var2", "451");
		instance.addProperty("section2.var1", "123.45");
		instance.addProperty("section2.var2", "bar");
		instance.addProperty("section3.var1", "true");
		instance.save(writer);
		assertEquals("Wrong content of ini file", INI_DATA, writer.toString());
	}

	/**
     * Test of load method, of class
     * org.apache.commons.configuration.INIConfiguration.
     */
	public void testLoad() throws Exception
	{
		checkLoad(INI_DATA);
	}

	/**
     * Tests the load() method when the alternative value separator is used (a
     * ':' for '=').
     */
	public void testLoadAlternativeSeparator() throws Exception
	{
		checkLoad(INI_DATA.replace('=', ':'));
	}

	/**
     * Helper method for testing the load operation. Loads the specified content
     * into a configuration and then checks some properties.
     *
     * @param data the data to load
     */
	private void checkLoad(String data) throws ConfigurationException,
			IOException
	{
		Reader reader = new StringReader(data);
		INIConfiguration instance = new INIConfiguration();
		instance.load(reader);
		reader.close();
		assertTrue(instance.getString("section1.var1").equals("foo"));
		assertTrue(instance.getInt("section1.var2") == 451);
		assertTrue(instance.getDouble("section2.var1") == 123.45);
		assertTrue(instance.getString("section2.var2").equals("bar"));
		assertTrue(instance.getBoolean("section3.var1"));
		assertTrue(instance.getSections().size() == 3);
	}

	/**
     * Test of isCommentLine method, of class
     * org.apache.commons.configuration.INIConfiguration.
     */
	public void testIsCommentLine()
	{
		INIConfiguration instance = new INIConfiguration();
		assertTrue(instance.isCommentLine("#comment1"));
		assertTrue(instance.isCommentLine(";comment1"));
		assertFalse(instance.isCommentLine("nocomment=true"));
		assertFalse(instance.isCommentLine(null));
	}

	/**
     * Test of isSectionLine method, of class
     * org.apache.commons.configuration.INIConfiguration.
     */
	public void testIsSectionLine()
	{
		INIConfiguration instance = new INIConfiguration();
		assertTrue(instance.isSectionLine("[section]"));
		assertFalse(instance.isSectionLine("nosection=true"));
		assertFalse(instance.isSectionLine(null));
	}

	/**
     * Test of getSections method, of class
     * org.apache.commons.configuration.INIConfiguration.
     */
	public void testGetSections()
	{
		INIConfiguration instance = new INIConfiguration();
		instance.addProperty("test1.foo", "bar");
		instance.addProperty("test2.foo", "abc");
		Set expResult = new HashSet();
		expResult.add("test1");
		expResult.add("test2");
		Set result = instance.getSections();
		assertEquals(expResult, result);
	}
}
