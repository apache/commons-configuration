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
package org.apache.commons.configuration2.fs;

import java.net.URL;

/**
 * <p>
 * A straightforward implementation of the {@code Locator} interface that is
 * initialized with the URLs to be returned by the {@code getURL()} method.
 * </p>
 * <p>
 * An instance of this class can be initialized with either a single URL or a
 * URL for input and a URL for output. The {@code getURL()} method returns these
 * URLs directly - no additional transformation is performed.
 * </p>
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public class URLLocator implements Locator
{
    /** Constant for the pattern for the toString() method. */
    private static final String TOSTR_PATTERN = "URLLocator [ inputURL = %s, outputURL = %s ]";

    /** The URL to be used for input. */
    private final URL inputURL;

    /** The URL to be used for output. */
    private final URL outputURL;

    /**
     * Creates a new instance of {@code URLLocator} with a single URL. This URL
     * is used as both input and output URL.
     *
     * @param url the single URL for this {@code URLLocator} (must not be
     *        <b>null</b>)
     * @throws IllegalArgumentException if the URL is <b>null</b>
     */
    public URLLocator(URL url)
    {
        this(url, url);
    }

    /**
     * Creates a new instance of {@code URLLocator} with an input and an output
     * URL. The input URL is returned by {@code getURL()} if the parameter
     * <b>false</b> is passed in. The output URL is returned by {@code getURL()}
     * if the parameter <b>true</b> is passed in.
     *
     * @param urlInput the input URL for this {@code URLLocator} (must not be
     *        <b>null</b>)
     * @param urlOutput the output URL for this {@code URLLocator} (must not be
     *        <b>null</b>)
     * @throws IllegalArgumentException if one of the URLs is <b>null</b>
     */
    public URLLocator(URL urlInput, URL urlOutput)
    {
        if (urlInput == null || urlOutput == null)
        {
            throw new IllegalArgumentException(
                    "URLs for URLLocator must not be null!");
        }

        inputURL = urlInput;
        outputURL = urlOutput;
    }

    /**
     * Returns the URL this locator points to. This implementation returns
     * either the input or the output URL, depending on the passed in flag.
     *
     * @param output flag for input or output
     * @return the corresponding URL
     */
    public URL getURL(boolean output)
    {
        return output ? outputURL : inputURL;
    }

    /**
     * Compares this object with another one. Two instances of {@code
     * URLLocator} are considered equal if they refer to the same URLs.
     *
     * @param obj the object to compare to
     * @return a flag whether these objects are equal
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof URLLocator))
        {
            return false;
        }

        URLLocator c = (URLLocator) obj;
        return equalsURLs(inputURL, c.inputURL)
                && equalsURLs(outputURL, c.outputURL);
    }

    /**
     * Returns a hash code for this object.
     *
     * @return a hash code
     */
    @Override
    public int hashCode()
    {
        final int factor = 31;
        int result = 17;
        result = factor * result + hashURL(inputURL);
        result = factor * result + hashURL(outputURL);
        return result;
    }

    /**
     * Returns a string representation of this object. This string contains both
     * the input and the output URL (which may be the same).
     *
     * @return a string for this object
     */
    @Override
    public String toString()
    {
        return String.format(TOSTR_PATTERN, inputURL.toExternalForm(),
                outputURL.toExternalForm());
    }

    /**
     * Helper method for comparing two URLs. As the equals() method of the URL
     * class performs a network access, we only compare the external form of the
     * URLs.
     *
     * @param url1 URL 1
     * @param url2 URL 2
     * @return a flag whether these URLs are equal
     */
    private static boolean equalsURLs(URL url1, URL url2)
    {
        return url1.toExternalForm().equals(url2.toExternalForm());
    }

    /**
     * Calculates a hash code for the specified URL. To be consistent with the
     * equals() implementation the hash code is determined using the external
     * form.
     *
     * @param url the URL
     * @return a hash code for this URL
     */
    private static int hashURL(URL url)
    {
        return url.toExternalForm().hashCode();
    }
}
