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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.io.TempDir;

/**
 * A utility class to make working with {@link TempDir} easier.
 * It encapsulates some of the functionality from JUnit 4's {@code TemporaryFolder} class.
 */
public final class TempDirUtils {

    private static final String TMP_PREFIX = "junit";

    /**
     * Returns a new fresh file with a random name under a temporary folder.
     *
     * @param tempFolder the temporary folder to create the file under
     * @return the created file
     * @throws IOException if an error occurs
     */
    public static File newFile(final File tempFolder) throws IOException {
        return Files.createTempFile(tempFolder.toPath(), TMP_PREFIX, null).toFile();
    }

    /**
     * Returns a new fresh file with the given name under a temporary folder.
     *
     * @param tempFolder the temporary folder to create the file under
     * @return the created file
     * @throws IOException if an error occurs
     */
    public static File newFile(final String fileName, final File tempFolder) throws IOException {
        return Files.createFile(tempFolder.toPath().resolve(fileName)).toFile();
    }

    /**
     * Returns a new fresh folder with a random name under a temporary folder.
     *
     * @param tempFolder the temporary folder to create the folder under
     * @return the created folder
     * @throws IOException if an error occurs
     */
    public static File newFolder(final File tempFolder) throws IOException {
        return Files.createTempDirectory(tempFolder.toPath(), TMP_PREFIX).toFile();
    }

    /**
     * Returns a new fresh folder with the given path under a temporary folder.
     *
     * @param tempFolder the temporary folder to create the folder under
     * @return the created folder
     * @throws IOException if an error occurs
     */
    public static File newFolder(final String path, final File tempFolder) throws IOException {
        return Files.createDirectory(tempFolder.toPath().resolve(path)).toFile();
    }

    private TempDirUtils() {
    }
}
