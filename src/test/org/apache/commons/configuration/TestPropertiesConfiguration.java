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

/**
 * test for loading and saving properties files
 *
 * @author <a href="mailto:mpoeschl@marmot.at">Martin Poeschl</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @author <a href="mailto:dlr@apache.org">Daniel Rall</a>
 * @version $Id: TestPropertiesConfiguration.java,v 1.1 2003/12/23 15:09:05 epugh Exp $
 */
public class TestPropertiesConfiguration extends TestBasePropertiesConfiguration
{
    /** The File that we test with */
    private String testProperties = new File("conf/test.properties").getAbsolutePath();

    private String testBasePath = new File("conf").getAbsolutePath();
    private String testBasePath2 = new File("conf").getAbsoluteFile().getParentFile().getAbsolutePath();

    public TestPropertiesConfiguration(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        conf = new PropertiesConfiguration(testProperties);
    }

    public void atestSave() throws Exception
    {
        PropertiesConfiguration toSave = new PropertiesConfiguration();
        toSave.addProperty("string", "value1");
        Vector vec = new Vector();
        for (int i = 1; i < 5; i++)
        {
            vec.add("value" + i);
        }
        toSave.addProperty("array", vec);
        String filename = "STRING0";

        toSave.save(filename);
    }

    public void atestLoadViaProperty() throws Exception
    {
        PropertiesConfiguration pc = new PropertiesConfiguration();
        pc.setFileName(testProperties);
        pc.load();

        assertTrue("Make sure we have multiple keys", pc.getBoolean("test.boolean"));
    }

    public void testLoadViaPropertyWithBasePath() throws Exception
    {

        PropertiesConfiguration pc = new PropertiesConfiguration();
        pc.setBasePath(testBasePath);
        pc.setFileName("test.properties");
        pc.load();

        assertTrue("Make sure we have multiple keys", pc.getBoolean("test.boolean"));
    }

    public void testLoadViaPropertyWithBasePath2() throws Exception
    {

        PropertiesConfiguration pc = new PropertiesConfiguration();
        pc.setBasePath(testBasePath2);
        pc.setFileName("conf/test.properties");
        pc.load();

        assertTrue("Make sure we have multiple keys", pc.getBoolean("test.boolean"));

        pc = new PropertiesConfiguration();
        pc.setBasePath(testBasePath2);
        pc.setFileName("conf/test.properties");
        pc.load();

        assertTrue("Make sure we have multiple keys", pc.getBoolean("test.boolean"));
    }
}
