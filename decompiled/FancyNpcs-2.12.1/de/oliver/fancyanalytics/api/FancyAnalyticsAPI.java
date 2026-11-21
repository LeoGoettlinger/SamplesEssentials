package de.oliver.fancyanalytics.api;

import de.oliver.fancyanalytics.api.errors.AnalyticsException;
import de.oliver.fancyanalytics.api.errors.GlobalExceptionHandler;
import de.oliver.fancyanalytics.api.metrics.DefaultPluginMetrics;
import de.oliver.fancyanalytics.api.metrics.MetricSupplier;
import de.oliver.fancyanalytics.sdk.ApiClient;
import de.oliver.fancyanalytics.sdk.errors.Error;
import de.oliver.fancyanalytics.sdk.events.Event;
import de.oliver.fancyanalytics.sdk.metrics.defaults.DefaultSystemMetrics;
import de.oliver.fancyanalytics.sdk.records.Record;
import de.oliver.fancyanalytics.sdk.utils.StackTraceUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class FancyAnalyticsAPI {
   public static final Logger LOGGER = Logger.getLogger("FancyAnalytics");
   private static final String BASE_URL = "https://api.fancyanalytics.net";
   private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "FancyAnalyticsAPI"));
   private final ApiClient apiClient;
   private final String projectId;
   private final String apiToken;
   private final List<MetricSupplier<?>> metrics;
   private final GlobalExceptionHandler exceptionHandler;
   private final Map<String, Long> exceptionCache;
   private final Configuration config;
   private Plugin minecraftPlugin;

   public FancyAnalyticsAPI(@NotNull String projectId, @NotNull String apiToken) {
      this.projectId = projectId;
      this.apiToken = apiToken;
      this.metrics = new ArrayList<>();
      this.exceptionHandler = new GlobalExceptionHandler(this);
      this.exceptionCache = new ConcurrentHashMap<>();
      this.config = Configuration.loadConfig();
      this.apiClient = new ApiClient("https://api.fancyanalytics.net", "", apiToken);
      this.apiClient.setDisableLogging(this.config.disableLogging());
      DefaultPluginMetrics.register(this);
   }

   public void initialize() {
      EXECUTOR.scheduleAtFixedRate(this::send, 5L, 30L, TimeUnit.SECONDS);
   }

   private void send() {
      if (this.config.sendMetrics()) {
         if (!Bukkit.isStopping()) {
            Record record = new Record(this.config.getSenderID(), this.projectId, System.currentTimeMillis(), new HashMap<>());

            for (MetricSupplier<?> metric : this.metrics) {
               record.withEntry(metric.name(), metric.valueSupplier().get());
            }

            this.apiClient.getRecordService().createRecord(this.projectId, record);
         }
      }
   }

   public void reportError(Throwable throwable) {
      if (this.config.sendErrors()) {
         if (!Bukkit.isStopping()) {
            String eClass = throwable.getClass().getName();
            String message = throwable.getMessage();
            String stackTrace = StackTraceUtils.getAsString(throwable);
            if (this.minecraftPlugin != null && this.minecraftPlugin.isEnabled()) {
               boolean found = false;
               if (message != null && message.contains(this.minecraftPlugin.getName())) {
                  found = true;
               }

               if (stackTrace != null && stackTrace.contains(this.minecraftPlugin.getName())) {
                  found = true;
               }

               if (eClass.contains(this.minecraftPlugin.getName())) {
                  found = true;
               }

               if (!found) {
                  return;
               }
            }

            long lastTimeReported = this.exceptionCache.getOrDefault(message + "_" + eClass, 0L);
            if (System.currentTimeMillis() - lastTimeReported >= 300000L) {
               Error error = new Error(eClass, message, stackTrace, new HashMap<>());
               error.withProperty("operatingSystem", DefaultSystemMetrics.operatingSystem());
               error.withProperty("operatingSystemVersion", DefaultSystemMetrics.operatingSystemVersion());
               error.withProperty("operatingSystemArchitecture", DefaultSystemMetrics.operatingSystemArchitecture());
               error.withProperty("javaVersion", DefaultSystemMetrics.javaVersion());
               error.withProperty("jvmName", DefaultSystemMetrics.jvmName());
               if (throwable instanceof AnalyticsException ae) {
                  ae.getProperties().forEach(error::withProperty);
               }

               this.exceptionCache.put(message + "_" + eClass, System.currentTimeMillis());
               this.apiClient.getErrorService().createError(this.projectId, error);
            }
         }
      }
   }

   public void sendEvent(Event event) {
      if (this.config.sendMetrics()) {
         if (!Bukkit.isStopping()) {
            this.apiClient.getEventService().createEvent(this.projectId, event);
         }
      }
   }

   public void registerMinecraftPluginMetrics(Plugin plugin) {
      this.registerStringMetric(new MetricSupplier<>("plugin_version", () -> DefaultPluginMetrics.pluginVersion(plugin)));
      this.minecraftPlugin = plugin;
   }

   public void registerNumberMetric(MetricSupplier<Double> metric) {
      for (MetricSupplier<?> m : this.metrics) {
         if (m.name().equals(metric.name())) {
            throw new IllegalArgumentException("Metric with name " + metric.name() + " already exists");
         }
      }

      this.metrics.add(metric);
   }

   public void registerNumberArrayMetric(MetricSupplier<Double[]> metric) {
      for (MetricSupplier<?> m : this.metrics) {
         if (m.name().equals(metric.name())) {
            throw new IllegalArgumentException("Metric with name " + metric.name() + " already exists");
         }
      }

      this.metrics.add(metric);
   }

   public void registerStringMetric(MetricSupplier<String> metric) {
      for (MetricSupplier<?> m : this.metrics) {
         if (m.name().equals(metric.name())) {
            throw new IllegalArgumentException("Metric with name " + metric.name() + " already exists");
         }
      }

      this.metrics.add(metric);
   }

   public void registerStringArrayMetric(MetricSupplier<String[]> metric) {
      for (MetricSupplier<?> m : this.metrics) {
         if (m.name().equals(metric.name())) {
            throw new IllegalArgumentException("Metric with name " + metric.name() + " already exists");
         }
      }

      this.metrics.add(metric);
   }

   public GlobalExceptionHandler getExceptionHandler() {
      return this.exceptionHandler;
   }

   public Configuration getConfig() {
      return this.config;
   }
}
