package com.donutorders.libs.hikari.metrics.dropwizard;

import com.codahale.metrics.MetricRegistry;
import com.donutorders.libs.hikari.metrics.IMetricsTracker;
import com.donutorders.libs.hikari.metrics.MetricsTrackerFactory;
import com.donutorders.libs.hikari.metrics.PoolStats;

public final class CodahaleMetricsTrackerFactory implements MetricsTrackerFactory {
   private final MetricRegistry registry;

   public CodahaleMetricsTrackerFactory(MetricRegistry registry) {
      this.registry = registry;
   }

   public MetricRegistry getRegistry() {
      return this.registry;
   }

   @Override
   public IMetricsTracker create(String poolName, PoolStats poolStats) {
      return new CodaHaleMetricsTracker(poolName, poolStats, this.registry);
   }
}
