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


/**
 * Tests {@link
 * org.apache.commons.configuration.ClassPropertiesConfiguration}.
 *
 * @version $Id: TestClassPropertiesConfiguration.java,v 1.3 2004/02/27 17:41:34 epugh Exp $
 */
public class TestClassPropertiesConfiguration extends TestBasePropertiesConfiguration
{
    /** The File that we test with */
    private static final String[] TEST_FILE_NAMES = 
    {
        "/testClasspath.properties", "test.properties", "./test.properties",
        "/org/apache/commons/configuration/test.properties"
     };

    protected void setUp() throws Exception
    {
        conf = new ClassPropertiesConfiguration(getClass(), TEST_FILE_NAMES[0]);
    }
    
    /** Test that loading from the classpath in various path formats works */
    public void testClasspathLoading() throws Exception
    {
      for (int i=0; i<TEST_FILE_NAMES.length; i++) 
      {
        conf = new ClassPropertiesConfiguration(getClass(), TEST_FILE_NAMES[i]);
        testLoad();
      }
    }
}
