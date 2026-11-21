package com.skyblockexp.ezrtp.teleport;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChunkyAdapter {
   private ChunkyAdapter() {
   }

   public static boolean isRunning(Object chunkyApi, String worldName) {
      if (chunkyApi == null) {
         return false;
      } else {
         try {
            Method m = chunkyApi.getClass().getMethod("isRunning", String.class);
            Object r = m.invoke(chunkyApi, worldName);
            return r instanceof Boolean ? (Boolean)r : false;
         } catch (Throwable var4) {
            return false;
         }
      }
   }

   public static boolean startTask(Object chunkyApi, String worldName, String shape, double centerX, double centerZ, int radiusX, int radiusZ, String pattern) {
      if (chunkyApi == null) {
         return false;
      } else {
         try {
            for (Method m : chunkyApi.getClass().getMethods()) {
               if (m.getName().equals("startTask") && m.getParameterCount() == 7) {
                  Object res = m.invoke(chunkyApi, worldName, shape, centerX, centerZ, radiusX, radiusZ, pattern);
                  if (res instanceof Boolean) {
                     return (Boolean)res;
                  }

                  return true;
               }
            }
         } catch (Throwable var15) {
         }

         return false;
      }
   }

   public static void registerListeners(Object chunkyApi, JavaPlugin plugin) {
      if (chunkyApi != null && plugin != null) {
         try {
            try {
               Method onComplete = chunkyApi.getClass().getMethod("onGenerationComplete", Consumer.class);
               onComplete.invoke(chunkyApi, (Consumer<Object>)event -> plugin.getLogger().info("Chunky generation completed"));
            } catch (Throwable var4) {
            }

            try {
               Method onProgress = chunkyApi.getClass().getMethod("onGenerationProgress", Consumer.class);
               onProgress.invoke(chunkyApi, (Consumer<Object>)event -> {});
            } catch (Throwable var3) {
            }
         } catch (Throwable var5) {
            plugin.getLogger().warning("Failed to register Chunky listeners: " + var5.getMessage());
         }
      }
   }
}
