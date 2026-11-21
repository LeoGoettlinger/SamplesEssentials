package me.chancesd.pvpmanager.sdutils.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class Utils {
   private static boolean isPaper = hasClass("com.destroystokyo.paper.PaperConfig") || hasClass("io.papermc.paper.configuration.Configuration");

   private Utils() {
   }

   private static boolean hasClass(String className) {
      try {
         Class.forName(className);
         return true;
      } catch (ClassNotFoundException var2) {
         return false;
      }
   }

   public static <T extends Event> void registerEvent(Plugin plugin, Listener listener, Class<T> eventClass, Consumer<T> handler) {
      Bukkit.getPluginManager().registerEvent(eventClass, listener, EventPriority.NORMAL, (l, event) -> {
         if (eventClass.isInstance(event)) {
            handler.accept(eventClass.cast(event));
         }
      }, plugin, true);
   }

   public static boolean isPaper() {
      return isPaper;
   }

   public static final boolean isVersionAtLeast(String v1, String v2) {
      if (v1.equals(v2)) {
         return true;
      } else {
         String[] v1Array = v1.split("\\.");
         String[] v2Array = v2.split("\\.");
         int length = Math.max(v2Array.length, v1Array.length);

         try {
            for (int i = 0; i < length; i++) {
               int x = i < v2Array.length ? Integer.parseInt(v2Array[i]) : 0;
               int y = i < v1Array.length ? Integer.parseInt(v1Array[i]) : 0;
               if (y > x) {
                  return true;
               }

               if (y < x) {
                  return false;
               }
            }
         } catch (NumberFormatException var8) {
            Log.severe("Error reading version number! Comparing " + v1 + " to " + v2);
         }

         return true;
      }
   }

   public static void renameFile(File file, String name) {
      try {
         Files.move(file.toPath(), file.toPath().resolveSibling(name), StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException var3) {
         Log.severe("Error resetting config file", var3);
      }
   }

   public static String stripTags(String version) {
      return version.replaceAll("[-;+].+", "");
   }

   public static double roundTo1Decimal(double value) {
      return Math.round(value * 10.0) / 10.0;
   }

   public static double roundTo2Decimal(double value) {
      return Math.round(value * 100.0) / 100.0;
   }
}
