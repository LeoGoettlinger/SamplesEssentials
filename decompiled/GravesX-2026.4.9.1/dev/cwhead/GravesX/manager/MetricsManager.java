package dev.cwhead.GravesX.manager;

import com.ranull.graves.Graves;
import com.ranull.graves.libraries.bstats.bukkit.Metrics;
import com.ranull.graves.libraries.bstats.charts.DrilldownPie;
import com.ranull.graves.libraries.bstats.charts.SimplePie;
import com.ranull.graves.libraries.bstats.charts.SingleLineChart;

public class MetricsManager {
   private final Graves plugin;

   public MetricsManager(Graves plugin) {
      this.plugin = plugin;
   }

   public void registerMetrics() {
      int id = this.getMetricsID();
      String url = this.getMetricsUrl(id);

      try {
         Metrics metrics = new Metrics(this.plugin, id);
         metrics.addCustomChart(new SingleLineChart("graves", () -> this.plugin.getCacheManager().getGraveMap().size()));
         metrics.addCustomChart(new SimplePie("permission_handler", () -> {
            if (this.plugin.getIntegrationManager().hasLuckPermsHandler()) {
               return "LuckPerms";
            } else {
               return this.plugin.getIntegrationManager().hasVaultPermProvider() ? "Vault" : "Bukkit";
            }
         }));
         metrics.addCustomChart(new SimplePie("database", () -> this.plugin.getDataManager().getType()));
         metrics.addCustomChart(new SimplePie("plugin_release", () -> {
            if (this.plugin.isPluginDevelopmentBuild()) {
               return "Development Build";
            } else if (this.plugin.isPluginOutdatedBuild()) {
               return "Outdated Build";
            } else {
               return this.plugin.isPluginUnknownBuild() ? "Unknown Build" : "Production Build";
            }
         }));
         metrics.addCustomChart(new DrilldownPie("database_versions", () -> this.plugin.getDataManager().getDatabaseVersions()));
         this.plugin.getLogger().info("bStats metrics loaded: " + url);
      } catch (Throwable var4) {
         this.plugin.getLogger().warning("Failed to load bStats metrics. Stats won't be sent to: " + url);
      }
   }

   public void registerMetricsLegacy() {
      int id = this.getMetricsIDLegacy();
      String url = this.getMetricsUrl(id);

      try {
         Metrics metricsLegacy = new Metrics(this.plugin, id);
         metricsLegacy.addCustomChart(new SingleLineChart("graves", () -> this.plugin.getCacheManager().getGraveMap().size()));
         metricsLegacy.addCustomChart(new SimplePie("permission_handler", () -> {
            if (this.plugin.getIntegrationManager().hasLuckPermsHandler()) {
               return "LuckPerms";
            } else {
               return this.plugin.getIntegrationManager().hasVaultPermProvider() ? "Vault" : "Bukkit";
            }
         }));
         metricsLegacy.addCustomChart(new SimplePie("database", () -> this.plugin.getDataManager().getType()));
         metricsLegacy.addCustomChart(new SimplePie("plugin_release", () -> {
            if (this.plugin.isPluginDevelopmentBuild()) {
               return "Development Build";
            } else if (this.plugin.isPluginOutdatedBuild()) {
               return "Outdated Build";
            } else if (this.plugin.isPluginOutdatedBuild()) {
               return "Outdated Build";
            } else {
               return this.plugin.isPluginUnknownBuild() ? "Unknown Build" : "Production Build";
            }
         }));
         metricsLegacy.addCustomChart(new DrilldownPie("database_versions", () -> this.plugin.getDataManager().getDatabaseVersions()));
         this.plugin.getLogger().info("bStats legacy metrics loaded: " + url);
      } catch (Throwable var4) {
         this.plugin.getLogger().warning("Failed to load bStats legacy metrics. Stats won't be sent to: " + url);
      }
   }

   public final int getMetricsID() {
      return 23069;
   }

   public final int getMetricsIDLegacy() {
      return 12849;
   }

   private String getMetricsUrl(int id) {
      return "https://bstats.org/plugin/bukkit/" + (id == this.getMetricsID() ? "GravesX" : "Graves") + "/" + id;
   }
}
