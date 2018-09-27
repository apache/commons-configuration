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

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.InputStreamSupport;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

/**
 * <p>
 * A specialized hierarchical configuration class that is able to parse YAML
 * documents.
 * </p>
 *
 * @since 2.2
 */
public class YAMLConfiguration extends AbstractYAMLBasedConfiguration
        implements FileBasedConfiguration, InputStreamSupport
{
    /**
     * Creates a new instance of {@code YAMLConfiguration}.
     */
    public YAMLConfiguration()
    {
        super();
    }

    /**
     * Creates a new instance of {@code YAMLConfiguration} as a copy of the
     * specified configuration.
     *
     * @param c the configuration to be copied
     */
    public YAMLConfiguration(final HierarchicalConfiguration<ImmutableNode> c)
    {
        super(c);
    }

    @Override
    public void read(final Reader in) throws ConfigurationException
    {
        try
        {
            final Yaml yaml = new Yaml();
            final Map<String, Object> map = (Map) yaml.load(in);
            load(map);
        }
        catch (final Exception e)
        {
            rethrowException(e);
        }
    }

    public void read(final Reader in, final LoaderOptions options)
            throws ConfigurationException
    {
        try
        {
            final Yaml yaml = new Yaml(options);
            final Map<String, Object> map = (Map) yaml.load(in);
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
        final DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dump(out, options);
    }

    public void dump(final Writer out, final DumperOptions options)
            throws ConfigurationException, IOException
    {
        final Yaml yaml = new Yaml(options);
        yaml.dump(constructMap(getNodeModel().getNodeHandler().getRootNode()),
                out);
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
            final Yaml yaml = new Yaml();
            final Map<String, Object> map = (Map) yaml.load(in);
            load(map);
        }
        catch (final Exception e)
        {
            rethrowException(e);
        }
    }

    public void read(final InputStream in, final LoaderOptions options)
            throws ConfigurationException
    {
        try
        {
            final Yaml yaml = new Yaml(options);
            final Map<String, Object> map = (Map) yaml.load(in);
            load(map);
        }
        catch (final Exception e)
        {
            rethrowException(e);
        }
    }

}
