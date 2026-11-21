package com.artillexstudios.axafkzone.libs.axapi.gui.inventory;

import com.artillexstudios.axafkzone.libs.axapi.AxPlugin;
import com.artillexstudios.axafkzone.libs.axapi.executor.ExceptionReportingScheduledThreadPool;
import com.artillexstudios.axafkzone.libs.axapi.gui.inventory.renderer.InventoryRenderer;
import com.artillexstudios.axafkzone.libs.axapi.gui.inventory.renderer.InventoryRenderers;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public enum InventoryUpdater {
   INSTANCE;

   private ScheduledExecutorService service;

   public void start(AxPlugin plugin) {
      this.service = new ExceptionReportingScheduledThreadPool(1, Thread.ofPlatform().name(plugin.getName() + "-AxAPI-inventory-updater").factory());
      this.service.scheduleWithFixedDelay(() -> {
         for (InventoryRenderer renderer : InventoryRenderers.getCurrentRenderers()) {
            Gui gui = renderer.getCurrentGui();
            if (gui != null && !renderer.isClosed()) {
               gui.tick();
            }
         }
      }, 0L, 50L, TimeUnit.MILLISECONDS);
   }

   public void shutdown() {
      if (this.service != null && !this.service.isShutdown()) {
         this.service.shutdown();
      }
   }
}
