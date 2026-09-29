/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.configuration2.io;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.configuration2.ex.ConfigurationDeniedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests {@link AbstractFileLocationStrategy}.
 */
public class TestAbstractFileLocationStrategy {

    private static Set<Pattern> hosts(final String... regexes) {
        final LinkedHashSet<Pattern> set = new LinkedHashSet<>();
        for (final String r : regexes) {
            set.add(Pattern.compile(r, Pattern.CASE_INSENSITIVE));
        }
        return set;
    }

    // Bypasses the validation of the single-arg constructor
    private static URL jarUrl(final String spec) throws Exception {
        return new URL("jar", null, spec);
    }

    private static Set<String> schemes(final String... values) {
        return new LinkedHashSet<>(Arrays.asList(values));
    }

    static Stream<Arguments> testCheckUrlAccepts() throws Exception {
        return Stream.of(
                // Empty scheme allows all.
                Arguments.of(url("file:/tmp/x.properties"), schemes(), hosts()),
                Arguments.of(url("https://example.com/x.properties"), schemes(), hosts()),
                // Bare schemes that match the allow-set.
                Arguments.of(url("file:/tmp/x.properties"), schemes("file"), hosts()),
                Arguments.of(url("https://example.com/x.properties"), schemes("https"), hosts()),
                // jar: unwraps to the inner scheme, which is in the allow-set.
                Arguments.of(url("jar:file:/tmp/x.jar!/y.properties"), schemes("file", "jar"), hosts()),
                Arguments.of(url("jar:https://example.com/x.jar!/y.properties"), schemes("https", "jar"), hosts()),
                // Empty host allow-set means "any host".
                Arguments.of(url("file:///tmp/x.properties"), schemes("file"), hosts()),
                Arguments.of(url("http://anything.example/x.properties"), schemes("http"), hosts()),
                Arguments.of(url("jar:https://anything.example/x.jar!/y.properties"), schemes("https", "jar"), hosts()),
                // Host satisfies allow-set
                Arguments.of(url("file:///tmp/x.properties"), schemes("file"), hosts("trusted\\.example")),
                Arguments.of(url("https://trusted.example/x.properties"), schemes("https", "jar"), hosts("trusted\\.example")),
                Arguments.of(url("jar:https://trusted.example/x.jar!/y.properties"), schemes("https", "jar"), hosts("trusted\\.example"))
        );
    }

    static Stream<Arguments> testCheckUrlRejects() throws Exception {
        return Stream.of(
                // Plain scheme not in the allow-set.
                Arguments.of(url("http://example.com/x.properties"), schemes("file", "jar"), hosts()),
                // jar: is allowed but the inner scheme is not.
                Arguments.of(url("jar:file:/tmp/x.jar!/y.properties"), schemes("jar"), hosts()),
                Arguments.of(url("jar:https://example.com/x.jar!/y.properties"), schemes("jar"), hosts()),
                // Invalid jar URL
                Arguments.of(jarUrl("file:/tmp/x.properties"), schemes("file", "jar"), hosts()),
                Arguments.of(jarUrl("invalid url!/y.properties"), schemes("file", "jar"), hosts()),
                // Host is not allowed
                Arguments.of(url("https://evilhost/x.properties"), schemes(), hosts("trusted\\.example")),
                Arguments.of(url("jar:https://evilhost/x.jar!/y.properties"), schemes(), hosts("trusted\\.example"))
        );
    }

    private static URL url(final String spec) throws Exception {
        return new URL(spec);
    }

    @Test
    void testBuilder() {
        assertThrows(NullPointerException.class, () -> new AbstractFileLocationStrategy.StrategyBuilder<>(null));
    }

    @ParameterizedTest
    @MethodSource
    void testCheckUrlAccepts(final URL url, final Set<String> validSchemes, final Set<Pattern> validHosts) {
        assertDoesNotThrow(() -> AbstractFileLocationStrategy.checkUrl(url, validSchemes, validHosts));
    }

    @ParameterizedTest
    @MethodSource
    void testCheckUrlRejects(final URL url, final Set<String> validSchemes, final Set<Pattern> validHosts) {
        assertThrows(ConfigurationDeniedException.class, () -> AbstractFileLocationStrategy.checkUrl(url, validSchemes, validHosts));
    }

}
