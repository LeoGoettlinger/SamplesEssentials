package cm.chunkManager.metrics;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.bukkit.plugin.java.JavaPlugin;

public class MetricsCollector {
   private final JavaPlugin plugin;
   private final Map<String, MetricsCollector.MetricData> metrics;
   private final Map<String, MetricsCollector.TimeSeries> timeSeries;
   private final ScheduledExecutorService scheduler;
   private final Path metricsDirectory;
   private final SimpleDateFormat dateFormat;
   private boolean exportEnabled;
   private int exportInterval;

   public MetricsCollector(JavaPlugin plugin) {
      this.plugin = plugin;
      this.metrics = new ConcurrentHashMap<>();
      this.timeSeries = new ConcurrentHashMap<>();
      this.scheduler = Executors.newScheduledThreadPool(1);
      this.metricsDirectory = Paths.get(plugin.getDataFolder().getPath(), "metrics");
      this.dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

      try {
         Files.createDirectories(this.metricsDirectory);
      } catch (IOException var3) {
         plugin.getLogger().warning("Failed to create metrics directory: " + var3.getMessage());
      }

      this.initializeMetrics();
   }

   private void initializeMetrics() {
      this.registerMetric("chunks_loaded", 1000);
      this.registerMetric("chunks_unloaded", 1000);
      this.registerMetric("cache_hits", 1000);
      this.registerMetric("cache_misses", 1000);
      this.registerMetric("tick_time", 1000);
      this.registerMetric("memory_usage", 1000);
      this.registerMetric("player_movements", 1000);
      this.registerMetric("optimization_runs", 100);
      this.registerMetric("gc_runs", 100);
      this.registerMetric("prefetch_success", 1000);
      this.registerMetric("prefetch_failure", 1000);
      this.registerTimeSeries("tps", 300, 1000L);
      this.registerTimeSeries("memory_mb", 300, 5000L);
      this.registerTimeSeries("loaded_chunks", 300, 5000L);
      this.registerTimeSeries("online_players", 300, 5000L);
   }

   public void registerMetric(String name, int maxSamples) {
      this.metrics.putIfAbsent(name, new MetricsCollector.MetricData(name, maxSamples));
   }

   public void registerTimeSeries(String name, int maxPoints, long intervalMs) {
      this.timeSeries.putIfAbsent(name, new MetricsCollector.TimeSeries(name, maxPoints, intervalMs));
   }

   public void recordMetric(String name, long value) {
      MetricsCollector.MetricData metric = this.metrics.get(name);
      if (metric != null) {
         metric.record(value);
      }
   }

   public void recordTimeSeriesPoint(String name, double value) {
      MetricsCollector.TimeSeries series = this.timeSeries.get(name);
      if (series != null) {
         series.addPoint(value);
      }
   }

   public void startExporting(boolean enabled, int intervalSeconds) {
      this.exportEnabled = enabled;
      this.exportInterval = intervalSeconds;
      if (this.exportEnabled) {
         this.scheduler.scheduleAtFixedRate(this::exportMetrics, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
      }
   }

   private void exportMetrics() {
      if (this.exportEnabled) {
         String filename = "metrics_" + this.dateFormat.format(new Date()) + ".json";
         Path filePath = this.metricsDirectory.resolve(filename);
         Map<String, Object> export = new HashMap<>();
         export.put("timestamp", System.currentTimeMillis());
         export.put("server", this.plugin.getServer().getName());
         export.put("version", this.plugin.getServer().getVersion());
         Map<String, Object> metricsData = new HashMap<>();

         for (Entry<String, MetricsCollector.MetricData> entry : this.metrics.entrySet()) {
            metricsData.put(entry.getKey(), entry.getValue().toMap());
         }

         export.put("metrics", metricsData);
         Map<String, Object> timeSeriesData = new HashMap<>();

         for (Entry<String, MetricsCollector.TimeSeries> entry : this.timeSeries.entrySet()) {
            MetricsCollector.TimeSeries series = entry.getValue();
            Map<String, Object> seriesData = new HashMap<>();
            seriesData.put("trend", series.getTrend());
            seriesData.put("recent", series.getRecentPoints(20));
            timeSeriesData.put(entry.getKey(), seriesData);
         }

         export.put("timeseries", timeSeriesData);

         try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write(this.toJson(export));
         } catch (IOException var12) {
            this.plugin.getLogger().warning("Failed to export metrics: " + var12.getMessage());
         }

         this.cleanOldMetrics();
      }
   }

