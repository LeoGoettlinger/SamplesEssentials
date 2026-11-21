package com.magmaguy.elitemobs.magmacore.initialization;

import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;

public record PluginInitializationConfig(String displayName, String adminPermission, int totalSteps, List<String> dependencies) {
   public PluginInitializationConfig(String displayName, String adminPermission, int totalSteps) {
      this(displayName, adminPermission, totalSteps, null);
   }

   public List<String> resolveDependencies(JavaPlugin plugin) {
      return this.dependencies != null ? this.dependencies : plugin.getDescription().getSoftDepend();
   }
}
