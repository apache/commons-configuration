/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.configuration2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.InputStreamSupport;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

/**
 * <p>
 * A specialized hierarchical configuration class that is able to parse JSON
 * documents.
 * </p>
 *
 * @since 2.2
 */
public class JSONConfiguration extends AbstractYAMLBasedConfiguration
        implements FileBasedConfiguration, InputStreamSupport
{
    private final ObjectMapper mapper = new ObjectMapper();
    private final MapType type = mapper.getTypeFactory()
            .constructMapType(Map.class, String.class, Object.class);

    /**
     * Creates a new instance of {@code JSONConfiguration}.
     */
    public JSONConfiguration()
    {
        super();
    }

    @Override
    public void read(Reader in) throws ConfigurationException
    {
        try
        {
            Map<String, Object> map = mapper.readValue(in, this.type);
            load(map);
        }
        catch (Exception e)
        {
            rethrowException(e);
        }
    }

    @Override
    public void write(Writer out) throws ConfigurationException, IOException
    {
        this.mapper.writer().writeValue(out, constructMap(
                this.getNodeModel().getNodeHandler().getRootNode()));
    }

    /**
     * Loads the configuration from the given input stream.
     *
     * @param in the input stream
     * @throws ConfigurationException if an error occurs
     */
    @Override
    public void read(InputStream in) throws ConfigurationException
    {
        try
        {
            Map<String, Object> map = mapper.readValue(in, this.type);
            load(map);
        }
        catch (Exception e)
        {
            rethrowException(e);
        }
    }

}
