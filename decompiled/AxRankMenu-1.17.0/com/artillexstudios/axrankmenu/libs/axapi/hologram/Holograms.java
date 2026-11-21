package com.artillexstudios.axrankmenu.libs.axapi.hologram;

import com.artillexstudios.axrankmenu.libs.axapi.AxPlugin;
import com.artillexstudios.axrankmenu.libs.axapi.executor.ExceptionReportingScheduledThreadPool;
import com.artillexstudios.axrankmenu.libs.axapi.hologram.page.HologramPage;
import com.artillexstudios.axrankmenu.libs.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axrankmenu.libs.axapi.utils.logging.LogUtils;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class Holograms {
   private static final AxPlugin plugin = (AxPlugin)AxPlugin.getPlugin(AxPlugin.class);
   private static final ConcurrentHashMap<Integer, HologramPage<?, ?>> pagesMap = new ConcurrentHashMap<>();
   private static ScheduledExecutorService hologramUpdater;

   public static void startTicking() {
      shutdown();
      hologramUpdater = new ExceptionReportingScheduledThreadPool(
         1, new ThreadFactoryBuilder().setNameFormat(plugin.getName() + "-Hologram-Update-Thread").build()
      );
      hologramUpdater.scheduleAtFixedRate(() -> getView().forEach((id, page) -> {
         if (page.containsPlaceholders()) {
            page.update();
         }
      }), 0L, FeatureFlags.HOLOGRAM_UPDATE_TICKS.get() * 50L, TimeUnit.MILLISECONDS);
   }

   public static void shutdown() {
      if (hologramUpdater != null) {
         hologramUpdater.shutdown();

         try {
            hologramUpdater.awaitTermination(30L, TimeUnit.SECONDS);
         } catch (InterruptedException var1) {
            LogUtils.error("An unexpected error occurred while disabling hologram ticker!", var1);
         }
      }
   }

   public static void put(int entityId, HologramPage<?, ?> page) {
      pagesMap.put(entityId, page);
   }

   public static void remove(int entityId) {
      pagesMap.remove(entityId);
   }

   public static HologramPage<?, ?> byId(int entityId) {
      return pagesMap.get(entityId);
   }

   public static Map<Integer, HologramPage<?, ?>> getView() {
      return Collections.unmodifiableMap(pagesMap);
   }
}
