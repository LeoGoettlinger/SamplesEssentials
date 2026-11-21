package me.chancesd.pvpmanager.sdutils.scheduler;

import org.bukkit.plugin.Plugin;

public interface SDTask {
   void cancel();

   boolean isCancelled();

   Plugin getPlugin();

   int getTaskID();
}
