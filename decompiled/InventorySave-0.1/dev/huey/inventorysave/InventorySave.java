package dev.huey.inventorysave;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class InventorySave extends JavaPlugin {
   public void onEnable() {
      Core.plugin = this;
      Bukkit.getLogger().info("[Inventory Save] Plugin Enabled");
      Core.MainCommandsToAPI handlers = new Core.MainCommandsToAPI();
      PluginCommand command = this.getCommand("inventory");

      assert command != null;

      command.setExecutor(handlers);
      command.setTabCompleter(handlers);
   }

   public void onDisable() {
      Bukkit.getLogger().info("[Inventory Save] Plugin Disabled");
   }
}
