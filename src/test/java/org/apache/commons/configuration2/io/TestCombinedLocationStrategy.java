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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.configuration2.ConfigurationAssert;
import org.easymock.EasyMock;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for {@code CombinedLocationStrategy}.
 *
 */
public class TestCombinedLocationStrategy
{
    /** A test locator. */
    private static FileLocator locator;

    /** A URL indicating a successful locate() operation. */
    private static URL locateURL;

    /** A mock for the file system. */
    private FileSystem fileSystem;

    /** An array with mock sub strategies. */
    private FileLocationStrategy[] subStrategies;

    @BeforeClass
    public static void setUpOnce() throws Exception
    {
        locator =
                FileLocatorUtils.fileLocator().fileName("testFile.tst")
                        .create();
        locateURL = ConfigurationAssert.getTestURL("test.xml");
    }

    /**
     * Returns the mock file system. It is created on demand.
     *
     * @return the mock file system
     */
    private FileSystem getFileSystem()
    {
        if (fileSystem == null)
        {
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
    private FileLocationStrategy[] getSubStrategies()
    {
        if (subStrategies == null)
        {
            subStrategies = new FileLocationStrategy[2];
            for (int i = 0; i < subStrategies.length; i++)
            {
                subStrategies[i] =
                        EasyMock.createMock(FileLocationStrategy.class);
            }
        }
        return subStrategies;
    }

    /**
     * Replays the mock objects for the sub strategies.
     */
    private void replaySubStrategies()
    {
        EasyMock.replay((Object[]) getSubStrategies());
    }

    /**
     * Verifies the mock objects for the sub strategies.
     */
    private void verifySubStrategies()
    {
        EasyMock.verify((Object[]) getSubStrategies());
    }

    /**
     * Checks whether the passed in combined strategy contains the expected sub
     * strategies.
     *
     * @param strategy the combined strategy to check
     */
    private void checkSubStrategies(final CombinedLocationStrategy strategy)
    {
        final Collection<FileLocationStrategy> subs = strategy.getSubStrategies();
        assertEquals("Wrong number of strategies", getSubStrategies().length,
                subs.size());
        int idx = 0;
        for (final FileLocationStrategy strat : subs)
        {
            assertEquals("Wrong sub strategy at " + idx,
                    getSubStrategies()[idx++], strat);
        }
    }

    /**
     * Helper method for creating a combined strategy with the mock sub
     * strategies.
     *
     * @return the newly created combined strategy
     */
    private CombinedLocationStrategy createCombinedStrategy()
    {
        return new CombinedLocationStrategy(Arrays.asList(getSubStrategies()));
    }

    /**
     * Tries to create an instance with a null collection.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitNullCollection()
    {
        new CombinedLocationStrategy(null);
    }

    /**
     * Tries to create an instance containing a null element.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitCollectionWithNullEntries()
    {
        final Collection<FileLocationStrategy> col =
                new LinkedList<>(
                        Arrays.asList(getSubStrategies()));
        col.add(null);
        new CombinedLocationStrategy(col);
    }

    /**
     * Tests whether a defensive copy of the collection with sub strategies is
     * made.
     */
    @Test
    public void testInitDefensiveCopy()
    {
        final Collection<FileLocationStrategy> col =
                new LinkedList<>(
                        Arrays.asList(getSubStrategies()));
        final CombinedLocationStrategy strategy = new CombinedLocationStrategy(col);
        col.add(EasyMock.createMock(FileLocationStrategy.class));
        checkSubStrategies(strategy);
    }

    /**
     * Tests that the collection with sub strategies cannot be modified.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetSubStrategiesModify()
    {
        final CombinedLocationStrategy strategy = createCombinedStrategy();
        strategy.getSubStrategies().clear();
    }

    /**
     * Tests a successful locate() operation if the first sub strategy can
     * locate the file.
     */
    @Test
    public void testLocateSuccessFirstSubStrategy()
    {
        EasyMock.expect(getSubStrategies()[0].locate(getFileSystem(), locator))
                .andReturn(locateURL);
        replaySubStrategies();
        final CombinedLocationStrategy strategy = createCombinedStrategy();
        assertSame("Wrong result", locateURL,
                strategy.locate(getFileSystem(), locator));
        verifySubStrategies();
    }

    /**
     * Tests a successful locate() operation if the 2nd sub strategy can locate
     * the file.
     */
    @Test
    public void testLocateSuccessSecondSubStrategy()
    {
        EasyMock.expect(getSubStrategies()[0].locate(getFileSystem(), locator))
                .andReturn(null);
        EasyMock.expect(getSubStrategies()[1].locate(getFileSystem(), locator))
                .andReturn(locateURL);
        replaySubStrategies();
        final CombinedLocationStrategy strategy = createCombinedStrategy();
        assertSame("Wrong result", locateURL,
                strategy.locate(getFileSystem(), locator));
        verifySubStrategies();
    }

    /**
     * Tests a failed locate() operation.
     */
    @Test
    public void testLocateFailed()
    {
        EasyMock.expect(getSubStrategies()[0].locate(getFileSystem(), locator))
                .andReturn(null);
        EasyMock.expect(getSubStrategies()[1].locate(getFileSystem(), locator))
                .andReturn(null);
        replaySubStrategies();
        final CombinedLocationStrategy strategy = createCombinedStrategy();
        assertNull("Got a URL", strategy.locate(getFileSystem(), locator));
        verifySubStrategies();
    }
}
