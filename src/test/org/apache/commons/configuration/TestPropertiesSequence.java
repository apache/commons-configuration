package org.apache.commons.configuration;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
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
 */

import java.io.File;

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.configuration.BaseConfiguration;

/**
 * Test that the configuration factory returns keys in the same
 * sequence as the properties configurator
 * 
 * @author <a href="mailto:epugh@upstate.com">Eric Pugh</a>
 * @author <a href0"mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @version $Id: TestPropertiesSequence.java,v 1.2 2004/01/16 14:23:39 epugh Exp $
 */
public class TestPropertiesSequence extends TestCase
{

    public void testConfigurationValuesInSameOrderFromFile() throws Exception
    {
        String simpleConfigurationFile = new File("conf/testSequence.properties").getAbsolutePath();
        String compositeConfigurationFile = new File("conf/testSequenceDigester.xml").getAbsolutePath();

        Configuration simpleConfiguration = new PropertiesConfiguration(simpleConfigurationFile);

        ConfigurationFactory configurationFactory = new ConfigurationFactory();
        configurationFactory.setConfigurationFileName(compositeConfigurationFile);
        Configuration compositeConfiguration = configurationFactory.getConfiguration();

        Configuration a = simpleConfiguration.subset("prefix");
        Configuration b = compositeConfiguration.subset("prefix");

        List keysSimpleConfiguration = IteratorUtils.toList(a.getKeys());
        List keysCompositeConfiguration = IteratorUtils.toList(b.getKeys());

        assertTrue("Size:" + keysSimpleConfiguration.size(), keysSimpleConfiguration.size() > 0);
        assertEquals(keysSimpleConfiguration.size(), keysCompositeConfiguration.size());

        for (int i = 0; i < keysSimpleConfiguration.size(); i++)
        {
            assertEquals(keysSimpleConfiguration.get(i), keysCompositeConfiguration.get(i));
        }
    }

    public void testConfigurationValuesInSameOrderWithManualAdd() throws Exception
    {
        String simpleConfigurationFile = new File("conf/testSequence.properties").getAbsolutePath();
        String compositeConfigurationFile = new File("conf/testSequenceDigester.xml").getAbsolutePath();

        Configuration simpleConfiguration = new PropertiesConfiguration(simpleConfigurationFile);

        ConfigurationFactory configurationFactory = new ConfigurationFactory();
        configurationFactory.setConfigurationFileName(compositeConfigurationFile);
        Configuration compositeConfiguration = configurationFactory.getConfiguration();

        simpleConfiguration.setProperty("prefix.Co.test", Boolean.TRUE);
        simpleConfiguration.setProperty("prefix.Av.test", Boolean.TRUE);

        compositeConfiguration.setProperty("prefix.Co.test", Boolean.TRUE);
        compositeConfiguration.setProperty("prefix.Av.test", Boolean.TRUE);

        Configuration a = simpleConfiguration.subset("prefix");
        Configuration b = compositeConfiguration.subset("prefix");

        List keysSimpleConfiguration = IteratorUtils.toList(a.getKeys());
        List keysCompositeConfiguration = IteratorUtils.toList(b.getKeys());

        assertTrue("Size:" + keysSimpleConfiguration.size(), keysSimpleConfiguration.size() > 0);
        assertEquals(keysSimpleConfiguration.size(), keysCompositeConfiguration.size());

        for (int i = 0; i < keysSimpleConfiguration.size(); i++)
        {
            assertEquals(keysSimpleConfiguration.get(i), keysCompositeConfiguration.get(i));
        }
    }

    public void testMappingInSameOrder() throws Exception
    {
        String simpleConfigurationFile = new File("conf/testSequence.properties").getAbsolutePath();
        String compositeConfigurationFile = new File("conf/testSequenceDigester.xml").getAbsolutePath();

        Configuration simpleConfiguration = new PropertiesConfiguration(simpleConfigurationFile);

        ConfigurationFactory configurationFactory = new ConfigurationFactory();
        configurationFactory.setConfigurationFileName(compositeConfigurationFile);
        Configuration compositeConfiguration = configurationFactory.getConfiguration();

        Configuration mapping = new BaseConfiguration();
        Configuration mapping2 = new BaseConfiguration();

        for (Iterator keys = simpleConfiguration.getKeys(); keys.hasNext();)
        {
            String key = (String) keys.next();
            String[] keyParts = StringUtils.split(key, ".");

            if ((keyParts.length == 3) && keyParts[0].equals("prefix") && keyParts[2].equals("postfix"))
            {
                String serviceKey = keyParts[1];

                if (!mapping.containsKey(serviceKey))
                {
                    mapping.setProperty(serviceKey, simpleConfiguration.getString(key));
                }
            }
        }

        for (Iterator keys = compositeConfiguration.getKeys(); keys.hasNext();)
        {
            String key = (String) keys.next();
            String[] keyParts = StringUtils.split(key, ".");

            if ((keyParts.length == 3) && keyParts[0].equals("prefix") && keyParts[2].equals("postfix"))
            {
                String serviceKey = keyParts[1];

                if (!mapping2.containsKey(serviceKey))
                {
                    mapping2.setProperty(serviceKey, compositeConfiguration.getString(key));
                }
            }
        }
    }
}
