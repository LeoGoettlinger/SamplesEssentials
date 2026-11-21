package net.coreprotect.hikari.metrics.dropwizard;

import com.codahale.metrics.MetricRegistry;
import net.coreprotect.hikari.metrics.IMetricsTracker;
import net.coreprotect.hikari.metrics.MetricsTrackerFactory;
import net.coreprotect.hikari.metrics.PoolStats;

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
