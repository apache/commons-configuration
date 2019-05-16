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

import java.util.Map;

/**
 * Some FileSystems allow options to be passed on File operations. Users of commons
 * configuration can implement this interface and register it with the FileSystem.
 * @since 1.7
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons Configuration team</a>
 */
public interface FileOptionsProvider
{
    /**
     * Key used to identify the user to be associated with the current file operations.
     * The value associated with this key is a String identifying the current user.
     */
    String CURRENT_USER = "currentUser";

    /**
     * Key used to indicate whether Webdav versioning support should be enabled.
     * The value associated with this key is a Boolean where True indicates versioning should
     * be enabled.
     */
    String VERSIONING = "versioning";

    /**
     * Key used to identify the proxy host to connect through.
     * The value associated with this key is a String identifying the host name of the proxy.
     */
    String PROXY_HOST = "proxyHost";

    /**
     * Key used to identify the proxy port to connect through.
     * The value associated with this key is an Integer identifying the port on the proxy.
     */
    String PROXY_PORT = "proxyPort";

    /**
     * Key used to identify the maximum number of connections allowed to a single host.
     * The value associated with this key is an Integer identifying the maximum number of
     * connections allowed to a single host.
     */
    String MAX_HOST_CONNECTIONS = "maxHostConnections";

    /**
     * Key used to identify the maximum number of connections allowed to all hosts.
     * The value associated with this key is an Integer identifying the maximum number of
     * connections allowed to all hosts.
     */
    String MAX_TOTAL_CONNECTIONS = "maxTotalConnections";

    /**
     *
     * @return Options to be used for this file.
     */
    Map<String, Object> getOptions();
}
