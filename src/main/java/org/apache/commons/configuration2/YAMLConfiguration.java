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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.InputStreamSupport;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.StreamDataWriter;
import org.snakeyaml.engine.v2.common.FlowStyle;

/**
 * <p>
 * A specialized hierarchical configuration class that is able to parse YAML documents.
 * </p>
 *
 * @since 2.2
 */
public class YAMLConfiguration extends AbstractYAMLBasedConfiguration implements FileBasedConfiguration, InputStreamSupport {
    /**
     * Creates a new instance of {@code YAMLConfiguration}.
     */
    public YAMLConfiguration() {
    }

    /**
     * Creates a new instance of {@code YAMLConfiguration} as a copy of the specified configuration.
     *
     * @param c the configuration to be copied
     */
    public YAMLConfiguration(final HierarchicalConfiguration<ImmutableNode> c) {
        super(c);
    }

    @Override
    public void read(final Reader in) throws ConfigurationException {
        read(in, LoadSettings.builder().build());
    }

    public void read(final Reader in, final LoadSettings options) throws ConfigurationException {
        try {
            final Load yaml = createYamlForReading(options);
            final Map<String, Object> map = (Map<String, Object>) yaml.loadFromReader(in);
            load(map);
        } catch (final Exception e) {
            rethrowException(e);
        }
    }

    @Override
    public void write(final Writer out) throws ConfigurationException, IOException {
        final DumpSettings options = DumpSettings.builder()
            .setDefaultFlowStyle(FlowStyle.BLOCK)
            .build();
        dump(out, options);
    }

    public void dump(final Writer out, final DumpSettings options) {
        final Dump yaml = new Dump(options);
        final StreamDataWriter output = new StreamToStringWriter(out);
        yaml.dump(constructMap(getNodeModel().getNodeHandler().getRootNode()), output);
    }

    /**
     * Loads the configuration from the given input stream.
     *
     * @param in the input stream
     * @throws ConfigurationException if an error occurs
     */
    @Override
    public void read(final InputStream in) throws ConfigurationException {
        read(in, LoadSettings.builder().build());
    }

    public void read(final InputStream in, final LoadSettings options) throws ConfigurationException {
        try {
            final Load yaml = createYamlForReading(options);
            final Map<String, Object> map = (Map<String, Object>) yaml.loadFromInputStream(in);
            load(map);
        } catch (final Exception e) {
            rethrowException(e);
        }
    }

    /**
     * Creates a {@code Load} object for reading a Yaml file. The object is configured with some default settings.
     *
     * @param options options for loading the file
     * @return the {@code Load} instance for loading a file
     */
    private static Load createYamlForReading(final LoadSettings options) {
        return new Load(options);
    }
}

/**
 * Internal wrapper to catch the IO exceptions
 */
class StreamToStringWriter implements StreamDataWriter {

    /**
     * The destination
     */
    private final Writer out;
    StreamToStringWriter(final Writer out) {
        this.out = out;
    }
    @Override
    public void write(String var1) {
        try {
            out.write(var1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(String var1, int var2, int var3) {
        try {
            out.write(var1, var2, var3);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
