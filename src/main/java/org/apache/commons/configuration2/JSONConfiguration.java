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
import org.apache.commons.configuration2.tree.ImmutableNode;

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

    /**
     * The object mapper used by the {@code JSONConfiguration}.
     */
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * The {@code MapType} used to convert types.
     */
    private final MapType type = mapper.getTypeFactory()
            .constructMapType(Map.class, String.class, Object.class);

    /**
     * Creates a new instance of {@code JSONConfiguration}.
     */
    public JSONConfiguration()
    {
        super();
    }

    /**
     * Creates a new instance of {@code JSONConfiguration} as a copy of the
     * specified configuration.
     *
     * @param c the configuration to be copied
     */
    public JSONConfiguration(final HierarchicalConfiguration<ImmutableNode> c)
    {
        super(c);
    }

    @Override
    public void read(final Reader in) throws ConfigurationException
    {
        try
        {
            final Map<String, Object> map = mapper.readValue(in, this.type);
            load(map);
        }
        catch (final Exception e)
        {
            rethrowException(e);
        }
    }

    @Override
    public void write(final Writer out) throws ConfigurationException, IOException
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
    public void read(final InputStream in) throws ConfigurationException
    {
        try
        {
            final Map<String, Object> map = mapper.readValue(in, this.type);
            load(map);
        }
        catch (final Exception e)
        {
            rethrowException(e);
        }
    }

}
