package org.evenmorefish.fish.libs.daisylib.logging;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.NonNull;

public class ComponentLogging extends SLF4JLogging {
   private final ComponentLogger logger;

   protected ComponentLogging(@NonNull ComponentLogger logger) {
      super(logger);
      this.logger = logger;
   }

   public void info(@NonNull Component message) {
      this.logger.info(message);
   }

   public void info(@NonNull Component message, @NonNull Throwable throwable) {
      this.logger.info(message, throwable);
   }

   public void warn(@NonNull Component message) {
      this.logger.warn(message);
   }

   public void warn(@NonNull Component message, @NonNull Throwable throwable) {
      this.logger.warn(message, throwable);
   }

   public void error(@NonNull Component message) {
      this.logger.error(message);
   }

   public void error(@NonNull Component message, @NonNull Throwable throwable) {
      this.logger.error(message, throwable);
   }
}
