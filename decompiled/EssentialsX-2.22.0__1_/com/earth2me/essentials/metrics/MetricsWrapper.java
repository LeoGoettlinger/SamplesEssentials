package com.earth2me.essentials.metrics;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.economy.EconomyLayer;
import com.earth2me.essentials.economy.EconomyLayers;
import com.earth2me.essentials.libs.bstats.bukkit.Metrics;
import com.earth2me.essentials.libs.bstats.charts.AdvancedBarChart;
import com.earth2me.essentials.libs.bstats.charts.CustomChart;
import com.earth2me.essentials.libs.bstats.charts.DrilldownPie;
import com.earth2me.essentials.libs.bstats.charts.MultiLineChart;
import com.earth2me.essentials.libs.bstats.charts.SimplePie;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MetricsWrapper {
   private final Essentials ess;
   private final Metrics metrics;
   private final JavaPlugin plugin;
   private final Map<String, Boolean> commands = new HashMap<>();

   public MetricsWrapper(final JavaPlugin plugin, final int pluginId, final boolean includeCommands) {
      this.plugin = plugin;
      this.ess = (Essentials)Bukkit.getPluginManager().getPlugin("Essentials");
      this.metrics = new Metrics(plugin, pluginId);
      plugin.getLogger().info("Starting Metrics. Opt-out using the global bStats config.");
      this.addPermsChart();
      this.addEconomyChart();
      this.addReleaseBranchChart();
      this.addVersionHistoryChart();
      if (includeCommands) {
         this.addCommandsChart();
      }
   }

   public void markCommand(final String command, final boolean state) {
      this.commands.put(command, state);
   }

   public void addCustomChart(final CustomChart chart) {
      this.metrics.addCustomChart(chart);
   }

   private void addPermsChart() {
      this.metrics.addCustomChart(new DrilldownPie("permsPlugin", () -> {
         Map<String, Map<String, Integer>> result = new HashMap<>();
         String handler = this.ess.getPermissionsHandler().getName();
         Map<String, Integer> backend = new HashMap<>();
         String backendName = this.ess.getPermissionsHandler().getBackendName();
         backend.put(backendName != null ? backendName : "Other", 1);
         result.put(handler, backend);
         return result;
      }));
   }

   private void addEconomyChart() {
      this.metrics.addCustomChart(new DrilldownPie("econPlugin", () -> {
         Map<String, Map<String, Integer>> result = new HashMap<>();
         Map<String, Integer> backend = new HashMap<>();
         EconomyLayer layer = EconomyLayers.getSelectedLayer();
         if (layer != null) {
            backend.put(layer.getBackendName(), 1);
            result.put(layer.getPluginName(), backend);
         } else {
            backend.put("Essentials", 1);
            result.put("Essentials", backend);
         }

         return result;
      }));
   }

   private void addVersionHistoryChart() {
      this.metrics.addCustomChart(new MultiLineChart("versionHistory", () -> {
         HashMap<String, Integer> result = new HashMap<>();
         result.put(this.plugin.getDescription().getVersion(), 1);
         return result;
      }));
   }

   private void addReleaseBranchChart() {
      this.metrics.addCustomChart(new SimplePie("releaseBranch", this.ess.getUpdateChecker()::getVersionBranch));
   }

   private void addCommandsChart() {
      for (String command : this.plugin.getDescription().getCommands().keySet()) {
         this.markCommand(command, false);
      }

      this.metrics.addCustomChart(new AdvancedBarChart("commands", () -> {
         Map<String, int[]> result = new HashMap<>();

         for (Entry<String, Boolean> entry : this.commands.entrySet()) {
            if (entry.getValue()) {
               result.put(entry.getKey(), new int[]{1, 0});
            } else {
               result.put(entry.getKey(), new int[]{0, 1});
            }
         }

         return result;
      }));
   }
}
