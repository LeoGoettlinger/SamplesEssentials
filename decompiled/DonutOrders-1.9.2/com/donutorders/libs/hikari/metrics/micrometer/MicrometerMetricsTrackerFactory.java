package com.donutorders.libs.hikari.metrics.micrometer;

import com.donutorders.libs.hikari.metrics.IMetricsTracker;
import com.donutorders.libs.hikari.metrics.MetricsTrackerFactory;
import com.donutorders.libs.hikari.metrics.PoolStats;
import io.micrometer.core.instrument.MeterRegistry;

public class MicrometerMetricsTrackerFactory implements MetricsTrackerFactory {
   private final MeterRegistry registry;

   public MicrometerMetricsTrackerFactory(MeterRegistry registry) {
      this.registry = registry;
   }

   @Override
   public IMetricsTracker create(String poolName, PoolStats poolStats) {
      return new MicrometerMetricsTracker(poolName, poolStats, this.registry);
   }
}
