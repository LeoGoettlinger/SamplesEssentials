package org.slf4j.spi;

import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.DefaultLoggingEvent;
import org.slf4j.event.KeyValuePair;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

public class DefaultLoggingEventBuilder implements LoggingEventBuilder {
   DefaultLoggingEvent loggingEvent;
   Logger logger;

   public DefaultLoggingEventBuilder(Logger logger, Level level) {
      this.logger = logger;
      this.loggingEvent = new DefaultLoggingEvent(level, logger);
   }

   @Override
   public LoggingEventBuilder addMarker(Marker marker) {
      this.loggingEvent.addMarker(marker);
      return this;
   }

   @Override
   public LoggingEventBuilder setCause(Throwable t) {
      this.loggingEvent.setThrowable(t);
      return this;
   }

   @Override
   public LoggingEventBuilder addArgument(Object p) {
      this.loggingEvent.addArgument(p);
      return this;
   }

   @Override
   public LoggingEventBuilder addArgument(Supplier<Object> objectSupplier) {
      this.loggingEvent.addArgument(objectSupplier.get());
      return this;
   }

   @Override
   public void log(String message) {
      this.loggingEvent.setMessage(message);
      this.innerLog(this.loggingEvent);
   }

   @Override
   public void log(String message, Object arg) {
      this.loggingEvent.setMessage(message);
      this.loggingEvent.addArgument(arg);
      this.innerLog(this.loggingEvent);
   }

   @Override
   public void log(String message, Object arg0, Object arg1) {
      this.loggingEvent.setMessage(message);
      this.loggingEvent.addArgument(arg0);
      this.loggingEvent.addArgument(arg1);
      this.innerLog(this.loggingEvent);
   }

   @Override
   public void log(String message, Object... args) {
      this.loggingEvent.setMessage(message);
      this.loggingEvent.addArguments(args);
      this.innerLog(this.loggingEvent);
   }

   private void innerLog(LoggingEvent logggingEvent) {
      if (this.logger instanceof org.slf4j.event.LoggingEventAware) {
         ((org.slf4j.event.LoggingEventAware)this.logger).log(logggingEvent);
      } else {
         this.logViaPublicLoggerAPI(logggingEvent);
      }
   }

   private void logViaPublicLoggerAPI(LoggingEvent logggingEvent) {
      Object[] argArray = logggingEvent.getArgumentArray();
      int argLen = argArray == null ? 0 : argArray.length;
      Throwable t = logggingEvent.getThrowable();
      int tLen = t == null ? 0 : 1;
      String msg = logggingEvent.getMessage();
      Object[] combinedArguments = new Object[argLen + tLen];
      if (argArray != null) {
         System.arraycopy(argArray, 0, combinedArguments, 0, argLen);
      }

      if (t != null) {
         combinedArguments[argLen] = t;
      }

      msg = this.mergeMarkersAndKeyValuePairs(logggingEvent, msg);
      switch (logggingEvent.getLevel()) {
         case ERROR:
            this.logger.error(msg, combinedArguments);
            break;
         case WARN:
            this.logger.warn(msg, combinedArguments);
            break;
         case INFO:
            this.logger.info(msg, combinedArguments);
            break;
         case DEBUG:
            this.logger.debug(msg, combinedArguments);
            break;
         case TRACE:
            this.logger.trace(msg, combinedArguments);
      }
   }

   private String mergeMarkersAndKeyValuePairs(LoggingEvent logggingEvent, String msg) {
      StringBuilder sb = null;
      if (this.loggingEvent.getMarkers() != null) {
         sb = new StringBuilder();

         for (Marker marker : logggingEvent.getMarkers()) {
            sb.append(marker);
            sb.append(' ');
         }
      }

      if (logggingEvent.getKeyValuePairs() != null) {
         if (sb == null) {
            sb = new StringBuilder();
         }

         for (KeyValuePair kvp : logggingEvent.getKeyValuePairs()) {
            sb.append(kvp.key);
            sb.append('=');
            sb.append(kvp.value);
            sb.append(' ');
         }
      }

      if (sb != null) {
         sb.append(msg);
         return sb.toString();
      } else {
         return msg;
      }
   }

   @Override
   public void log(Supplier<String> messageSupplier) {
      if (messageSupplier == null) {
         this.log(null);
      } else {
         this.log(messageSupplier.get());
      }
   }

   @Override
   public LoggingEventBuilder addKeyValue(String key, Object value) {
      this.loggingEvent.addKeyValue(key, value);
      return this;
   }

   @Override
   public LoggingEventBuilder addKeyValue(String key, Supplier<Object> value) {
      this.loggingEvent.addKeyValue(key, value.get());
      return this;
   }
}
