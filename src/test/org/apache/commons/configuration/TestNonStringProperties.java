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

import java.io.File;

/**
 * Test if non-string properties are handled correctly.
 *
 * @version $Id: TestNonStringProperties.java,v 1.3 2004/02/27 17:41:34 epugh Exp $
 */
public class TestNonStringProperties extends BaseNonStringProperties
{
    /** The File that we test with */
    private String testProperties = new File("conf/test.properties").getAbsolutePath();
    
    public void setUp() throws Exception{
		conf = new PropertiesConfiguration(testProperties);
		nonStringTestHolder.setConfiguration(conf);
    }

   
}
