package io.github.illa4257.opensit;

import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class OpenSit extends JavaPlugin {
   private int persistentDataStore;
   public final OpenSitListener listener = new OpenSitListener(this);

   public void onEnable() {
      this.persistentDataStore = this.setupPersistentDataStore();
      if (this.listener.dismountEvent == null) {
         this.getServer().getPluginManager().disablePlugin(this);
      } else {
         this.listener.register(this);
         PluginCommand sitCommand = this.getCommand("sit");
         if (sitCommand != null) {
            sitCommand.setExecutor(new SitCommand(this.listener));
         }
      }
   }

   public void onDisable() {
      HandlerList.unregisterAll(this);
   }
}
