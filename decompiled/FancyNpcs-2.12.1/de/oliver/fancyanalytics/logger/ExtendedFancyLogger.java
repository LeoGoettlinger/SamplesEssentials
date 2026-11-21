package de.oliver.fancyanalytics.logger;

import de.oliver.fancyanalytics.logger.appender.Appender;
import de.oliver.fancyanalytics.logger.middleware.Middleware;
import de.oliver.fancyanalytics.logger.properties.Property;
import de.oliver.fancyanalytics.logger.properties.ThrowableProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtendedFancyLogger extends FancyLogger {
   public ExtendedFancyLogger(String name) {
      super(name);
   }

   public ExtendedFancyLogger(String name, LogLevel currentLevel, List<Appender> appenders, List<Middleware> middlewares) {
      super(name, currentLevel, appenders, middlewares);
   }

   public void log(LogLevel logLevel, String message, Property<?>... properties) {
      Throwable throwable = null;
      Map<String, Object> props = new HashMap<>();

      for (Property<?> property : properties) {
         if (property instanceof ThrowableProperty tp) {
            throwable = tp.getValue();
         } else {
            props.put(property.getKey(), property.getValue());
         }
      }

      long threadId = Thread.currentThread().getId();
      String threadName = Thread.currentThread().getName();
      props.put("thread_id", threadId);
      props.put("thread_name", threadName);
      if (throwable != null) {
         props.put("throwable", throwable);
      }

      this.log(logLevel, new LogEntry(this.name, logLevel, message, System.currentTimeMillis(), props), this.defaultAppenders);
   }

   public void debug(String message, Property<?>... properties) {
      this.log(LogLevel.DEBUG, message, properties);
   }

   public void info(String message, Property<?>... properties) {
      this.log(LogLevel.INFO, message, properties);
   }

   public void warn(String message, Property<?>... properties) {
      this.log(LogLevel.WARN, message, properties);
   }

   public void error(String message, Property<?>... properties) {
      this.log(LogLevel.ERROR, message, properties);
   }
}
