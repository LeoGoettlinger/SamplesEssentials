package me.clip.placeholderapi.scheduler.paper;

import me.clip.placeholderapi.scheduler.folia.FoliaScheduler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class PaperScheduler extends FoliaScheduler {
   public PaperScheduler(Plugin plugin) {
      super(plugin);
   }

   @Override
   public boolean isGlobalThread() {
      return Bukkit.getServer().isPrimaryThread();
   }
}
