package com.skyblockexp.ezrtp.platform;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public final class PlatformRuntimeCapabilitiesDetector {
   private PlatformRuntimeCapabilitiesDetector() {
   }

   public static PlatformRuntimeCapabilities detect(Plugin plugin) {
      ClassLoader classLoader = plugin != null ? plugin.getClass().getClassLoader() : PlatformRuntimeCapabilitiesDetector.class.getClassLoader();
      return detect(classLoader);
   }

   public static PlatformRuntimeCapabilities detect(ClassLoader classLoader) {
      boolean purpurApi = hasClass("org.purpurmc.purpur.PurpurConfig", classLoader) || hasClass("org.purpurmc.purpur.PurpurConfigServer", classLoader);
      boolean regionizedRuntime = hasClass("io.papermc.paper.threadedregions.RegionizedServer", classLoader);
      boolean paperApi = purpurApi
         || hasClass("com.destroystokyo.paper.PaperConfig", classLoader)
         || hasClass("io.papermc.paper.configuration.Configuration", classLoader)
         || regionizedRuntime;
      return new PlatformRuntimeCapabilities(paperApi, purpurApi, regionizedRuntime);
   }

   public static boolean isPaper121PlusAsync(ClassLoader classLoader) {
      boolean paperPresent = hasClass("com.destroystokyo.paper.PaperConfig", classLoader)
         || hasClass("io.papermc.paper.configuration.Configuration", classLoader)
         || hasClass("org.purpurmc.purpur.PurpurConfig", classLoader)
         || hasClass("org.purpurmc.purpur.PurpurConfigServer", classLoader)
         || hasClass("io.papermc.paper.threadedregions.RegionizedServer", classLoader);
      if (!paperPresent) {
         return false;
      } else {
         try {
            String versionString = Bukkit.getMinecraftVersion();
            return isAtLeast121(versionString);
         } catch (Throwable var3) {
            return false;
         }
      }
   }

   static boolean isAtLeast121(String versionString) {
      if (versionString != null && !versionString.isBlank()) {
         String clean = versionString.replaceFirst("[^0-9.]+.*$", "").trim();
         if (clean.isEmpty()) {
            return false;
         } else {
            String[] parts = clean.split("\\.");
            if (parts.length < 2) {
               return false;
            } else {
               try {
                  int major = Integer.parseInt(parts[0]);
                  int minor = Integer.parseInt(parts[1]);
                  return major > 1 || major == 1 && minor >= 21;
               } catch (NumberFormatException var5) {
                  return false;
               }
            }
         }
      } else {
         return false;
      }
   }

   private static boolean hasClass(String className, ClassLoader classLoader) {
      try {
         Class.forName(className, false, classLoader);
         return true;
      } catch (ClassNotFoundException var3) {
         return false;
      }
   }
}
