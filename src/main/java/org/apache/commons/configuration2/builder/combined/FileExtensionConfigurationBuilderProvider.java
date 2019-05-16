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
package org.apache.commons.configuration2.builder.combined;

import java.util.Collection;

import org.apache.commons.configuration2.builder.BuilderParameters;
import org.apache.commons.configuration2.builder.FileBasedBuilderParametersImpl;
import org.apache.commons.configuration2.ex.ConfigurationException;

/**
 * <p>
 * A specialized implementation of {@link ConfigurationBuilderProvider} which
 * determines the name of the result configuration class based on the extension
 * of the file to load.
 * </p>
 * <p>
 * This class works analogously to its base class {@link BaseConfigurationBuilderProvider};
 * especially, the resulting builder is created based on reflection. It extends
 * the super class's functionality by a specific mechanism for determining the
 * resulting configuration class: At construction time two configuration class
 * names and a file extension are passed in. If a file name is provided in the
 * builder's initialization parameters and this file name has the specified
 * extension, then the first configuration class name is used; otherwise the
 * default configuration class name is selected.
 * </p>
 * <p>
 * There are some tags for {@code CombinedConfigurationProvider} which can
 * produce different results depending on the configuration files they have to
 * load. This class can be used to implement this feature in a generic way.
 * </p>
 *
 * @since 2.0
 */
public class FileExtensionConfigurationBuilderProvider extends
        BaseConfigurationBuilderProvider
{
    /** Constant for the file extension separator. */
    private static final char EXT_SEPARATOR = '.';

    /** The matching configuration class. */
    private final String matchingConfigurationClass;

    /** The file extension. */
    private final String extension;

    /**
     * Creates a new instance of
     * {@code FileExtensionConfigurationBuilderProvider}.
     *
     * @param bldrCls the name of the builder class
     * @param reloadBldrCls the name of a builder class to be used if reloading
     *        support is required (<b>null</b> if reloading is not supported)
     * @param matchingConfigCls the name of the configuration class to be used
     *        if the provided file extension matches (must not be <b>null</b>)
     * @param defConfigClass the name of the configuration class to be used if
     *        the provided file extension does not match (must not be
     *        <b>null</b>)
     * @param ext the file extension to select the configuration class (must not
     *        be <b>null</b>)
     * @param paramCls a collection with the names of parameters classes; an
     *        instance of a parameters object with basic properties is created
     *        automatically and does not need to be contained in this list; the
     *        collection can be <b>null</b> if no additional parameter objects
     *        are needed
     * @throws IllegalArgumentException if a required parameter is missing
     */
    public FileExtensionConfigurationBuilderProvider(final String bldrCls,
            final String reloadBldrCls, final String matchingConfigCls,
            final String defConfigClass, final String ext, final Collection<String> paramCls)
    {
        super(bldrCls, reloadBldrCls, defConfigClass, paramCls);
        if (matchingConfigCls == null)
        {
            throw new IllegalArgumentException(
                    "Matching configuration class must not be null!");
        }
        if (ext == null)
        {
            throw new IllegalArgumentException(
                    "File extension must not be null!");
        }

        matchingConfigurationClass = matchingConfigCls;
        extension = ext;
    }

    /**
     * Returns the name of the matching configuration class. This class is used
     * if the file extension matches the extension of this provider.
     *
     * @return the matching configuration class
     */
    public String getMatchingConfigurationClass()
    {
        return matchingConfigurationClass;
    }

    /**
     * Returns the file extension of this provider.
     *
     * @return the file extension to match
     */
    public String getExtension()
    {
        return extension;
    }

    /**
     * {@inheritDoc} This implementation tries to find a
     * {@link FileBasedBuilderParametersImpl} object in the parameter objects. If
     * one is found, the extension of the file name is obtained and compared
     * against the stored file extension. In case of a match, the matching
     * configuration class is selected, otherwise the default one.
     */
    @Override
    protected String determineConfigurationClass(final ConfigurationDeclaration decl,
            final Collection<BuilderParameters> params) throws ConfigurationException
    {
        final String currentExt = extractExtension(fetchCurrentFileName(params));
        return getExtension().equalsIgnoreCase(currentExt) ? getMatchingConfigurationClass()
                : getConfigurationClass();
    }

    /**
     * Tries to obtain the current file name from the given list of parameter
     * objects.
     *
     * @param params the parameter objects
     * @return the file name or <b>null</b> if unspecified
     */
    private static String fetchCurrentFileName(
            final Collection<BuilderParameters> params)
    {
        for (final BuilderParameters p : params)
        {
            if (p instanceof FileBasedBuilderParametersImpl)
            {
                final FileBasedBuilderParametersImpl fp = (FileBasedBuilderParametersImpl) p;
                return fp.getFileHandler().getFileName();
            }
        }
        return null;
    }

    /**
     * Extracts the extension from the given file name. The name can be
     * <b>null</b>.
     *
     * @param fileName the file name
     * @return the extension (<b>null</b> if there is none)
     */
    private static String extractExtension(final String fileName)
    {
        if (fileName == null)
        {
            return null;
        }

        final int pos = fileName.lastIndexOf(EXT_SEPARATOR);
        return (pos < 0) ? null : fileName.substring(pos + 1);
    }
}
