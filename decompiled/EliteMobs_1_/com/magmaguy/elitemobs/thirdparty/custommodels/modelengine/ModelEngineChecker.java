package com.magmaguy.elitemobs.thirdparty.custommodels.modelengine;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class ModelEngineChecker {
   private ModelEngineChecker() {
   }

   public static boolean modelEngineIsInstalled() {
      Plugin plugin = Bukkit.getPluginManager().getPlugin("ModelEngine");
      return plugin != null && plugin.isEnabled() && plugin.getDescription().getVersion().contains("R3");
   }
}
