package xyz.lychee.lagfixer.managers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.zip.GZIPOutputStream;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.objects.AbstractManager;
import xyz.lychee.lagfixer.objects.AbstractModule;

public class MetricsManager extends AbstractManager {
   private static final Gson gson = new Gson();
   private MetricsManager.MetricsBase metricsBase;
   private String uuid;

   public MetricsManager(LagFixer plugin) {
      super(plugin);
   }

   public void addCustomChart(MetricsManager.CustomChart chart) {
      this.metricsBase.addCustomChart(chart);
   }

   @Override
   public void load() throws Exception {
      File bStatsFolder = new File(this.getPlugin().getDataFolder().getParentFile(), "bStats");
      File configFile = new File(bStatsFolder, "config.yml");
      YamlConfiguration cfg = YamlConfiguration.loadConfiguration(configFile);
      if (!cfg.isSet("serverUuid")) {
         this.uuid = UUID.randomUUID().toString();
         cfg.set("serverUuid", this.uuid);
         cfg.save(configFile);
      } else {
         this.uuid = cfg.getString("serverUuid");
      }

      this.metricsBase = new MetricsManager.MetricsBase(19292, Runnable::run);
      this.addCustomChart(new MetricsManager.SingleLineChart("entities", () -> SupportManager.getInstance().getWorldsMonitor().getEntities()));
      this.addCustomChart(new MetricsManager.SingleLineChart("creatures", () -> SupportManager.getInstance().getWorldsMonitor().getCreatures()));
      this.addCustomChart(new MetricsManager.SingleLineChart("items", () -> SupportManager.getInstance().getWorldsMonitor().getItems()));
      this.addCustomChart(new MetricsManager.SingleLineChart("projectiles", () -> SupportManager.getInstance().getWorldsMonitor().getProjectiles()));
      this.addCustomChart(new MetricsManager.SingleLineChart("vehicles", () -> SupportManager.getInstance().getWorldsMonitor().getVehicles()));
      this.addCustomChart(new MetricsManager.AdvancedPie("modules", () -> {
         Map<String, Integer> values = new HashMap<>();

         for (AbstractModule module : new HashSet<>(ModuleManager.getInstance().getModules().values())) {
            if (module.isLoaded()) {
               values.put(module.getName(), 1);
            }
         }

         return values;
      }));
   }

   @Override
   public void disable() throws Exception {
      if (this.metricsBase != null) {
         this.metricsBase.shutdown();
      }
   }

   @Override
   public boolean isEnabled() {
      return this.getPlugin().getConfig().getBoolean("main.bStats");
   }

   public static class AdvancedPie extends MetricsManager.CustomChart {
      private final Callable<Map<String, Integer>> callable;

      public AdvancedPie(String chartId, Callable<Map<String, Integer>> callable) {
         super(chartId);
         this.callable = callable;
      }

      @Override
      protected JsonObject getChartData() throws Exception {
         JsonObject data = new JsonObject();
         JsonObject values = new JsonObject();
         Map<String, Integer> map = this.callable.call();
         if (map != null && !map.isEmpty()) {
            boolean allSkipped = true;

            for (Entry<String, Integer> entry : map.entrySet()) {
               if (entry.getValue() != 0) {
                  allSkipped = false;
                  values.addProperty(entry.getKey(), entry.getValue());
               }
            }

            if (allSkipped) {
               return null;
            } else {
               data.add("values", values);
               return data;
            }
         } else {
            return null;
         }
      }
   }

   public abstract static class CustomChart {
      private final String chartId;

      protected CustomChart(String chartId) {
         if (chartId == null) {
            throw new IllegalArgumentException("chartId must not be null");
         } else {
            this.chartId = chartId;
         }
      }

      public JsonObject getRequestJsonObject() {
         JsonObject jsonObject = new JsonObject();
         jsonObject.addProperty("chartId", this.chartId);

         try {
            JsonObject data = this.getChartData();
            if (data == null) {
               return null;
            } else {
               jsonObject.add("data", data);
               return jsonObject;
            }
         } catch (Throwable var3) {
            return null;
         }
      }

      protected abstract JsonObject getChartData() throws Exception;
   }

