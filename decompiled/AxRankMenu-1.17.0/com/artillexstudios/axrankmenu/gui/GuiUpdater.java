package com.artillexstudios.axrankmenu.gui;

import com.artillexstudios.axrankmenu.gui.impl.RankGui;
import com.artillexstudios.axrankmenu.libs.axapi.executor.ExceptionReportingScheduledThreadPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GuiUpdater {
   private static ScheduledExecutorService service = null;

   public static void start() {
      if (service != null) {
         service.shutdown();
      }

      service = new ExceptionReportingScheduledThreadPool(1);
      service.scheduleAtFixedRate(() -> {
         try {
            for (RankGui gui : RankGui.getOpenMenus()) {
               gui.open();
            }
         } catch (Exception var2) {
            var2.printStackTrace();
         }
      }, 1L, 1L, TimeUnit.SECONDS);
   }

   public static void stop() {
      if (service != null) {
         service.shutdown();
      }
   }
}
