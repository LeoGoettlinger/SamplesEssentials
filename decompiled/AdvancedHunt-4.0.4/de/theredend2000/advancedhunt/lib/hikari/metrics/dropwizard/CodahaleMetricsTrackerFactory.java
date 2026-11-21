package de.theredend2000.advancedhunt.lib.hikari.metrics.dropwizard;

import com.codahale.metrics.MetricRegistry;
import de.theredend2000.advancedhunt.lib.hikari.metrics.IMetricsTracker;
import de.theredend2000.advancedhunt.lib.hikari.metrics.MetricsTrackerFactory;
import de.theredend2000.advancedhunt.lib.hikari.metrics.PoolStats;

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
