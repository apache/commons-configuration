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
 * @version $Id: TestCompositeConfigurationNonStringProperties.java,v 1.4 2004/06/02 16:42:24 ebourg Exp $
 */
public class TestCompositeConfigurationNonStringProperties extends BaseNonStringProperties
{
	/** The File that we test with */
	private String testProperties = new File("conf/test.properties").getAbsolutePath();

	public void setUp() throws Exception
	{
		PropertiesConfiguration pc =
			new PropertiesConfiguration(testProperties);
		CompositeConfiguration cc = new CompositeConfiguration();
		cc.addConfiguration(pc);
		conf = cc;
		nonStringTestHolder.setConfiguration(conf);
	}

}
