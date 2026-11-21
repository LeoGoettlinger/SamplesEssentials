package org.sethomegui.SetHomeGUI.libs.hikari.metrics.prometheus;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.sethomegui.SetHomeGUI.libs.hikari.metrics.IMetricsTracker;
import org.sethomegui.SetHomeGUI.libs.hikari.metrics.MetricsTrackerFactory;
import org.sethomegui.SetHomeGUI.libs.hikari.metrics.PoolStats;

public class PrometheusMetricsTrackerFactory implements MetricsTrackerFactory {
   private static final Map<CollectorRegistry, PrometheusMetricsTrackerFactory.RegistrationStatus> registrationStatuses = new ConcurrentHashMap<>();
   private final HikariCPCollector collector = new HikariCPCollector();
   private final CollectorRegistry collectorRegistry;

   public PrometheusMetricsTrackerFactory() {
      this(CollectorRegistry.defaultRegistry);
   }

   public PrometheusMetricsTrackerFactory(CollectorRegistry collectorRegistry) {
      this.collectorRegistry = collectorRegistry;
   }

   @Override
   public IMetricsTracker create(String poolName, PoolStats poolStats) {
      this.registerCollector(this.collector, this.collectorRegistry);
      this.collector.add(poolName, poolStats);
      return new PrometheusMetricsTracker(poolName, this.collectorRegistry, this.collector);
   }

   private void registerCollector(Collector collector, CollectorRegistry collectorRegistry) {
      if (registrationStatuses.putIfAbsent(collectorRegistry, PrometheusMetricsTrackerFactory.RegistrationStatus.REGISTERED) == null) {
         collector.register(collectorRegistry);
      }
   }

   static enum RegistrationStatus {
      REGISTERED;
   }
}
