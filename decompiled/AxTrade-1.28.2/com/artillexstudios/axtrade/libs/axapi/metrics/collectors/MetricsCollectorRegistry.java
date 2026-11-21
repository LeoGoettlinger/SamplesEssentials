package com.artillexstudios.axtrade.libs.axapi.metrics.collectors;

import com.artillexstudios.axtrade.libs.axapi.AxPlugin;
import com.artillexstudios.axtrade.libs.axapi.metrics.collectors.implementation.CPUModelMetricsCollector;
import com.artillexstudios.axtrade.libs.axapi.metrics.collectors.implementation.JavaVersionMetricsCollector;
import com.artillexstudios.axtrade.libs.axapi.metrics.collectors.implementation.MinecraftVersionMetricsCollector;
import com.artillexstudios.axtrade.libs.axapi.metrics.collectors.implementation.OnlineModeMetricsCollector;
import com.artillexstudios.axtrade.libs.axapi.metrics.collectors.implementation.OnlinePlayersMetricsCollector;
import com.artillexstudios.axtrade.libs.axapi.metrics.collectors.implementation.OperatingSystemMetricsCollector;
import com.artillexstudios.axtrade.libs.axapi.metrics.collectors.implementation.PluginVersionMetricsCollector;
import com.artillexstudios.axtrade.libs.axapi.metrics.collectors.implementation.ServerSoftwareMetricsCollector;
import com.artillexstudios.axtrade.libs.axapi.utils.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public final class MetricsCollectorRegistry {
   private final List<MetricsCollector> collectors = new ArrayList<>();

   public MetricsCollectorRegistry(AxPlugin plugin) {
      this.register(JavaVersionMetricsCollector::new);
      this.register(MinecraftVersionMetricsCollector::new);
      this.register(OnlinePlayersMetricsCollector::new);
      this.register(() -> new PluginVersionMetricsCollector(plugin));
      this.register(ServerSoftwareMetricsCollector::new);
      this.register(OnlineModeMetricsCollector::new);

      try {
         Object systemInfo = Class.forName("oshi.SystemInfo").getDeclaredConstructor().newInstance();
         this.register(() -> new CPUModelMetricsCollector(systemInfo));
         this.register(() -> new OperatingSystemMetricsCollector(systemInfo));
      } catch (Exception var3) {
         LogUtils.error("Failed to get systeminfo!", var3);
      }
   }

   public void register(Supplier<MetricsCollector> collector) {
      try {
         MetricsCollector metricsCollector = collector.get();
         this.collectors.add(metricsCollector);
      } catch (Exception var3) {
         LogUtils.error("Failed to initialize collector!");
      }
   }

   public List<MetricsCollector> collectors() {
      return Collections.unmodifiableList(this.collectors);
   }
}
