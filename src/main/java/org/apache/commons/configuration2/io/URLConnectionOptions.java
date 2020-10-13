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

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

/**
 * Carries options and operates on {@link URLConnection}.
 *
 * @since 2.8.0
 */
public final class URLConnectionOptions {

    /**
     * Default options.
     */
    public static final URLConnectionOptions DEFAULT = new URLConnectionOptions();

    private boolean allowUserInteraction;

    private int connectTimeoutMillis;

    private int readTimeoutMillis;

    private boolean useCaches = true;

    /**
     * Constructs a new default instance.
     */
    public URLConnectionOptions() {
        // Defaults initialized in declarations.
    }

    /**
     * Constructs an instance with values from the given URLConnectionOptions.
     *
     * @param urlConnectionOptions the source
     */
    public URLConnectionOptions(final URLConnectionOptions urlConnectionOptions) {
        this.allowUserInteraction = urlConnectionOptions.getAllowUserInteraction();
        this.useCaches = urlConnectionOptions.getUseCaches();
        this.connectTimeoutMillis = urlConnectionOptions.getConnectTimeoutMillis();
        this.readTimeoutMillis = urlConnectionOptions.getReadTimeoutMillis();
    }

    /**
     * Applies the options to the given connection.
     *
     * @param urlConnection the target connection.
     * @return the given connection.
     */
    public URLConnection apply(final URLConnection urlConnection) {
        urlConnection.setUseCaches(useCaches);
        urlConnection.setConnectTimeout(connectTimeoutMillis);
        urlConnection.setReadTimeout(readTimeoutMillis);
        return urlConnection;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof URLConnectionOptions)) {
            return false;
        }
        final URLConnectionOptions other = (URLConnectionOptions) obj;
        return allowUserInteraction == other.allowUserInteraction && connectTimeoutMillis == other.connectTimeoutMillis
            && readTimeoutMillis == other.readTimeoutMillis && useCaches == other.useCaches;
    }

    /**
     * Gets whether to allow user interaction.
     *
     * @return whether to allow user interaction.
     */
    public boolean getAllowUserInteraction() {
        return allowUserInteraction;
    }

    /**
     * Gets the connect timeout.
     *
     * @return the connect timeout.
     */
    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    /**
     * Gets the read timeout.
     *
     * @return the read timeout.
     */
    public int getReadTimeoutMillis() {
        return readTimeoutMillis;
    }

    /**
     * Whether to cache.
     *
     * @return Whether to cache.
     */
    public boolean getUseCaches() {
        return useCaches;
    }

    @Override
    public int hashCode() {
        return Objects.hash(allowUserInteraction, connectTimeoutMillis, readTimeoutMillis, useCaches);
    }

    /**
     * Opens a connection for the given URL with our options.
     *
     * @param url the URL to open
     * @return A new connection
     * @throws IOException if an I/O exception occurs.
     */
    public URLConnection openConnection(final URL url) throws IOException {
        return apply(url.openConnection());
    }

    public URLConnectionOptions setAllowUserInteraction(final boolean allowUserInteraction) {
        this.allowUserInteraction = allowUserInteraction;
        return this;
    }

    public URLConnectionOptions setConnectTimeoutMillis(final int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
        return this;
    }

    public URLConnectionOptions setReadTimeoutMillis(final int readTimeoutMillis) {
        this.readTimeoutMillis = readTimeoutMillis;
        return this;
    }

    public URLConnectionOptions setUseCaches(final boolean useCaches) {
        this.useCaches = useCaches;
        return this;
    }

    @Override
    public String toString() {
        return "URLConnectionOptions [allowUserInteraction=" + allowUserInteraction + ", connectTimeoutMillis="
            + connectTimeoutMillis + ", readTimeoutMillis=" + readTimeoutMillis + ", useCaches=" + useCaches + "]";
    }
}