   public class MetricsBase {
      private final ScheduledExecutorService scheduler;
      private final int serviceId;
      private final Consumer<Runnable> submitTaskConsumer;
      private final Set<MetricsManager.CustomChart> customCharts = new HashSet<>();

      public MetricsBase(int serviceId, Consumer<Runnable> submitTaskConsumer) {
         ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1, task -> new Thread(task, "bStats-Metrics"));
         scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
         this.scheduler = scheduler;
         this.serviceId = serviceId;
         this.submitTaskConsumer = submitTaskConsumer;
         this.startSubmitting();
      }

      private byte[] compress(String str) {
         if (str == null) {
            return null;
         } else {
            try {
               byte[] var4;
               try (
                  ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                  GZIPOutputStream gzip = new GZIPOutputStream(outputStream);
               ) {
                  gzip.write(str.getBytes(StandardCharsets.UTF_8));
                  var4 = outputStream.toByteArray();
               }

               return var4;
            } catch (IOException var10) {
               return null;
            }
         }
      }

      public void addCustomChart(MetricsManager.CustomChart chart) {
         this.customCharts.add(chart);
      }

      public void shutdown() {
         this.scheduler.shutdown();
      }

      private void startSubmitting() {
         Runnable submitTask = () -> {
            if (this.submitTaskConsumer != null) {
               this.submitTaskConsumer.accept(this::submitData);
            } else {
               this.submitData();
            }
         };
         long initialDelay = (long)(60000.0 * (3.0 + Math.random() * 3.0));
         long secondDelay = (long)(60000.0 * (Math.random() * 30.0));
         this.scheduler.schedule(submitTask, initialDelay, TimeUnit.MILLISECONDS);
         this.scheduler.scheduleAtFixedRate(submitTask, initialDelay + secondDelay, 1800000L, TimeUnit.MILLISECONDS);
      }

      private void submitData() {
         JsonObject baseJson = new JsonObject();
         baseJson.addProperty("playerAmount", Bukkit.getOnlinePlayers().size());
         baseJson.addProperty("onlineMode", Bukkit.getOnlineMode() ? 1 : 0);
         baseJson.addProperty("bukkitVersion", Bukkit.getVersion());
         baseJson.addProperty("bukkitName", Bukkit.getName());
         baseJson.addProperty("javaVersion", System.getProperty("java.version"));
         baseJson.addProperty("osName", System.getProperty("os.name"));
         baseJson.addProperty("osArch", System.getProperty("os.arch"));
         baseJson.addProperty("osVersion", System.getProperty("os.version"));
         baseJson.addProperty("coreCount", Runtime.getRuntime().availableProcessors());
         JsonObject serviceJson = new JsonObject();
         serviceJson.addProperty("pluginVersion", MetricsManager.this.getPlugin().getDescription().getVersion());
         serviceJson.addProperty("id", this.serviceId);
         JsonObject[] chartData = this.customCharts
            .stream()
            .map(MetricsManager.CustomChart::getRequestJsonObject)
            .filter(Objects::nonNull)
            .toArray(JsonObject[]::new);
         serviceJson.add("customCharts", MetricsManager.gson.toJsonTree(chartData));
         baseJson.add("service", serviceJson);
         baseJson.addProperty("serverUUID", MetricsManager.this.uuid);
         baseJson.addProperty("metricsVersion", "3.0.2");
         this.sendData(baseJson);
      }

      private void sendData(JsonObject data) {
         HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://bStats.org/api/v2/data/bukkit"))
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("User-Agent", "Metrics-Service/1")
            .POST(BodyPublishers.ofString(data.toString()))
            .build();
         SupportManager.getInstance().getClient().sendAsync(request, BodyHandlers.discarding());
      }
   }

   public static class SingleLineChart extends MetricsManager.CustomChart {
      private final Callable<Integer> callable;

      public SingleLineChart(String chartId, Callable<Integer> callable) {
         super(chartId);
         this.callable = callable;
      }

      @Override
      protected JsonObject getChartData() throws Exception {
         int value = this.callable.call();
         if (value == 0) {
            return null;
         } else {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("value", value);
            return jsonObject;
         }
      }
   }
}
