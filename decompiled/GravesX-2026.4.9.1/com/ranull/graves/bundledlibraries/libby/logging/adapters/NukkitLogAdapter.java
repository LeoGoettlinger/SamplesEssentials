package com.ranull.graves.bundledlibraries.libby.logging.adapters;

import cn.nukkit.plugin.PluginLogger;
import com.ranull.graves.bundledlibraries.libby.logging.LogLevel;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NukkitLogAdapter implements LogAdapter {
   @NotNull
   private final PluginLogger logger;

   public NukkitLogAdapter(@NotNull PluginLogger logger) {
      this.logger = Objects.requireNonNull(logger, "logger");
   }

   @Override
   public void log(@NotNull LogLevel level, @Nullable String message) {
      switch ((LogLevel)Objects.requireNonNull(level, "level")) {
         case DEBUG:
            this.logger.debug(message);
            break;
         case INFO:
            this.logger.info(message);
            break;
         case WARN:
            this.logger.warning(message);
            break;
         case ERROR:
            this.logger.error(message);
      }
   }

   @Override
   public void log(@NotNull LogLevel level, @Nullable String message, @Nullable Throwable throwable) {
      switch ((LogLevel)Objects.requireNonNull(level, "level")) {
         case DEBUG:
            this.logger.debug(message, throwable);
            break;
         case INFO:
            this.logger.info(message, throwable);
            break;
         case WARN:
            this.logger.warning(message, throwable);
            break;
         case ERROR:
            this.logger.error(message, throwable);
      }
   }
}