   private void cleanOldMetrics() {
      try {
         long cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7L);
         Files.list(this.metricsDirectory).filter(path -> {
            try {
               return Files.getLastModifiedTime(path).toMillis() < cutoff;
            } catch (IOException var4) {
               return false;
            }
         }).forEach(path -> {
            try {
               Files.delete(path);
            } catch (IOException var3x) {
               this.plugin.getLogger().warning("Failed to delete old metrics file: " + path);
            }
         });
      } catch (IOException var3) {
         this.plugin.getLogger().warning("Failed to clean old metrics: " + var3.getMessage());
      }
   }

   private String escapeJson(String s) {
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < s.length(); i++) {
         char c = s.charAt(i);
         switch (c) {
            case '\t':
               sb.append("\\t");
               break;
            case '\n':
               sb.append("\\n");
               break;
            case '\r':
               sb.append("\\r");
               break;
            case '"':
               sb.append("\\\"");
               break;
            case '\\':
               sb.append("\\\\");
               break;
            default:
               sb.append(c);
         }
      }

      return sb.toString();
   }

   private String toJson(Map<String, Object> map) {
      StringBuilder json = new StringBuilder();
      json.append("{");
      Iterator<Entry<String, Object>> iter = map.entrySet().iterator();

      while (iter.hasNext()) {
         Entry<String, Object> entry = iter.next();
         json.append("\"").append(this.escapeJson(entry.getKey())).append("\":");
         this.appendValue(json, entry.getValue());
         if (iter.hasNext()) {
            json.append(",");
         }
      }

      json.append("}");
      return json.toString();
   }

   private void appendValue(StringBuilder json, Object value) {
      if (value instanceof Map) {
         json.append(this.toJson((Map<String, Object>)value));
      } else if (value instanceof List) {
         json.append("[");
         List<?> list = (List<?>)value;

         for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
               json.append(",");
            }

            this.appendValue(json, list.get(i));
         }

         json.append("]");
      } else if (value instanceof String) {
         json.append("\"").append(this.escapeJson((String)value)).append("\"");
      } else {
         json.append(value);
      }
   }

   public Map<String, Object> getCurrentMetrics() {
      Map<String, Object> current = new HashMap<>();

      for (Entry<String, MetricsCollector.MetricData> entry : this.metrics.entrySet()) {
         current.put(entry.getKey(), entry.getValue().toMap());
      }

      return current;
   }

   public void shutdown() {
      if (this.exportEnabled) {
         this.exportMetrics();
      }

      this.scheduler.shutdown();

      try {
         if (!this.scheduler.awaitTermination(5L, TimeUnit.SECONDS)) {
            this.scheduler.shutdownNow();
         }
      } catch (InterruptedException var2) {
         this.scheduler.shutdownNow();
      }
   }

   public static class MetricData {
      private final String name;
      private final AtomicLong count;
      private final AtomicLong sum;
      private final AtomicLong min;
      private final AtomicLong max;
      private final List<Long> samples;
      private final int maxSamples;

      public MetricData(String name, int maxSamples) {
         this.name = name;
         this.count = new AtomicLong(0L);
         this.sum = new AtomicLong(0L);
         this.min = new AtomicLong(Long.MAX_VALUE);
         this.max = new AtomicLong(Long.MIN_VALUE);
         this.samples = Collections.synchronizedList(new ArrayList<>());
         this.maxSamples = maxSamples;
      }

      public void record(long value) {
         this.count.incrementAndGet();
         this.sum.addAndGet(value);
         long currentMin = this.min.get();

         while (value < currentMin && !this.min.compareAndSet(currentMin, value)) {
            currentMin = this.min.get();
         }

         long currentMax = this.max.get();

         while (value > currentMax && !this.max.compareAndSet(currentMax, value)) {
            currentMax = this.max.get();
         }

         synchronized (this.samples) {
            this.samples.add(value);
            if (this.samples.size() > this.maxSamples) {
               this.samples.remove(0);
            }
         }
      }

      public double getAverage() {
         long c = this.count.get();
         return c > 0L ? (double)this.sum.get() / c : 0.0;
      }

      public double getMedian() {
         synchronized (this.samples) {
            if (this.samples.isEmpty()) {
               return 0.0;
            } else {
               List<Long> sorted = new ArrayList<>(this.samples);
               Collections.sort(sorted);
               int size = sorted.size();
               return size % 2 == 0 ? (sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2.0 : sorted.get(size / 2).longValue();
            }
         }
      }

      public double getPercentile(double percentile) {
         synchronized (this.samples) {
            if (this.samples.isEmpty()) {
               return 0.0;
            } else {
               List<Long> sorted = new ArrayList<>(this.samples);
               Collections.sort(sorted);
               int index = (int)Math.ceil(percentile / 100.0 * sorted.size()) - 1;
               return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1))).longValue();
            }
         }
      }

      public Map<String, Object> toMap() {
         Map<String, Object> map = new HashMap<>();
         map.put("name", this.name);
         map.put("count", this.count.get());
         map.put("average", this.getAverage());
         map.put("median", this.getMedian());
         map.put("min", this.min.get() == Long.MAX_VALUE ? 0L : this.min.get());
         map.put("max", this.max.get() == Long.MIN_VALUE ? 0L : this.max.get());
         map.put("p95", this.getPercentile(95.0));
         map.put("p99", this.getPercentile(99.0));
         return map;
      }
   }

   public static class TimeSeries {
      private final String name;
      private final List<MetricsCollector.TimeSeries.DataPoint> dataPoints;
      private final int maxPoints;
      private final long intervalMs;

      public TimeSeries(String name, int maxPoints, long intervalMs) {
         this.name = name;
         this.dataPoints = Collections.synchronizedList(new ArrayList<>());
         this.maxPoints = maxPoints;
         this.intervalMs = intervalMs;
      }

      public void addPoint(double value) {
         this.dataPoints.add(new MetricsCollector.TimeSeries.DataPoint(System.currentTimeMillis(), value));
         if (this.dataPoints.size() > this.maxPoints) {
            this.dataPoints.remove(0);
         }
      }

      public List<MetricsCollector.TimeSeries.DataPoint> getRecentPoints(int count) {
         synchronized (this.dataPoints) {
            int start = Math.max(0, this.dataPoints.size() - count);
            return new ArrayList<>(this.dataPoints.subList(start, this.dataPoints.size()));
         }
      }

      public double getTrend() {
         if (this.dataPoints.size() < 2) {
            return 0.0;
         } else {
            int n = Math.min(10, this.dataPoints.size());
            List<MetricsCollector.TimeSeries.DataPoint> recent = this.getRecentPoints(n);
            double sumX = 0.0;
            double sumY = 0.0;
            double sumXY = 0.0;
            double sumX2 = 0.0;

            for (int i = 0; i < recent.size(); i++) {
               sumX += i;
               sumY += recent.get(i).value;
               sumXY += i * recent.get(i).value;
               sumX2 += i * i;
            }

            return (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
         }
      }

      public static class DataPoint {
         public final long timestamp;
         public final double value;

         public DataPoint(long timestamp, double value) {
            this.timestamp = timestamp;
            this.value = value;
         }

         @Override
         public String toString() {
            return "{\"timestamp\":" + this.timestamp + ",\"value\":" + this.value + "}";
         }
      }
   }
}
