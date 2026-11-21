package org.evenmorefish.fish.libs.daisylib.logging;

import java.util.logging.Logger;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.plugin.Plugin;
import org.evenmorefish.fish.libs.daisylib.DaisyLib;
import org.jspecify.annotations.NonNull;

public interface Logging {
   static JavaLogging logging(@NonNull Logger logger) {
      return new JavaLogging(logger);
   }

   static SLF4JLogging logging(@NonNull org.slf4j.Logger logger) {
      return new SLF4JLogging(logger);
   }

   static ComponentLogging logging(@NonNull ComponentLogger logger) {
      return new ComponentLogging(logger);
   }

   static ComponentLogging logging(@NonNull String name) {
      ComponentLogger logger = ComponentLogger.logger(name);
      return new ComponentLogging(logger);
   }

   static ComponentLogging logging(@NonNull Class<?> clazz) {
      ComponentLogger logger = ComponentLogger.logger(clazz);
      return new ComponentLogging(logger);
   }

   static ComponentLogging logging(@NonNull Plugin plugin) {
      return new ComponentLogging(plugin.getComponentLogger());
   }

   void info(@NonNull String var1);

   void info(@NonNull String var1, @NonNull Throwable var2);

   default void info(@NonNull String... message) {
      for (String line : message) {
         this.info(line);
      }
   }

   void warn(@NonNull String var1);

   void warn(@NonNull String var1, @NonNull Throwable var2);

   default void warn(@NonNull String... message) {
      for (String line : message) {
         this.warn(line);
      }
   }

   void error(@NonNull String var1);

   void error(@NonNull String var1, @NonNull Throwable var2);

   default void error(@NonNull String... message) {
      for (String line : message) {
         this.error(line);
      }
   }

   default void debug(@NonNull String message) {
      if (DaisyLib.Settings.ENABLE_DEBUG.get()) {
         this.info(this.formatDebug(message));
      }
   }

   default void debug(@NonNull String message, @NonNull Throwable throwable) {
      if (DaisyLib.Settings.ENABLE_DEBUG.get()) {
         this.info(this.formatDebug(message), throwable);
      }
   }

   default void debug(@NonNull String... message) {
      for (String line : message) {
         this.debug(line);
      }
   }

   default void exception(@NonNull Throwable throwable) {
      this.error(throwable.getMessage(), throwable);
   }

   @NonNull
   default String formatDebug(@NonNull String message) {
      return "[DEBUG] " + message;
   }
}
