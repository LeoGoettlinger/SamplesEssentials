package com.artillexstudios.axafkzone.libs.axapi.libs.hikari.metrics.dropwizard;

import com.artillexstudios.axafkzone.libs.axapi.libs.hikari.metrics.IMetricsTracker;
import com.artillexstudios.axafkzone.libs.axapi.libs.hikari.metrics.MetricsTrackerFactory;
import com.artillexstudios.axafkzone.libs.axapi.libs.hikari.metrics.PoolStats;
import io.dropwizard.metrics5.MetricRegistry;

public class Dropwizard5MetricsTrackerFactory implements MetricsTrackerFactory {
   private final MetricRegistry registry;

   public Dropwizard5MetricsTrackerFactory(MetricRegistry registry) {
      this.registry = registry;
   }

   public MetricRegistry getRegistry() {
      return this.registry;
   }

   @Override
   public IMetricsTracker create(String poolName, PoolStats poolStats) {
      return new Dropwizard5MetricsTracker(poolName, poolStats, this.registry);
   }
}
