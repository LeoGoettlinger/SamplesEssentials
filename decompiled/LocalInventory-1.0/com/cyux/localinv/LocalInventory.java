package com.cyux.localinv;

import org.bukkit.plugin.java.JavaPlugin;

public class LocalInventory extends JavaPlugin {
   private int playerCache;

   public void onEnable() {
      this.playerCache = this.initializePlayerCache();
      this.saveDefaultConfig();
      this.getCommand("sli").setExecutor(new InventoryCommands(this));
      this.getCommand("lli").setExecutor(new InventoryCommands(this));
      this.getCommand("ali").setExecutor(new InventoryCommands(this));
      this.getLogger().info("LocalInventory: Enabled successfully.");
   }
}
