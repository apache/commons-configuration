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
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Maven" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Maven", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
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
 *
 * ====================================================================
 */

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.cactus.ServletTestCase;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TestConfigurationFactoryWithJNDI extends ServletTestCase
{
	private File testDigesterFile = new File("conf/testDigesterConfigurationWJNDI.xml");
	private static Log log = LogFactory.getLog(TestConfigurationFactoryWithJNDI.class);
	
	public TestConfigurationFactoryWithJNDI(String testName) {
		super(testName);
	}


	public void testLoadingWithDigester() throws Exception {

		ConfigurationFactory cf = new ConfigurationFactory();
		cf.setConfigurationFileName(testDigesterFile.toString());
		CompositeConfiguration compositeConfiguration = (CompositeConfiguration)cf.getConfiguration();

		assertEquals("Verify how many configs", 4, compositeConfiguration.getNumberOfConfigurations());
		
		assertEquals(JNDIConfiguration.class, compositeConfiguration.getConfiguration(1).getClass());
		assertEquals(PropertiesConfiguration.class, compositeConfiguration.getConfiguration(2).getClass());
		assertEquals(DOM4JConfiguration.class, compositeConfiguration.getConfiguration(3).getClass());
		PropertiesConfiguration pc = (PropertiesConfiguration) compositeConfiguration.getConfiguration(2);

		assertNotNull("Make sure we have a fileName:" + pc.getFileName(), pc.getFileName());
		
		assertTrue("Make sure we have loaded our key", compositeConfiguration.getBoolean("test.boolean"));
		assertEquals("I'm complex!", compositeConfiguration.getProperty("element2.subelement.subsubelement"));

		assertEquals("Make sure the JNDI config overwrites everything else!","80", compositeConfiguration.getString("test.overwrite"));
	}
	
	/**
	 * Verify the getKeys() method works.
	 * @throws Exception
	 */
    public void testGetKeys() throws Exception
    {
        ConfigurationFactory cf = new ConfigurationFactory();
        cf.setConfigurationFileName(testDigesterFile.toString());

        Configuration c = cf.getConfiguration();

        List iteratedList = IteratorUtils.toList(c.getKeys());
        assertTrue(iteratedList.contains("test.jndi"));
    }

	/**
	 * Test that a simple key works with JNDI
	 * @throws Exception
	 */
    public void testGetKeysWithString() throws Exception
    {
        String KEY = "test";
        ConfigurationFactory cf = new ConfigurationFactory();
        cf.setConfigurationFileName(testDigesterFile.toString());

        Configuration c = cf.getConfiguration();

        List iteratedList = IteratorUtils.toList(c.getKeys(KEY));
                     
        assertTrue("Size:" + iteratedList.size(),iteratedList.size()>0);
        assertTrue(iteratedList.contains("test.jndi"));
        for (Iterator i = iteratedList.iterator();i.hasNext();){
            String foundKey = (String)i.next();
            assertTrue(foundKey.startsWith(KEY)); 
        }
    }
    
    /**
     * Verify that if a key is made of multiple parts, we still find
     * the correct JNDI Context.
     * @throws Exception
     */
    public void testGetKeysWithString2() throws Exception
    {
        String KEY = "test.deep";
        ConfigurationFactory cf = new ConfigurationFactory();
        cf.setConfigurationFileName(testDigesterFile.toString());

        Configuration c = cf.getConfiguration();

        List iteratedList = IteratorUtils.toList(c.getKeys(KEY));
                     
        assertTrue("Size:" + iteratedList.size(),iteratedList.size()==2);
        assertTrue(iteratedList.contains("test.deep.somekey"));
        for (Iterator i = iteratedList.iterator();i.hasNext();){
            String foundKey = (String)i.next();
            assertTrue(foundKey.startsWith(KEY)); 
        }
    }

}
