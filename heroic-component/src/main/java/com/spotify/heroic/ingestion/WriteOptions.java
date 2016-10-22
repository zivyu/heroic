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

package com.spotify.heroic.ingestion;

import com.spotify.heroic.metric.Tracing;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Optional;

@Data
public class WriteOptions {
    public static final Tracing DEFAULT_TRACING = Tracing.disabled();

    public static final WriteOptions DEFAULTS = builder().build();

    /**
     * Indicates if tracing is enabled.
     * <p>
     * Traces queries will include a {@link com.spotify.heroic.metric.QueryTrace} object that
     * indicates detailed timings of the request.
     */
    private final Tracing tracing;

    public static WriteOptions defaults() {
        return DEFAULTS;
    }

    public static Builder builder() {
        return new Builder();
    }

    @AllArgsConstructor
    public static class Builder {
        private Optional<Tracing> tracing = Optional.empty();

        public Builder() {
        }

        public Builder tracing(Tracing tracing) {
            this.tracing = Optional.of(tracing);
            return this;
        }

        public WriteOptions build() {
            final Tracing tracing = this.tracing.orElse(DEFAULT_TRACING);
            return new WriteOptions(tracing);
        }
    }
}
