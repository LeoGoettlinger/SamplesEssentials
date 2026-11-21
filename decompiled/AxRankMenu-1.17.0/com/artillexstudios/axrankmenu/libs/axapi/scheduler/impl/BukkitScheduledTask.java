package com.artillexstudios.axrankmenu.libs.axapi.scheduler.impl;

import com.artillexstudios.axrankmenu.libs.axapi.scheduler.ScheduledTask;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class BukkitScheduledTask implements ScheduledTask {
   private final BukkitTask task;

   public BukkitScheduledTask(BukkitTask task) {
      this.task = task;
   }

   @Override
   public Plugin getOwningPlugin() {
      return this.task.getOwner();
   }

   @Override
   public void cancel() {
      this.task.cancel();
   }

   @Override
   public boolean isCancelled() {
      return this.task.isCancelled();
   }
}
