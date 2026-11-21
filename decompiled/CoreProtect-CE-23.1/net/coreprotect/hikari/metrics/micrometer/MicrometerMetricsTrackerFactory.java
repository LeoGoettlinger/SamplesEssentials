package net.coreprotect.hikari.metrics.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import net.coreprotect.hikari.metrics.IMetricsTracker;
import net.coreprotect.hikari.metrics.MetricsTrackerFactory;
import net.coreprotect.hikari.metrics.PoolStats;

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
