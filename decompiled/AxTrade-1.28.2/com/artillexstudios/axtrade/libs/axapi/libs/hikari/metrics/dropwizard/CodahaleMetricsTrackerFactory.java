package com.artillexstudios.axtrade.libs.axapi.libs.hikari.metrics.dropwizard;

import com.artillexstudios.axtrade.libs.axapi.libs.hikari.metrics.IMetricsTracker;
import com.artillexstudios.axtrade.libs.axapi.libs.hikari.metrics.MetricsTrackerFactory;
import com.artillexstudios.axtrade.libs.axapi.libs.hikari.metrics.PoolStats;
import com.codahale.metrics.MetricRegistry;

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
