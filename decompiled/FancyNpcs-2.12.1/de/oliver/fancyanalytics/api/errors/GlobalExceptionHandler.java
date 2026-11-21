package de.oliver.fancyanalytics.api.errors;

import de.oliver.fancyanalytics.api.FancyAnalyticsAPI;
import de.oliver.fancyanalytics.logger.FancyLogger;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class GlobalExceptionHandler extends ConsoleHandler {
   private final FancyAnalyticsAPI api;

   public GlobalExceptionHandler(FancyAnalyticsAPI api) {
      this.api = api;
      this.registerLogger(Logger.getGlobal());
   }

   public void registerLogger(Logger logger) {
      logger.addHandler(this);
   }

   public void registerLogger(FancyLogger logger) {
      logger.addMiddlware(logEntry -> {
         if (logEntry.properties().get("throwable") != null) {
         }

         return logEntry;
      });
   }

   @Override
   public void publish(LogRecord record) {
      Throwable exception = record.getThrown();
      if (exception != null) {
         ;
      }
   }
}
