package de.theredend2000.advancedhunt.platform;

import org.bukkit.Bukkit;

public final class PlatformAccess {
   private static volatile PlatformAdapter adapter;

   private PlatformAccess() {
   }

   public static PlatformAdapter get() {
      PlatformAdapter local = adapter;
      if (local != null) {
         return local;
      } else {
         synchronized (PlatformAccess.class) {
            if (adapter != null) {
               return adapter;
            } else {
               adapter = loadAdapter();
               return adapter;
            }
         }
      }
   }

   private static PlatformAdapter loadAdapter() {
      MinecraftVersion version = MinecraftVersion.detect();
      String implClass;
      if (version.isLessThan(1, 9, 0)) {
         implClass = "de.theredend2000.advancedhunt.platform.impl.Spigot18PlatformAdapter";
      } else if (version.isLessThan(1, 13, 0)) {
         implClass = "de.theredend2000.advancedhunt.platform.impl.Spigot19PlatformAdapter";
      } else if (version.isLessThan(1, 14, 0)) {
         implClass = "de.theredend2000.advancedhunt.platform.impl.Spigot113PlatformAdapter";
      } else if (version.isLessThan(1, 15, 0)) {
         implClass = "de.theredend2000.advancedhunt.platform.impl.Spigot114PlatformAdapter";
      } else if (version.isLessThan(1, 20, 5)) {
         implClass = "de.theredend2000.advancedhunt.platform.impl.Spigot115PlatformAdapter";
      } else if (version.isLessThan(1, 21, 0)) {
         implClass = "de.theredend2000.advancedhunt.platform.impl.Spigot1205PlusPlatformAdapter";
      } else {
         implClass = "de.theredend2000.advancedhunt.platform.impl.Spigot121PlusPlatformAdapter";
      }

      try {
         Class<?> clazz = Class.forName(implClass, true, PlatformAccess.class.getClassLoader());
         Object instance = clazz.getDeclaredConstructor().newInstance();
         if (!(instance instanceof PlatformAdapter)) {
            throw new IllegalStateException("Platform adapter does not implement PlatformAdapter: " + implClass);
         } else {
            Bukkit.getLogger().info("[AdvancedHunt] Using platform adapter: " + implClass + " (MC " + version + ")");
            return (PlatformAdapter)instance;
         }
      } catch (Throwable var4) {
         throw new IllegalStateException("Failed to load platform adapter: " + implClass + " (MC " + version + ")", var4);
      }
   }
}
