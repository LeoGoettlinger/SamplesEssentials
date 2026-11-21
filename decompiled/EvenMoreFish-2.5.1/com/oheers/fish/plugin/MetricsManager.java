package com.oheers.fish.plugin;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.MainConfig;
import org.evenmorefish.fish.libs.bstats.bukkit.Metrics;
import org.evenmorefish.fish.libs.bstats.charts.SimplePie;
import org.evenmorefish.fish.libs.bstats.charts.SingleLineChart;

public class MetricsManager {
   private final EvenMoreFish plugin;
   private int fishCaught = 0;
   private int baitsUsed = 0;
   private int baitsApplied = 0;

   public MetricsManager(EvenMoreFish plugin) {
      this.plugin = plugin;
   }

   public void setupMetrics() {
      if (!MainConfig.getInstance().shouldDebug()) {
         Metrics metrics = new Metrics(this.plugin, 11054);
         metrics.addCustomChart(new SingleLineChart("fish_caught", this::getAndResetFishCaught));
         metrics.addCustomChart(new SingleLineChart("baits_applied", this::getAndResetBaitsApplied));
         metrics.addCustomChart(new SingleLineChart("baits_used", this::getAndResetBaitsUsed));
         metrics.addCustomChart(new SimplePie("database", () -> String.valueOf(MainConfig.getInstance().databaseEnabled())));
      }
   }

   public void incrementFishCaught(int amount) {
      this.fishCaught += amount;
   }

   public void incrementBaitsUsed(int amount) {
      this.baitsUsed += amount;
   }

   public void incrementBaitsApplied(int amount) {
      this.baitsApplied += amount;
   }

   private int getAndResetFishCaught() {
      int value = this.fishCaught;
      this.fishCaught = 0;
      return value;
   }

   private int getAndResetBaitsUsed() {
      int value = this.baitsUsed;
      this.baitsUsed = 0;
      return value;
   }

   private int getAndResetBaitsApplied() {
      int value = this.baitsApplied;
      this.baitsApplied = 0;
      return value;
   }
}
