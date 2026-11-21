package me.usainsrht.ujobs.listeners;

import me.usainsrht.ujobs.UJobsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;

public class SaveListener implements Listener {
   UJobsPlugin plugin;

   public SaveListener(UJobsPlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onSave(WorldSaveEvent event) {
      if (event.getWorld() == this.plugin.getServer().getWorlds().getFirst()) {
         this.plugin.getLeaderboardManager().save();
         this.plugin.getConfigManager().saveLeaderboard();
         this.plugin.getStorage().save();
      }
   }
}
