package org.lushplugins.lushrewards.utils;

import java.util.logging.Logger;
import org.lushplugins.lushrewards.LushRewards;

public class Debugger {
   private static Logger logger;
   private static Debugger.DebugMode debugMode = Debugger.DebugMode.NONE;

   public static void sendDebugMessage(String string, Debugger.DebugMode mode) {
      if (debugMode == mode || debugMode == Debugger.DebugMode.ALL) {
         getOrInitLogger().info("DEBUG >> " + string);
      }
   }

   public static void setDebugMode(Debugger.DebugMode debugMode) {
      Debugger.debugMode = debugMode;
   }

   private static Logger getOrInitLogger() {
      if (logger == null) {
         logger = LushRewards.getInstance().getLogger();
      }

      return logger;
   }

   public static enum DebugMode {
      NONE,
      PLAYTIME,
      DAILY,
      ALL;
   }
}
