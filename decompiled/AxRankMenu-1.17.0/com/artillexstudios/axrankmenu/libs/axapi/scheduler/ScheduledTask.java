package com.artillexstudios.axrankmenu.libs.axapi.scheduler;

import org.bukkit.plugin.Plugin;

public interface ScheduledTask {
   Plugin getOwningPlugin();

   void cancel();

   boolean isCancelled();
}
