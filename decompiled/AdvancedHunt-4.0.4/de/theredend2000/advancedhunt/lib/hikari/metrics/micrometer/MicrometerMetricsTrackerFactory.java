package de.theredend2000.advancedhunt.lib.hikari.metrics.micrometer;

import de.theredend2000.advancedhunt.lib.hikari.metrics.IMetricsTracker;
import de.theredend2000.advancedhunt.lib.hikari.metrics.MetricsTrackerFactory;
import de.theredend2000.advancedhunt.lib.hikari.metrics.PoolStats;
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
