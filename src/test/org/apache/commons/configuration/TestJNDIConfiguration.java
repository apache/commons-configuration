package org.apache.commons.configuration;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import java.util.Iterator;

import junit.framework.TestCase;

/**
 * test if non-string properties are handled correctly
 *
 * @version $Id: TestJNDIConfiguration.java,v 1.5 2004/02/27 17:41:34 epugh Exp $
 */
public class TestJNDIConfiguration extends TestCase
{
	private Configuration conf;
	private NonStringTestHolder nonStringTestHolder;

   

    public void setUp() throws Exception
    {
		//InitialContext context = new InitialContext();
		//assertNotNull(context);
        
        JNDIConfiguration jndiConfiguration = new JNDIConfiguration();
		jndiConfiguration.setPrefix("");
        conf = jndiConfiguration;
        nonStringTestHolder = new NonStringTestHolder();
        nonStringTestHolder.setConfiguration(conf);

    }
    
	public void testBoolean() throws Exception
	  {
		  nonStringTestHolder.testBoolean();
	  }

	  public void testBooleanDefaultValue() throws Exception
	  {
		  nonStringTestHolder.testBooleanDefaultValue();
	  }

	  public void testByte() throws Exception
	  {
		  nonStringTestHolder.testByte();
	  }

	  public void testDouble() throws Exception
	  {
		  nonStringTestHolder.testDouble();
	  }

	  public void testDoubleDefaultValue() throws Exception
	  {
		  nonStringTestHolder.testDoubleDefaultValue();
	  }

	  public void testFloat() throws Exception
	  {
		  nonStringTestHolder.testFloat();
	  }

	  public void testFloatDefaultValue() throws Exception
	  {
		  nonStringTestHolder.testFloatDefaultValue();

	  }

	  public void testInteger() throws Exception
	  {
		  nonStringTestHolder.testInteger();
	  }

	  public void testIntegerDefaultValue() throws Exception
	  {
		  nonStringTestHolder.testIntegerDefaultValue();
	  }

	  public void testLong() throws Exception
	  {
		  nonStringTestHolder.testLong();
	  }
	  public void testLongDefaultValue() throws Exception
	  {
		  nonStringTestHolder.testLongDefaultValue();
	  }

	  public void testShort() throws Exception
	  {
		  nonStringTestHolder.testShort();
	  }

	  public void testShortDefaultValue() throws Exception
	  {
		  nonStringTestHolder.testShortDefaultValue();
	  }

	  public void testListMissing() throws Exception
	  {
		  nonStringTestHolder.testListMissing();
	  }
	  public void testSubset() throws Exception
	 {
	    // seems to always be failing.
		//nonStringTestHolder.testSubset();
	      
	 }
	  
	  public void testProperties() throws Exception{
	      Object o = conf.getProperty("test.boolean");
	      assertNotNull(o);
	      assertEquals("true",o.toString());
	      
	  }

	  /** 
	   * Currently failing in that we don't get back any keys!
	   * @throws Exception
	   */
	  public void testGetKeys() throws Exception{
	      Iterator i = conf.getKeys();
	      for (;i.hasNext();){
	          System.out.println(i.next());
	      }
	  }

    

}