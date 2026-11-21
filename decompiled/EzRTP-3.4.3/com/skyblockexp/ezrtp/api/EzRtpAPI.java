package com.skyblockexp.ezrtp.api;

import com.skyblockexp.ezrtp.teleport.TeleportReason;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;

public final class EzRtpAPI {
   private EzRtpAPI() {
   }

   public static TeleportService getTeleportService() {
      try {
         ServicesManager sm = Bukkit.getServicesManager();
         if (sm != null) {
            RegisteredServiceProvider<TeleportService> reg = sm.getRegistration(TeleportService.class);
            if (reg != null) {
               return (TeleportService)reg.getProvider();
            }
         }
      } catch (Throwable var6) {
      }

      try {
         PluginManager pm = Bukkit.getPluginManager();
         if (pm != null) {
            Plugin plugin = pm.getPlugin("EzRTP");
            if (plugin != null) {
               try {
                  Method m = plugin.getClass().getMethod("getTeleportService");
                  Object svc = m.invoke(plugin);
                  if (svc != null) {
                     if (svc instanceof TeleportService) {
                        return (TeleportService)svc;
                     }

                     return adaptToTeleportService(svc);
                  }
               } catch (Throwable var4) {
               }
            }
         }
      } catch (Throwable var5) {
      }

      return null;
   }

   private static TeleportService adaptToTeleportService(final Object svc) {
      return new TeleportService() {
         @Override
         public void teleportPlayer(Player player, TeleportReason reason) {
            try {
               Method m = svc.getClass().getMethod("teleportPlayer", Player.class, TeleportReason.class);
               m.invoke(svc, player, reason);
            } catch (Throwable var4) {
            }
         }

         @Override
         public void teleportPlayer(Player player, Object settings, TeleportReason reason) {
            try {
               Method m = svc.getClass().getMethod("teleportPlayer", Player.class, settings.getClass(), TeleportReason.class);
               m.invoke(svc, player, settings, reason);
               return;
            } catch (NoSuchMethodException var7) {
               try {
                  Method mx = svc.getClass().getMethod("teleportPlayer", Player.class, Object.class, TeleportReason.class);
                  mx.invoke(svc, player, settings, reason);
               } catch (Throwable var6) {
               }
            } catch (Throwable var8) {
            }
         }

         @Override
         public void teleportPlayer(Player player, TeleportReason reason, Consumer<Boolean> callback) {
            try {
               Method m = svc.getClass().getMethod("teleportPlayer", Player.class, TeleportReason.class, Consumer.class);
               m.invoke(svc, player, reason, callback);
            } catch (Throwable var5) {
            }
         }

         @Override
         public void teleportPlayer(Player player, Object settings, TeleportReason reason, Consumer<Boolean> callback) {
            try {
               Method m = svc.getClass().getMethod("teleportPlayer", Player.class, settings.getClass(), TeleportReason.class, Consumer.class);
               m.invoke(svc, player, settings, reason, callback);
               return;
            } catch (NoSuchMethodException var8) {
               try {
                  Method mx = svc.getClass().getMethod("teleportPlayer", Player.class, TeleportReason.class, Consumer.class);
                  mx.invoke(svc, player, reason, callback);
               } catch (Throwable var7) {
               }
            } catch (Throwable var9) {
            }
         }
      };
   }

   public static void registerProvider(Plugin plugin, TeleportService provider) {
      if (plugin != null && provider != null) {
         try {
            Bukkit.getServicesManager().register(TeleportService.class, provider, plugin, ServicePriority.Normal);
         } catch (Throwable var3) {
         }
      }
   }

   public static void unregisterProvider(TeleportService provider) {
      if (provider != null) {
         try {
            Bukkit.getServicesManager().unregister(TeleportService.class, provider);
         } catch (Throwable var2) {
         }
      }
   }

   public static boolean isAvailable() {
      return getTeleportService() != null;
   }

   public static void rtpPlayer(Player player) {
      Object service = getTeleportService();
      if (service != null) {
         try {
            TeleportReason reason = TeleportReason.COMMAND;
            Method m = service.getClass().getMethod("teleportPlayer", Player.class, TeleportReason.class);
            m.invoke(service, player, reason);
         } catch (Throwable var4) {
         }
      }
   }

   public static void rtpPlayer(Player player, Object settings) {
      Object service = getTeleportService();
      if (service != null) {
         try {
            TeleportReason reason = TeleportReason.COMMAND;
            Method m = service.getClass().getMethod("teleportPlayer", Player.class, settings.getClass(), TeleportReason.class);
            m.invoke(service, player, settings, reason);
         } catch (NoSuchMethodException var7) {
            try {
               TeleportReason reasonx = TeleportReason.COMMAND;
               Method mx = service.getClass().getMethod("teleportPlayer", Player.class, Object.class, TeleportReason.class);
               mx.invoke(service, player, settings, reasonx);
            } catch (Throwable var6) {
            }
         } catch (Throwable var8) {
         }
      }
   }

   public static void rtpPlayer(Player player, Object settings, Consumer<Boolean> callback) {
      Object service = getTeleportService();
      if (service == null) {
         if (callback != null) {
            callback.accept(false);
         }
      } else {
         try {
            TeleportReason reason = TeleportReason.COMMAND;
            Method m = service.getClass().getMethod("teleportPlayer", Player.class, settings.getClass(), TeleportReason.class, Consumer.class);
            m.invoke(service, player, settings, reason, callback);
            return;
         } catch (NoSuchMethodException var8) {
            try {
               TeleportReason reasonx = TeleportReason.COMMAND;
               Method mx = service.getClass().getMethod("teleportPlayer", Player.class, TeleportReason.class, Consumer.class);
               mx.invoke(service, player, reasonx, callback);
               return;
            } catch (Throwable var7) {
            }
         } catch (Throwable var9) {
         }

         if (callback != null) {
            callback.accept(false);
         }
      }
   }

   public static void rtpPlayerWithSettingsFallback(Player player, Consumer<Boolean> fallback) {
      Object service = getTeleportService();
      if (service == null) {
         if (fallback != null) {
            fallback.accept(false);
         }
      } else {
         rtpPlayer(player);
         if (fallback != null) {
            fallback.accept(true);
         }
      }
   }
}
