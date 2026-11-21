package de.oliver.fancyanalytics.api.metrics;

import de.oliver.fancyanalytics.api.FancyAnalyticsAPI;
import de.oliver.fancyanalytics.sdk.metrics.defaults.DefaultSystemMetrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class DefaultPluginMetrics {
   public static double playerCount() {
      return Bukkit.getOnlinePlayers().size();
   }

   public static double pluginCount() {
      return Bukkit.getPluginManager().getPlugins().length;
   }

   public static String serverSoftware() {
      return Bukkit.getServer().getName();
   }

   public static String serverVersion() {
      return Bukkit.getMinecraftVersion();
   }

   public static String pluginVersion(Plugin plugin) {
      return plugin.getDescription().getVersion();
   }

   public static void register(FancyAnalyticsAPI analytics) {
      analytics.registerStringMetric(new MetricSupplier<>("operating_system", DefaultSystemMetrics::operatingSystem));
      analytics.registerStringMetric(new MetricSupplier<>("operating_system_architecture", DefaultSystemMetrics::operatingSystemArchitecture));
      analytics.registerNumberMetric(new MetricSupplier<>("cpu_cores", DefaultSystemMetrics::cpuCoreCount));
      analytics.registerStringMetric(new MetricSupplier<>("java_version", DefaultSystemMetrics::javaVersion));
      analytics.registerStringMetric(new MetricSupplier<>("jvm_name", DefaultSystemMetrics::jvmName));
      if (isBukkit()) {
         analytics.registerNumberMetric(new MetricSupplier<>("online_players", DefaultPluginMetrics::playerCount));
         analytics.registerNumberMetric(new MetricSupplier<>("amount_plugins", DefaultPluginMetrics::pluginCount));
         analytics.registerStringMetric(new MetricSupplier<>("server_version", DefaultPluginMetrics::serverVersion));
         analytics.registerStringMetric(new MetricSupplier<>("server_software", DefaultPluginMetrics::serverSoftware));
      }
   }

   private static boolean isBukkit() {
      try {
         Class.forName("org.bukkit.Bukkit");
         return true;
      } catch (ClassNotFoundException var1) {
         return false;
      }
   }
}
