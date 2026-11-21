package com.lishid.openinv.util;

import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.LegacyAbstractLogger;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.helpers.NormalizedParameters;
import org.slf4j.spi.LocationAwareLogger;

public class JulLoggerAdapter extends LegacyAbstractLogger implements LocationAwareLogger {
   private final Logger wrapped;

   public JulLoggerAdapter(Logger wrapped) {
      this.wrapped = wrapped;
      this.name = wrapped.getName();
   }

   protected String getFullyQualifiedCallerName() {
      return JulLoggerAdapter.class.getName();
   }

   protected void handleNormalizedLoggingCall(Level slf4jLevel, Marker marker, String msg, Object[] args, Throwable thrown) {
      java.util.logging.Level level = this.fromSlf4jLevel(slf4jLevel);
      if (this.wrapped.isLoggable(level)) {
         this.normalizedLog(this.getFullyQualifiedCallerName(), level, msg, args, thrown);
      }
   }

   private void normalizedLog(String fqcn, java.util.logging.Level level, String msg, Object[] args, Throwable thrown) {
      String formatted = MessageFormatter.basicArrayFormat(msg, args);
      LogRecord logRecord = new LogRecord(level, formatted);
      logRecord.setLoggerName(this.getName());
      logRecord.setThrown(thrown);
      this.addSource(fqcn, logRecord);
      this.wrapped.log(logRecord);
   }

   private void addSource(String fqcn, LogRecord logRecord) {
      StackTraceElement[] trace = new Throwable().getStackTrace();
      int maxElements = 12;
      int lastIgnored = maxElements;

      for (int i = 2; i < maxElements; i++) {
         if (this.isIgnored(trace[i].getClassName(), fqcn)) {
            lastIgnored = i;
         }
      }

      if (lastIgnored < maxElements - 1) {
         StackTraceElement caller = trace[lastIgnored + 1];
         logRecord.setSourceClassName(caller.getClassName());
         logRecord.setSourceMethodName(caller.getMethodName());
      }
   }

   private boolean isIgnored(String className, String fqcn) {
      if (className.equals(fqcn)) {
         return true;
      } else {
         return className.startsWith("org.slf4j.") ? true : className.equals(this.getFullyQualifiedCallerName());
      }
   }

   public void log(Marker marker, String callerFqn, int levelInt, String msg, Object[] args, Throwable thrown) {
      java.util.logging.Level level = this.fromSlf4jLevel(Level.intToLevel(levelInt));
      if (this.wrapped.isLoggable(level)) {
         NormalizedParameters params = NormalizedParameters.normalize(msg, args, thrown);
         this.normalizedLog(callerFqn, level, params.getMessage(), params.getArguments(), params.getThrowable());
      }
   }

   private java.util.logging.Level fromSlf4jLevel(Level level) {
      return switch (level) {
         case TRACE -> java.util.logging.Level.FINEST;
         case DEBUG -> java.util.logging.Level.FINE;
         case INFO -> java.util.logging.Level.INFO;
         case WARN -> java.util.logging.Level.WARNING;
         case ERROR -> java.util.logging.Level.SEVERE;
         default -> throw new MatchException(null, null);
      };
   }

   public boolean isTraceEnabled() {
      return this.wrapped.isLoggable(java.util.logging.Level.FINEST);
   }

   public boolean isDebugEnabled() {
      return this.wrapped.isLoggable(java.util.logging.Level.FINE);
   }

   public boolean isInfoEnabled() {
      return this.wrapped.isLoggable(java.util.logging.Level.INFO);
   }

   public boolean isWarnEnabled() {
      return this.wrapped.isLoggable(java.util.logging.Level.WARNING);
   }

   public boolean isErrorEnabled() {
      return this.wrapped.isLoggable(java.util.logging.Level.SEVERE);
   }
}
