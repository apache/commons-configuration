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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.configuration2.ConfigurationAssert;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code CombinedLocationStrategy}.
 *
 */
public class TestCombinedLocationStrategy {
    /** A test locator. */
    private static FileLocator locator;

    /** A URL indicating a successful locate() operation. */
    private static URL locateURL;

    @BeforeAll
    public static void setUpOnce() throws Exception {
        locator = FileLocatorUtils.fileLocator().fileName("testFile.tst").create();
        locateURL = ConfigurationAssert.getTestURL("test.xml");
    }

    /** A mock for the file system. */
    private FileSystem fileSystem;

    /** An array with mock sub strategies. */
    private FileLocationStrategy[] subStrategies;

    /**
     * Checks whether the passed in combined strategy contains the expected sub strategies.
     *
     * @param strategy the combined strategy to check
     */
    private void checkSubStrategies(final CombinedLocationStrategy strategy) {
        final Collection<FileLocationStrategy> subs = strategy.getSubStrategies();
        assertEquals(getSubStrategies().length, subs.size(), "Wrong number of strategies");
        int idx = 0;
        for (final FileLocationStrategy strat : subs) {
            assertEquals(getSubStrategies()[idx++], strat, "Wrong sub strategy at " + idx);
        }
    }

    /**
     * Helper method for creating a combined strategy with the mock sub strategies.
     *
     * @return the newly created combined strategy
     */
    private CombinedLocationStrategy createCombinedStrategy() {
        return new CombinedLocationStrategy(Arrays.asList(getSubStrategies()));
    }

    /**
     * Returns the mock file system. It is created on demand.
     *
     * @return the mock file system
     */
    private FileSystem getFileSystem() {
        if (fileSystem == null) {
            fileSystem = EasyMock.createMock(FileSystem.class);
            EasyMock.replay(fileSystem);
        }
        return fileSystem;
    }

    /**
     * Returns an array with mock objects for sub strategies.
     *
     * @return the array with mock strategies
     */
    private FileLocationStrategy[] getSubStrategies() {
        if (subStrategies == null) {
            subStrategies = new FileLocationStrategy[2];
            for (int i = 0; i < subStrategies.length; i++) {
                subStrategies[i] = EasyMock.createMock(FileLocationStrategy.class);
            }
        }
        return subStrategies;
    }

    /**
     * Replays the mock objects for the sub strategies.
     */
    private void replaySubStrategies() {
        EasyMock.replay((Object[]) getSubStrategies());
    }

    /**
     * Tests that the collection with sub strategies cannot be modified.
     */
    @Test
    public void testGetSubStrategiesModify() {
        final CombinedLocationStrategy strategy = createCombinedStrategy();
        final Collection<FileLocationStrategy> strategies = strategy.getSubStrategies();
        assertThrows(UnsupportedOperationException.class, strategies::clear);
    }

    /**
     * Tries to create an instance containing a null element.
     */
    @Test
    public void testInitCollectionWithNullEntries() {
        final Collection<FileLocationStrategy> col = new LinkedList<>(Arrays.asList(getSubStrategies()));
        col.add(null);
        assertThrows(IllegalArgumentException.class, () -> new CombinedLocationStrategy(col));
    }

    /**
     * Tests whether a defensive copy of the collection with sub strategies is made.
     */
    @Test
    public void testInitDefensiveCopy() {
        final Collection<FileLocationStrategy> col = new LinkedList<>(Arrays.asList(getSubStrategies()));
        final CombinedLocationStrategy strategy = new CombinedLocationStrategy(col);
        col.add(EasyMock.createMock(FileLocationStrategy.class));
        checkSubStrategies(strategy);
    }

    /**
     * Tries to create an instance with a null collection.
     */
    @Test
    public void testInitNullCollection() {
        assertThrows(IllegalArgumentException.class, () -> new CombinedLocationStrategy(null));
    }

    /**
     * Tests a failed locate() operation.
     */
    @Test
    public void testLocateFailed() {
        EasyMock.expect(getSubStrategies()[0].locate(getFileSystem(), locator)).andReturn(null);
        EasyMock.expect(getSubStrategies()[1].locate(getFileSystem(), locator)).andReturn(null);
        replaySubStrategies();
        final CombinedLocationStrategy strategy = createCombinedStrategy();
        assertNull(strategy.locate(getFileSystem(), locator), "Got a URL");
        verifySubStrategies();
    }

    /**
     * Tests a successful locate() operation if the first sub strategy can locate the file.
     */
    @Test
    public void testLocateSuccessFirstSubStrategy() {
        EasyMock.expect(getSubStrategies()[0].locate(getFileSystem(), locator)).andReturn(locateURL);
        replaySubStrategies();
        final CombinedLocationStrategy strategy = createCombinedStrategy();
        assertSame(locateURL, strategy.locate(getFileSystem(), locator), "Wrong result");
        verifySubStrategies();
    }

    /**
     * Tests a successful locate() operation if the 2nd sub strategy can locate the file.
     */
    @Test
    public void testLocateSuccessSecondSubStrategy() {
        EasyMock.expect(getSubStrategies()[0].locate(getFileSystem(), locator)).andReturn(null);
        EasyMock.expect(getSubStrategies()[1].locate(getFileSystem(), locator)).andReturn(locateURL);
        replaySubStrategies();
        final CombinedLocationStrategy strategy = createCombinedStrategy();
        assertSame(locateURL, strategy.locate(getFileSystem(), locator), "Wrong result");
        verifySubStrategies();
    }

    /**
     * Verifies the mock objects for the sub strategies.
     */
    private void verifySubStrategies() {
        EasyMock.verify((Object[]) getSubStrategies());
    }
}
