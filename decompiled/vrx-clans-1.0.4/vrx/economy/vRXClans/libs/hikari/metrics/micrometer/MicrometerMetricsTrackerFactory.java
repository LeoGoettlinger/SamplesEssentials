package vrx.economy.vRXClans.libs.hikari.metrics.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import vrx.economy.vRXClans.libs.hikari.metrics.IMetricsTracker;
import vrx.economy.vRXClans.libs.hikari.metrics.MetricsTrackerFactory;
import vrx.economy.vRXClans.libs.hikari.metrics.PoolStats;

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
