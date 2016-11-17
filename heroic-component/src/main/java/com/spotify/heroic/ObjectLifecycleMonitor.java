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
import eu.toolchain.async.ResolvableFuture;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Data
public class ObjectLifecycleMonitor extends Thread {
    private final CountDownLatch pleaseStop = new CountDownLatch(1);
    protected final ResolvableFuture<Void> hasStopped;

    ConcurrentHashMap<WeakReference<Object>, ObjectMetadata> objectRegistry;
    ReferenceQueue<? super WeakReference<Object>> deadReferenceQueue;

    public void registerObject(Object object, String description, long numBytes) {

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

            m.setNoLongerReferencedTime(System.currentTimeMillis());
        }
        hasStopped.resolve(null);
    }

    void periodicLogDump() {
        Enumeration<ObjectMetadata> e = objectRegistry.elements();
        log.info("Periodic dump of tracked in-memory objects: num:" + objectRegistry.size());
        while (e.hasMoreElements()) {
            ObjectMetadata m = e.nextElement();
            log.info("  " + m.getDescription() + ": ");
        }
    }

    @Data
    class ObjectMetadata {
        private String description;
        private long numBytes;
        private long creationTime;
        private long noLongerReferencedTime;
        private long finalizedTime;
    }
}
