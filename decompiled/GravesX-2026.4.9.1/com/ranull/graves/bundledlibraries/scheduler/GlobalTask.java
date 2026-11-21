package com.ranull.graves.bundledlibraries.scheduler;

import org.bukkit.plugin.Plugin;

public interface GlobalTask {
   int getTaskId();

   void cancel();

   boolean isCancelled();

   Plugin getPlugin();

   boolean isRunning();

   boolean isRepeating();
}
