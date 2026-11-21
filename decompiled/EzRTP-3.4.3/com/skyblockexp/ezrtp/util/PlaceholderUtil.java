package com.skyblockexp.ezrtp.util;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;

public final class PlaceholderUtil {
   private static boolean placeholderAPIAvailable = false;
   private static boolean checkedAvailability = false;
   private static final Map<String, String> placeholderCache = new ConcurrentHashMap<>();

   private PlaceholderUtil() {
   }

   public static boolean isPlaceholderAPIAvailable(Logger logger) {
      if (!checkedAvailability) {
         try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            Object pluginManager = null;

            try {
               Class<?> bukkitCls = Class.forName("org.bukkit.Bukkit");
               Method getPluginManager = bukkitCls.getMethod("getPluginManager");
               pluginManager = getPluginManager.invoke(null);
               if (pluginManager != null) {
                  Method getPlugin = pluginManager.getClass().getMethod("getPlugin", String.class);
                  Object papi = getPlugin.invoke(pluginManager, "PlaceholderAPI");
                  if (papi != null) {
                     try {
                        Method isEnabled = papi.getClass().getMethod("isEnabled");
                        Object enabled = isEnabled.invoke(papi);
                        placeholderAPIAvailable = Boolean.TRUE.equals(enabled);
                     } catch (Throwable var8) {
                        placeholderAPIAvailable = true;
                     }
                  } else {
                     placeholderAPIAvailable = false;
                  }
               }
            } catch (Throwable var9) {
               placeholderAPIAvailable = false;
            }

            if (placeholderAPIAvailable) {
               logger.info("PlaceholderAPI found and enabled. Placeholder support is available.");
            } else {
               logger.info("PlaceholderAPI not found. Placeholder resolution will use fallback behavior.");
            }
         } catch (Exception var10) {
            placeholderAPIAvailable = false;
            logger.info("PlaceholderAPI not available. Placeholder resolution will use fallback behavior.");
         }

         checkedAvailability = true;
      }

      return placeholderAPIAvailable;
   }

   public static String resolvePlaceholders(Object player, String text, Logger logger) {
      if (text == null) {
         return null;
      } else if (!isPlaceholderAPIAvailable(logger)) {
         return text;
      } else {
         String cacheKey = (player != null ? player.toString() : "null") + ":" + text;
         String cached = placeholderCache.get(cacheKey);
         if (cached != null) {
            return cached;
         } else {
            try {
               Class<?> cls = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
               Method method = cls.getMethod("setPlaceholders", Class.forName("org.bukkit.entity.Player"), String.class);
               String resolved = (String)method.invoke(null, player, text);
               placeholderCache.put(cacheKey, resolved);
               return resolved;
            } catch (Exception var8) {
               logger.warning("Failed to resolve placeholders: " + var8.getMessage());
               return text;
            }
         }
      }
   }

   public static String resolvePlaceholders(String text, Logger logger) {
      if (text == null) {
         return null;
      } else if (!isPlaceholderAPIAvailable(logger)) {
         return text;
      } else {
         try {
            Class<?> cls = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            Class<?> playerCls = Class.forName("org.bukkit.entity.Player");
            Method method = cls.getMethod("setPlaceholders", playerCls, String.class);
            return (String)method.invoke(null, null, text);
         } catch (Exception var5) {
            logger.warning("Failed to resolve placeholders: " + var5.getMessage());
            return text;
         }
      }
   }

   public static Component resolvePlaceholdersComponent(Object player, Component component, Logger logger) {
      if (component == null) {
         return Component.empty();
      } else if (!isPlaceholderAPIAvailable(logger)) {
         return component;
      } else {
         try {
            ClassLoader papiLoader = Class.forName("me.clip.placeholderapi.PlaceholderAPI").getClassLoader();
            Class<?> mmClass = Class.forName("net.kyori.adventure.text.minimessage.MiniMessage", true, papiLoader);
            Method mmFactory = mmClass.getMethod("miniMessage");
            Object mmInst = mmFactory.invoke(null);
            Method deser = mmClass.getMethod("deserialize", String.class);
            String mm = MessageUtil.serializeToMiniMessage(component);
            Object runtimeComp = deser.invoke(mmInst, mm);

            try {
               Class<?> papiCompClass = Class.forName("me.clip.placeholderapi.PAPIComponents", true, papiLoader);

               for (Method m : papiCompClass.getMethods()) {
                  String name = m.getName().toLowerCase();
                  if (name.contains("replace") || name.contains("resolve") || name.contains("parse")) {
                     Class<?>[] params = m.getParameterTypes();
                     if (params.length == 2) {
                        try {
                           Object result = m.invoke(null, runtimeComp, player);
                           if (result != null) {
                              Method serialize = mmClass.getMethod("serialize", Class.forName("net.kyori.adventure.text.Component", true, papiLoader));
                              String outMm = (String)serialize.invoke(mmInst, result);
                              return MessageUtil.parseMiniMessage(outMm);
                           }
                        } catch (ReflectiveOperationException | IllegalArgumentException var21) {
                        }
                     }
                  }
               }
            } catch (ClassNotFoundException var22) {
            }
         } catch (Throwable var23) {
         }

         try {
            String mm = MessageUtil.serializeToMiniMessage(component);
            String replaced = resolvePlaceholders(player, mm, logger);
            return MessageUtil.parseMiniMessage(replaced);
         } catch (Throwable var20) {
            logger.warning("Failed to resolve component placeholders: " + var20.getMessage());
            return component;
         }
      }
   }

   public static String formatTime(int totalSeconds) {
      if (totalSeconds <= 0) {
         return "0s";
      } else {
         int hours = totalSeconds / 3600;
         int minutes = totalSeconds % 3600 / 60;
         int seconds = totalSeconds % 60;
         StringBuilder result = new StringBuilder();
         if (hours > 0) {
            result.append(hours).append("h ");
         }

         if (minutes > 0) {
            result.append(minutes).append("m ");
         }

         if (seconds > 0 || result.length() == 0) {
            result.append(seconds).append("s");
         }

         return result.toString().trim();
      }
   }

   public static void resetAvailabilityCheck() {
      checkedAvailability = false;
      placeholderAPIAvailable = false;
      placeholderCache.clear();
   }
}
