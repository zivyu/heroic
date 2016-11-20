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

package com.spotify.heroic.cluster;

import java.net.URI;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@AllArgsConstructor
class ClusterNodeConnectionStatus {
    private boolean isConnected;
    private boolean isError;
    private ClusterNode clusterNode;
    private URI uri;
    private Throwable error;

    public ClusterNodeConnectionStatus() {
        isConnected = false;
        isError = false;
        clusterNode = null;
        uri = null;
        error = null;
    }

    public static ClusterNodeConnectionStatus error(URI _uri, Throwable _throwable) {
        return new ClusterNodeConnectionStatus(false, true, null, _uri, _throwable);
    }

    public static ClusterNodeConnectionStatus ok(URI _uri, ClusterNode _node) {
        return new ClusterNodeConnectionStatus(true, false, _node, _uri, null);
    }
}

