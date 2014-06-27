package com.spotify.heroic.metrics;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.spotify.heroic.statistics.MetricBackendManagerReporter;

public class MetricBackendManagerTest {
    @Mock
    private MetricBackendManagerReporter reporter;
    @Mock
    private List<MetricBackend> metricBackends;

    private static final long MAGNITUDE = 42;
    private static final boolean UPDATE_METADATA = false;

    private MetricBackendManager manager;

    @Before
    public void before() {
        this.manager = new MetricBackendManager(reporter, metricBackends,
                MAGNITUDE, UPDATE_METADATA);
    }

    @Test(expected = MetricQueryException.class)
    public void testQueryMustBeDefined() throws MetricQueryException {
        manager.queryMetrics(null);
    }
}