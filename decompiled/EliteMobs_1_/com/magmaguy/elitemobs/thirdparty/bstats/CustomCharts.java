package com.magmaguy.elitemobs.thirdparty.bstats;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.shaded.bstats.bukkit.Metrics;
import com.magmaguy.shaded.bstats.charts.AdvancedPie;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;

public class CustomCharts {
   public CustomCharts() {
      Metrics metrics = EliteMobs.metrics;
      metrics.addCustomChart(new AdvancedPie("minidungeons", () -> {
         Map<String, Integer> valueMap = new HashMap<>();

         for (EMPackage emPackage : EMPackage.getEmPackages().values()) {
            valueMap.put(ChatColor.stripColor(emPackage.getContentPackagesConfigFields().getFilename()), emPackage.isInstalled() ? 1 : 0);
         }

         return valueMap;
      }));
   }
}
