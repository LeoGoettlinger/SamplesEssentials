package org.evenmorefish.fish.libs.daisylib.logging;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jspecify.annotations.NonNull;

public class JavaLogging implements Logging {
   private final Logger logger;

   protected JavaLogging(@NonNull Logger logger) {
      this.logger = logger;
   }

   @Override
   public void info(@NonNull String message) {
      this.logger.info(message);
   }

   @Override
   public void info(@NonNull String message, @NonNull Throwable throwable) {
      this.logger.log(Level.INFO, message, throwable);
   }

   @Override
   public void warn(@NonNull String message) {
      this.logger.warning(message);
   }

   @Override
   public void warn(@NonNull String message, @NonNull Throwable throwable) {
      this.logger.log(Level.WARNING, message, throwable);
   }

   @Override
   public void error(@NonNull String message) {
      this.logger.severe(message);
   }

   @Override
   public void error(@NonNull String message, @NonNull Throwable throwable) {
      this.logger.log(Level.SEVERE, message, throwable);
   }
}
