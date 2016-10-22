/*
 * Copyright (c) 2015 Spotify AB.
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

package com.spotify.heroic.metric;

import com.google.common.collect.ImmutableList;
import com.spotify.heroic.cluster.ClusterShard;
import com.spotify.heroic.common.Series;
import eu.toolchain.async.Collector;
import eu.toolchain.async.Transform;
import lombok.Data;

import java.util.List;

@Data
public class WriteMetric {
    private final List<RequestError> errors;
    private final QueryTrace trace;

    public static WriteMetric of(final QueryTrace trace) {
        return new WriteMetric(ImmutableList.of(), trace);
    }

    public static WriteMetric error(final RequestError error, final QueryTrace trace) {
        return new WriteMetric(ImmutableList.of(error), trace);
    }

    public static Transform<Throwable, WriteMetric> shardError(
        final ClusterShard c, final QueryTrace.NamedWatch watch
    ) {
        return e -> new WriteMetric(ImmutableList.of(ShardError.fromThrowable(c, e)), watch.end());
    }

    public static Collector<WriteMetric, WriteMetric> reduce(final QueryTrace.NamedWatch watch) {
        return results -> {
            final ImmutableList.Builder<RequestError> errors = ImmutableList.builder();
            final QueryTrace.Joiner joiner = watch.joiner();

            for (final WriteMetric r : results) {
                errors.addAll(r.getErrors());
                joiner.addChild(r.getTrace());
            }

            return new WriteMetric(errors.build(), joiner.result());
        };
    }

    @Data
    public static class Request {
        private final Tracing tracing;
        private final Series series;
        private final MetricCollection data;
    }
}
