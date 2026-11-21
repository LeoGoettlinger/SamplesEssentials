package me.chancesd.pvpmanager.sdutils.utils;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Log {
   private static Logger logger;
   private static boolean debug;
   private static boolean silent;
   private static String prefixMsg;

   private Log() {
   }

   public static void setup(Logger log, String pluginPrefix) {
      logger = log;
      prefixMsg = pluginPrefix;
   }

   public static void setup(JavaPlugin plugin) {
      setup(plugin.getLogger(), "[" + plugin.getName() + "]");
   }

   public static void infoColor(String message) {
      if (!silent) {
         Bukkit.getConsoleSender().sendMessage(prefixMsg + " " + message);
      }
   }

   public static void info(String message) {
      if (!silent) {
         logger.info(message);
      }
   }

   public static void info(Object message) {
      if (!silent) {
         if (message == null) {
            logger.info("null");
         } else {
            logger.info(message::toString);
         }
      }
   }

   public static void severe(String message) {
      logger.severe(message);
   }

   public static void severe(String message, Throwable thrown) {
      logger.log(Level.SEVERE, message, thrown);
   }

   public static void warning(String message) {
      logger.warning(message);
   }

   public static void warning(String message, Throwable thrown) {
      logger.log(Level.WARNING, message, thrown);
   }

   public static void debug(String message) {
      if (debug) {
         logger.info(message);
      }
   }

   public static void debugLazy(Supplier<String> string) {
      if (debug) {
         logger.info(string.get());
      }
   }

   public static void setDebug(boolean debug) {
      Log.debug = debug;
   }

   public static void setSilent(boolean silent) {
      Log.silent = silent;
   }
}
