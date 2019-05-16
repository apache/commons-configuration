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
package org.apache.commons.configuration2.sync;

import static org.junit.Assert.assertEquals;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import org.easymock.EasyMock;
import org.junit.Test;

/**
 * Test class for {@code ReadWriteSynchronizer}.
 *
 */
public class TestReadWriteSynchronizer
{
    /** Constant for the total amount of money in the system. */
    private static final long TOTAL_MONEY = 1000000L;

    /**
     * Tests whether a lock passed to the constructor is used.
     */
    @Test
    public void testInitLock()
    {
        final ReadWriteLock lock = EasyMock.createMock(ReadWriteLock.class);
        final Lock readLock = EasyMock.createMock(Lock.class);
        EasyMock.expect(lock.readLock()).andReturn(readLock);
        readLock.lock();
        EasyMock.replay(lock, readLock);
        final ReadWriteSynchronizer sync = new ReadWriteSynchronizer(lock);
        sync.beginRead();
        EasyMock.verify(lock, readLock);
    }

    /**
     * Tests whether the synchronizer is reentrant. This is important for some
     * combined operations on a configuration.
     */
    @Test
    public void testReentrance()
    {
        final Synchronizer sync = new ReadWriteSynchronizer();
        sync.beginWrite();
        sync.beginRead();
        sync.beginRead();
        sync.endRead();
        sync.endRead();
        sync.beginWrite();
        sync.endWrite();
        sync.endWrite();
    }

    /**
     * Performs a test of the synchronizer based on the classic example of
     * account objects. Money is transferred between two accounts. If everything
     * goes well, the total amount of money stays constant over time.
     */
    @Test
    public void testSynchronizerInAction() throws InterruptedException
    {
        final int numberOfUpdates = 10000;
        final int numberOfReads = numberOfUpdates / 2;
        final int readThreadCount = 3;
        final int updateThreadCount = 2;

        final Synchronizer sync = new ReadWriteSynchronizer();
        final Account account1 = new Account();
        final Account account2 = new Account();
        account1.change(TOTAL_MONEY / 2);
        account2.change(TOTAL_MONEY / 2);

        final UpdateThread[] updateThreads = new UpdateThread[updateThreadCount];
        for (int i = 0; i < updateThreads.length; i++)
        {
            updateThreads[i] =
                    new UpdateThread(sync, numberOfUpdates, account1, account2);
            updateThreads[i].start();
        }
        final ReaderThread[] readerThreads = new ReaderThread[readThreadCount];
        for (int i = 0; i < readerThreads.length; i++)
        {
            readerThreads[i] =
                    new ReaderThread(sync, numberOfReads, account1, account2);
            readerThreads[i].start();
        }

        for (final UpdateThread t : updateThreads)
        {
            t.join();
        }
        for (final ReaderThread t : readerThreads)
        {
            t.join();
            assertEquals("Got read errors", 0, t.getErrors());
        }
        sync.beginRead();
        assertEquals("Wrong sum of money", TOTAL_MONEY,
                sumUpAccounts(account1, account2));
        sync.endRead();
    }

    /**
     * Helper method to calculate the sum over all accounts.
     *
     * @param accounts the accounts to check
     * @return the sum of the money on these accounts
     */
    private static long sumUpAccounts(final Account... accounts)
    {
        long sum = 0;
        for (final Account acc : accounts)
        {
            sum += acc.getAmount();
        }
        return sum;
    }

    /**
     * A class representing an account.
     */
    private static class Account
    {
        /** The amount stored in this account. */
        private long amount;

        /**
         * Returns the amount of money stored in this account.
         *
         * @return the amount
         */
        public long getAmount()
        {
            return amount;
        }

        /**
         * Changes the amount of money by the given delata.
         *
         * @param delta the delta
         */
        public void change(final long delta)
        {
            amount += delta;
        }
    }

    /**
     * A thread which performs a number of read operations on the bank's
     * accounts and checks whether the amount of money is consistent.
     */
    private static class ReaderThread extends Thread
    {
        /** The acounts to monitor. */
        private final Account[] accounts;

        /** The synchronizer object. */
        private final Synchronizer sync;

        /** The number of read operations. */
        private final int numberOfReads;

        /** Stores errors detected on read operations. */
        private volatile int errors;

        /**
         * Creates a new instance of {@code ReaderThread}.
         *
         * @param s the synchronizer to be used
         * @param readCount the number of read operations
         * @param accs the accounts to monitor
         */
        public ReaderThread(final Synchronizer s, final int readCount, final Account... accs)
        {
            accounts = accs;
            sync = s;
            numberOfReads = readCount;
        }

        /**
         * Performs the given number of read operations.
         */
        @Override
        public void run()
        {
            for (int i = 0; i < numberOfReads; i++)
            {
                sync.beginRead();
                final long sum = sumUpAccounts(accounts);
                sync.endRead();
                if (sum != TOTAL_MONEY)
                {
                    errors++;
                }
            }
        }

        /**
         * Returns the number of errors occurred during read operations.
         *
         * @return the number of errors
         */
        public int getErrors()
        {
            return errors;
        }
    }

    /**
     * A test thread for updating account objects. This thread executes a number
     * of transactions on two accounts. Each transaction determines the account
     * containing more money. Then a random number of money is transferred from
     * this account to the other one.
     */
    private static class UpdateThread extends Thread
    {
        /** The synchronizer. */
        private final Synchronizer sync;

        /** Account 1. */
        private final Account account1;

        /** Account 2. */
        private final Account account2;

        /** An object for creating random numbers. */
        private final Random random;

        /** The number of transactions. */
        private final int numberOfUpdates;

        /**
         * Creates a new instance of {@code UpdateThread}.
         *
         * @param s the synchronizer
         * @param updateCount the number of updates
         * @param ac1 account 1
         * @param ac2 account 2
         */
        public UpdateThread(final Synchronizer s, final int updateCount, final Account ac1,
                final Account ac2)
        {
            sync = s;
            account1 = ac1;
            account2 = ac2;
            numberOfUpdates = updateCount;
            random = new Random();
        }

        /**
         * Performs the given number of update transactions.
         */
        @Override
        public void run()
        {
            for (int i = 0; i < numberOfUpdates; i++)
            {
                sync.beginWrite();
                Account acSource;
                Account acDest;
                if (account1.getAmount() < account2.getAmount())
                {
                    acSource = account1;
                    acDest = account2;
                }
                else
                {
                    acSource = account2;
                    acDest = account1;
                }
                final long x =
                        Math.round(random.nextDouble()
                                * (acSource.getAmount() - 1)) + 1;
                acSource.change(-x);
                acDest.change(x);
                sync.endWrite();
            }
        }
    }
}
