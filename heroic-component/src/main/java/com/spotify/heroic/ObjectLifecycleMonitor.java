/*
 * Copyright (c) 2016 Spotify AB.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.heroic;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.spotify.heroic.aggregation.AggregationSession;
import com.spotify.heroic.common.Series;
import dagger.Module;
import dagger.Provides;
import eu.toolchain.async.AsyncFramework;
import eu.toolchain.async.ResolvableFuture;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

@Module
@Slf4j
@Data
public class ObjectLifecycleMonitor extends Thread {
    static final boolean LOG_OBJECT_DESTRUCTION = true;
    static final boolean PERIODIC_LOG_OF_STATISTICS = true;
    static final boolean PERIODIC_LOG_OF_FULL_LIST = true;

    private final CountDownLatch pleaseStop = new CountDownLatch(1);

    LongAdder totalByteCount = new LongAdder();
    ConcurrentHashMap<Object, ObjectMetadata> objectRegistry = new ConcurrentHashMap<>();
    ReferenceQueue<Object> deadReferenceQueue = new ReferenceQueue<>();

    public ObjectLifecycleMonitor() {
        start();
    }

    public void registerObject(Object object, String description, long numBytes) {
        ObjectMetadata m = new ObjectMetadata(description, numBytes, System.currentTimeMillis(), 0, 0);

        WeakReference<Object> weakReference = new WeakReference<Object>(object, deadReferenceQueue);
        objectRegistry.put(weakReference, m);

        totalByteCount.add(numBytes);
    }

    public void objectIsFinalized(Object object) {
        log.info("Object is finalized:" + object.toString());
        log.info("Call stack:\n" + Thread.currentThread().getStackTrace().toString());
    }


    public void run() {
        long lastLogTime = System.currentTimeMillis();
        long logInterval = 10000;

        while (pleaseStop.getCount() == 1) {
            if (lastLogTime + logInterval < System.currentTimeMillis()) {
                periodicLogDump();
                lastLogTime = System.currentTimeMillis();
            }

            Object deadObject = deadReferenceQueue.poll();
            if (deadObject == null) {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                }
                continue;
            }

            WeakReference<Object> deadReference = (WeakReference<Object>)deadObject;

            ObjectMetadata m = objectRegistry.get(deadReference);
            if (m == null) {
                continue;
            }

            // An object has no strong references and will be GC:d soon
            m.setNoLongerReferencedTime(System.currentTimeMillis());

            if (LOG_OBJECT_DESTRUCTION) {
                log.info("Object is getting ready for GC:");
                m.dumpToLog();

                log.info("Call stack:\n" + Thread.currentThread().getStackTrace());
                StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                int numPrinted = 0;
                while (numPrinted < stackTraceElements.length && numPrinted < 10) {
                    log.info("  " + stackTraceElements[numPrinted].toString());
                    numPrinted++;
                }
            }

            totalByteCount.add( - m.numBytes);
            objectRegistry.remove(deadObject);
        }
        //hasStopped.resolve(null);
    }

    void periodicLogDump() {
        long currTime = System.currentTimeMillis();
        if (PERIODIC_LOG_OF_STATISTICS) {
            log.info("Tracked in-memory objects: num:" + objectRegistry.size() +
                     " totalBytes:" + totalByteCount.longValue());
        }
        if (PERIODIC_LOG_OF_FULL_LIST) {
            Enumeration<ObjectMetadata> e = objectRegistry.elements();
            while (e.hasMoreElements()) {
                ObjectMetadata m = e.nextElement();
                m.dumpToLog(currTime);
            }
        }
    }

    @AllArgsConstructor
    @Data
    class ObjectMetadata {
        private String description;
        private long numBytes;
        private long creationTime;
        private long noLongerReferencedTime;
        private long finalizedTime;

        public void dumpToLog() {
            dumpToLog(System.currentTimeMillis());
        }

        public void dumpToLog(long currTime) {
            log.info("  \"" + description + "\": bytes:" + numBytes +
                     " lifetime(ms):" + (currTime - creationTime) +
                     " stronglyReferenced:" + (noLongerReferencedTime == 0 ? "true" : "false weakReferenceTime:" + (currTime - noLongerReferencedTime))
            );
        }
    }
}
