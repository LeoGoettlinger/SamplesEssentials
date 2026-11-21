package com.skyblockexp.ezrtp.teleport;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChunkyRuntimeProvider implements ChunkyProvider {
   private final Object impl;

   private ChunkyRuntimeProvider(Object impl) {
      this.impl = impl;
   }

   public static ChunkyProvider loadProvider() {
      try {
         Class<?> chunkyClass = Class.forName("org.popcraft.chunky.api.ChunkyAPI");
         Object svc = Bukkit.getServicesManager().load(chunkyClass);
         return svc == null ? null : new ChunkyRuntimeProvider(svc);
      } catch (ClassNotFoundException var2) {
         return null;
      } catch (Throwable var3) {
         return null;
      }
   }

   @Override
   public boolean isRunning(String worldName) {
      if (this.impl == null) {
         return false;
      } else {
         try {
            Method m = this.impl.getClass().getMethod("isRunning", String.class);
            Object r = m.invoke(this.impl, worldName);
            return r instanceof Boolean ? (Boolean)r : false;
         } catch (Throwable var4) {
            return false;
         }
      }
   }

   @Override
   public boolean startTask(String worldName, String shape, double centerX, double centerZ, int radiusX, int radiusZ, String pattern) {
      if (this.impl == null) {
         return false;
      } else {
         try {
            for (Method m : this.impl.getClass().getMethods()) {
               if (m.getName().equals("startTask") && m.getParameterCount() == 7) {
                  Object res = m.invoke(this.impl, worldName, shape, centerX, centerZ, radiusX, radiusZ, pattern);
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

   @Override
   public void registerListeners(JavaPlugin plugin) {
      if (this.impl != null && plugin != null) {
         try {
            try {
               Method m = this.impl.getClass().getMethod("onGenerationComplete", Consumer.class);
               m.invoke(this.impl, (Consumer<Object>)event -> plugin.getLogger().info("Chunky generation completed"));
            } catch (Throwable var4) {
            }

            try {
               Method m2 = this.impl.getClass().getMethod("onGenerationProgress", Consumer.class);
               m2.invoke(this.impl, (Consumer<Object>)event -> {});
            } catch (Throwable var3) {
            }
         } catch (Throwable var5) {
         }
      }
   }
}
