package com.artillexstudios.axtrade.libs.axapi.scheduler.impl;

import com.artillexstudios.axtrade.libs.axapi.reflection.ClassUtils;
import com.artillexstudios.axtrade.libs.axapi.scheduler.Scheduler;
import org.bukkit.plugin.java.JavaPlugin;

public class SchedulerHandler {
   private Scheduler scheduler;

   public void init(JavaPlugin plugin) {
      if (ClassUtils.INSTANCE.classExists("io.papermc.paper.threadedregions.RegionizedServer")) {
         try {
            this.scheduler = (Scheduler)Class.forName("com.artillexstudios.axtrade.libs.axapi.scheduler.impl.FoliaScheduler")
               .getDeclaredConstructor(JavaPlugin.class)
               .newInstance(plugin);
         } catch (Exception var3) {
            this.scheduler = new BukkitScheduler(plugin);
         }
      } else {
         this.scheduler = new BukkitScheduler(plugin);
      }
   }

   public Scheduler getScheduler() {
      return this.scheduler;
   }
}
