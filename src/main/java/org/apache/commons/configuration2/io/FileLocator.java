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
package org.apache.commons.configuration2.io;

import java.net.URL;
import java.util.Objects;

/**
 * <p>
 * A class describing the location of a file.
 * </p>
 * <p>
 * An instance of this class provides information for locating and accessing a file. The file location can be defined
 * </p>
 * <ul>
 * <li>as a URL; this identifies a file in a unique way</li>
 * <li>as a combination of base path and file name; if this variant is used, there may be an additional location step
 * required in order to identify the referenced file (for instance, the file name may be interpreted as the name of a
 * resource to be loaded from class path).</li>
 * </ul>
 * <p>
 * In addition, other properties are available which are also needed for loading or saving a file, like the underlying
 * {@link FileSystem}. The encoding to be used when accessing the represented data is also part of the data contained in
 * an instance; if no encoding is set explicitly, the platform's default encoding is used.
 * <p>
 * Instances of this class are immutable and thus can be safely shared between arbitrary components. {@link FileHandler}
 * also uses an instance to reference the associated file. Instances are created using a <em>builder</em>.
 * {@link FileLocatorUtils} offers convenience methods for obtaining such a builder.
 * </p>
 *
 * @since 2.0
 */
public final class FileLocator {
    /**
     * A typical <em>builder</em> implementation for creating {@code FileLocator} objects. An instance of this class is
     * returned by the {@code fileLocator()} method of {link FileLocatorUtils}. It can be used to define the various
     * components of the {@code FileLocator} object. By calling {@code create()} the new immutable {@code FileLocator}
     * instance is created.
     */
    public static final class FileLocatorBuilder {
        /** The base path. */
        private String basePath;

        /** The encoding. */
        private String encoding;

        /** The file name. */
        private String fileName;

        /** The file system. */
        private FileSystem fileSystem;

        /** The location strategy. */
        private FileLocationStrategy locationStrategy;

        /** The URL. */
        private URL sourceURL;

        /** The URL connection options. */
        private URLConnectionOptions urlConnectionOptions;

        /**
         * Creates a new instance of {@code FileLocatorBuilder} and initializes the builder's properties from the passed in
         * {@code FileLocator} object.
         *
         * @param src the source {@code FileLocator} (may be <b>null</b>)
         */
        FileLocatorBuilder(final FileLocator src) {
            if (src != null) {
                initBuilder(src);
            }
        }

        /**
         * Specifies the base path of the new {@code FileLocator}.
         *
         * @param path the base path
         * @return a reference to this builder for method chaining
         */
        public FileLocatorBuilder basePath(final String path) {
            basePath = path;
            return this;
        }

        /**
         * Creates a new immutable {@code FileLocatorImpl} object based on the properties set so far for this builder.
         *
         * @return the newly created {@code FileLocator} object
         */
        public FileLocator create() {
            return new FileLocator(this);
        }

        /**
         * Specifies the encoding of the new {@code FileLocator}.
         *
         * @param enc the encoding
         * @return a reference to this builder for method chaining
         */
        public FileLocatorBuilder encoding(final String enc) {
            encoding = enc;
            return this;
        }

        /**
         * Specifies the file name of the new {@code FileLocator}.
         *
         * @param name the file name
         * @return a reference to this builder for method chaining
         */
        public FileLocatorBuilder fileName(final String name) {
            fileName = name;
            return this;
        }

        /**
         * Specifies the {@code FileSystem} of the new {@code FileLocator}.
         *
         * @param fs the {@code FileSystem}
         * @return a reference to this builder for method chaining
         */
        public FileLocatorBuilder fileSystem(final FileSystem fs) {
            fileSystem = fs;
            return this;
        }

        /**
         * Initializes the properties of this builder from the passed in locator object.
         *
         * @param src the source {@code FileLocator}
         */
        private void initBuilder(final FileLocator src) {
            basePath = src.getBasePath();
            fileName = src.getFileName();
            sourceURL = src.getSourceURL();
            urlConnectionOptions = src.getURLConnectionOptions();
            encoding = src.getEncoding();
            fileSystem = src.getFileSystem();
            locationStrategy = src.getLocationStrategy();
        }

