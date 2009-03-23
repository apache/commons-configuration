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

import java.io.OutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Wraps the output stream so errors can be detected in the HTTP response.
 * @since 1.7
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons Configuration team</a>
 */
public class HttpOutputStream extends VerifiableOutputStream
{
    /** The wrapped OutputStream */
    private final OutputStream stream;

    /** The HttpURLConnection */
    private final HttpURLConnection connection;

    public HttpOutputStream(OutputStream stream, HttpURLConnection connection)
    {
        this.stream = stream;
        this.connection = connection;
    }

    public void write(byte[] bytes) throws IOException
    {
        stream.write(bytes);
    }

    public void write(byte[] bytes, int i, int i1) throws IOException
    {
        stream.write(bytes, i, i1);
    }

    public void flush() throws IOException
    {
        stream.flush();
    }

    public void close() throws IOException
    {
        stream.close();
    }

    public void write(int i) throws IOException
    {
        stream.write(i);
    }

    public String toString()
    {
        return stream.toString();
    }

    public void verify() throws IOException
    {
        if (connection.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST)
        {
            throw new IOException("HTTP Error " + connection.getResponseCode()
                    + " " + connection.getResponseMessage());
        }
    }
}
