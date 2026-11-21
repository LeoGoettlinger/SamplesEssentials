package com.artillexstudios.axtrade.libs.axapi.metrics;

import com.artillexstudios.axtrade.libs.axapi.AxPlugin;
import com.artillexstudios.axtrade.libs.axapi.config.YamlConfiguration;
import com.artillexstudios.axtrade.libs.axapi.executor.ExceptionReportingScheduledThreadPool;
import com.artillexstudios.axtrade.libs.axapi.metrics.collectors.MetricsCollector;
import com.artillexstudios.axtrade.libs.axapi.metrics.collectors.MetricsCollectorRegistry;
import com.artillexstudios.axtrade.libs.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axtrade.libs.axapi.utils.http.Requests;
import com.artillexstudios.axtrade.libs.axapi.utils.logging.LogUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;

public final class AxMetrics {
   private final MetricsCollectorRegistry registry;
   private final long pluginId;
   private ScheduledExecutorService executorService;
   private final YamlConfiguration<?> metricsConfig;
   private final String pluginName;

   public AxMetrics(AxPlugin plugin, long pluginId) {
      this.pluginId = pluginId;
      this.registry = new MetricsCollectorRegistry(plugin);
      this.pluginName = plugin.getName();
      Path path = plugin.getDataFolder().toPath();
      Path metricsConfigPath = path.getParent().resolve("AxAPI").resolve("metrics.yml");
      this.metricsConfig = YamlConfiguration.of(metricsConfigPath, MetricsConfig.class).build();
      this.metricsConfig.load();
   }

   public void start() {
      if (MetricsConfig.enabled) {
         if (this.executorService != null) {
            LogUtils.error("Failed to start metrics, as it had already been started!");
         } else {
            this.executorService = new ExceptionReportingScheduledThreadPool(0, Thread.ofVirtual().name(this.pluginName + "-AxMetrics-executor").factory());
            this.executorService.scheduleAtFixedRate(this::submitData, 60000L, 60000L, TimeUnit.MILLISECONDS);
         }
      }
   }

   public void register(MetricsCollector collector) {
      this.registry.register(() -> collector);
   }

   public void cancel() {
      if (this.executorService != null && !this.executorService.isShutdown()) {
         this.executorService.shutdown();

         try {
            this.executorService.awaitTermination(30L, TimeUnit.SECONDS);
         } catch (InterruptedException var2) {
            LogUtils.error("An unexpected error occurred while shutting down the metrics executor!", var2);
         }
      }

      this.executorService = null;
   }

   private void submitData() {
      JsonObject obj = new JsonObject();
      obj.addProperty("timestamp", System.currentTimeMillis());
      obj.addProperty("plugin-id", this.pluginId);
      obj.addProperty("port", Bukkit.getPort());
      JsonArray data = new JsonArray();

      for (MetricsCollector collector : this.registry.collectors()) {
         collector.collect(data);
      }

      obj.add("data", data);

      try {
         HttpResponse<String> response = Requests.post(
            "https://metrics.artillex-studios.com/api/v1/upload?uuid=" + MetricsConfig.serverUuid.toString(),
            Map.of("Content-Type", "application/json"),
            () -> obj
         );
         UUID previousUUID = MetricsConfig.serverUuid;
         if (response.statusCode() == 425) {
            this.metricsConfig.load();
            if (previousUUID.equals(MetricsConfig.serverUuid)) {
               MetricsConfig.serverUuid = UUID.randomUUID();
               this.metricsConfig.save();
            }

            LogUtils.warn("Regenerated server uuid, because it was already in use!");
         } else if (response.statusCode() == 200 && FeatureFlags.DEBUG.get()) {
            LogUtils.debug("Sent metrics successfully!");
         }
      } catch (RuntimeException var5) {
         if (FeatureFlags.DEBUG.get()) {
            LogUtils.debug("Caught exception while sending metrics! Body: {}", obj, var5.getCause());
         }
      }
   }
}
