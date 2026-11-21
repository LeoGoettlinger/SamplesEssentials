package de.oliver.fancyanalytics.logger.testing;

import de.oliver.fancyanalytics.logger.ExtendedFancyLogger;
import de.oliver.fancyanalytics.logger.LogLevel;
import de.oliver.fancyanalytics.logger.appender.ConsoleAppender;
import de.oliver.fancyanalytics.logger.appender.JsonAppender;
import de.oliver.fancyanalytics.logger.middleware.Middleware;
import de.oliver.fancyanalytics.logger.properties.ThrowableProperty;
import java.util.List;

public class TestLogging {
   public static void main(String[] args) {
      Middleware m = entry -> {
         entry.addProperty("something", "value");
         return entry;
      };
      ExtendedFancyLogger logger = new ExtendedFancyLogger(
         "TestLogger", LogLevel.DEBUG, List.of(new ConsoleAppender(), new JsonAppender(true, true, false, "")), List.of(m)
      );
      logger.setCurrentLevel(LogLevel.INFO);
      logger.log(LogLevel.DEBUG, "This is a debug message.");
      logger.log(LogLevel.INFO, "This is an informational message.");
      logger.log(LogLevel.WARN, "This is a warning message.");
      logger.log(LogLevel.ERROR, "This is an error message.");

      try {
         throw new RuntimeException("This is a test exception.");
      } catch (RuntimeException var4) {
         logger.error("Runtime exception occurred.", ThrowableProperty.of(var4));
      }
   }
}