        /**
         * Specifies the {@code FileLocationStrategy} to be used when the referenced file is to be located.
         *
         * @param strategy the {@code FileLocationStrategy}
         * @return a reference to this builder for method chaining
         */
        public FileLocatorBuilder locationStrategy(final FileLocationStrategy strategy) {
            locationStrategy = strategy;
            return this;
        }

        /**
         * Specifies the source URL of the new {@code FileLocator}.
         *
         * @param url the source URL
         * @return a reference to this builder for method chaining
         */
        public FileLocatorBuilder sourceURL(final URL url) {
            this.sourceURL = url;
            return this;
        }

        /**
         * Specifies the source URL connection options of the new {@code FileLocator}.
         *
         * @param urlConnectionOptions the source URL connection options.
         * @return a reference to this builder for method chaining
         */
        public FileLocatorBuilder urlConnectionOptions(final URLConnectionOptions urlConnectionOptions) {
            this.urlConnectionOptions = urlConnectionOptions;
            return this;

        }
    }

    /** The base path. */
    private final String basePath;

    /** The encoding. */
    private final String encoding;

    /** The file name. */
    private final String fileName;

    /** The file system. */
    private final FileSystem fileSystem;

    /** The file location strategy. */
    private final FileLocationStrategy locationStrategy;

    /** The source URL. */
    private final URL sourceURL;

    /** The source URL connection options. */
    private final URLConnectionOptions urlConnectionOptions;

    /**
     * Creates a new instance of {@code FileLocatorImpl} and initializes it from the given builder instance
     *
     * @param builder the builder
     */
    public FileLocator(final FileLocatorBuilder builder) {
        fileName = builder.fileName;
        basePath = builder.basePath;
        sourceURL = builder.sourceURL;
        urlConnectionOptions = builder.urlConnectionOptions;
        encoding = builder.encoding;
        fileSystem = builder.fileSystem;
        locationStrategy = builder.locationStrategy;
    }

    /**
     * Compares this object with another one. Two instances of {@code FileLocatorImpl} are considered equal if all of their
     * properties are equal.
     *
     * @param obj the object to compare to
     * @return a flag whether these objects are equal
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FileLocator)) {
            return false;
        }
        final FileLocator other = (FileLocator) obj;
        return Objects.equals(basePath, other.basePath) && Objects.equals(encoding, other.encoding) && Objects.equals(fileName, other.fileName)
            && Objects.equals(fileSystem, other.fileSystem) && Objects.equals(locationStrategy, other.locationStrategy)
            && Objects.equals(sourceURL, other.sourceURL) && Objects.equals(urlConnectionOptions, other.urlConnectionOptions);
    }

    /**
     * Returns the base path stored in this locator or <b>null</b> if it is undefined.
     *
     * @return the base path
     */
    public String getBasePath() {
        return basePath;
    }

    /**
     * Returns the encoding stored in this locator or <b>null</b> if it is undefined.
     *
     * @return the encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Returns the file name stored in this locator or <b>null</b> if it is undefined.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns the {@code FileSystem} to be used for accessing the file referenced by this locator or <b>null</b> if it is
     * undefined.
     *
     * @return the {@code FileSystem}
     */
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    /**
     * Returns the {@code FileLocationStrategy} to be used for locating the referenced file. If no specific
     * {@code FileLocationStrategy} has been set, result is <b>null</b>. This means that the default strategy should be
     * used.
     *
     * @return the {@code FileLocationStrategy} to be used
     */
    public FileLocationStrategy getLocationStrategy() {
        return locationStrategy;
    }

    /**
     * Returns the URL pointing to the referenced source file or <b>null</b> if it is undefined.
     *
     * @return the source URL
     */
    public URL getSourceURL() {
        return sourceURL;
    }

    /**
     * Returns the URLConnectionOptions
     *
     * @return the URLConnectionOptions
     */
    public URLConnectionOptions getURLConnectionOptions() {
        return urlConnectionOptions;
    }

    /**
     * Returns a hash code for this object.
     *
     * @return a hash code for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(basePath, encoding, fileName, fileSystem, locationStrategy, sourceURL, urlConnectionOptions);
    }

    @Override
    public String toString() {
        return "FileLocator [basePath=" + basePath + ", encoding=" + encoding + ", fileName=" + fileName + ", fileSystem=" + fileSystem + ", locationStrategy="
            + locationStrategy + ", sourceURL=" + sourceURL + ", urlConnectionOptions=" + urlConnectionOptions + "]";
    }
}
