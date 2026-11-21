package org.lushplugins.lushrewards.libraries.lushlib.utils;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Deprecated(
   forRemoval = true
)
public class LushLogger {
   public static Logger getLogger() {
      return org.lushplugins.lushrewards.libraries.lushlib.LushLogger.getLogger();
   }

   public static void setLogger(Logger logger) {
      org.lushplugins.lushrewards.libraries.lushlib.LushLogger.setLogger(logger);
   }

   public static void logCurrentStackTrace(@NotNull Level level) {
      org.lushplugins.lushrewards.libraries.lushlib.LushLogger.logCurrentStackTrace(level);
   }

   public static void logCurrentStackTrace(@NotNull Level level, @Nullable String message) {
      org.lushplugins.lushrewards.libraries.lushlib.LushLogger.logCurrentStackTrace(level, message);
   }
}
