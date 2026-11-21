package github.nighter.smartspawner.libs.mariadb.util.log;

import java.util.logging.Level;
import java.util.regex.Matcher;

public class JdkLogger implements Logger {
   private final java.util.logging.Logger logger;

   public JdkLogger(java.util.logging.Logger logger) {
      this.logger = logger;
   }

   @Override
   public String getName() {
      return this.logger.getName();
   }

   @Override
   public boolean isTraceEnabled() {
      return this.logger.isLoggable(Level.FINEST);
   }

   @Override
   public void trace(String msg) {
      this.logger.log(Level.FINEST, msg);
   }

   @Override
   public void trace(String format, Object... arguments) {
      this.logger.log(Level.FINEST, this.format(format, arguments));
   }

   @Override
   public void trace(String msg, Throwable t) {
      this.logger.log(Level.FINEST, msg, t);
   }

   @Override
   public boolean isDebugEnabled() {
      return this.logger.isLoggable(Level.FINE);
   }

   @Override
   public void debug(String msg) {
      this.logger.log(Level.FINE, msg);
   }

   @Override
   public void debug(String format, Object... arguments) {
      this.logger.log(Level.FINE, this.format(format, arguments));
   }

   @Override
   public void debug(String msg, Throwable t) {
      this.logger.log(Level.FINE, msg, t);
   }

   @Override
   public boolean isInfoEnabled() {
      return this.logger.isLoggable(Level.INFO);
   }

   @Override
   public void info(String msg) {
      this.logger.log(Level.INFO, msg);
   }

   @Override
   public void info(String format, Object... arguments) {
      this.logger.log(Level.INFO, this.format(format, arguments));
   }

   @Override
   public void info(String msg, Throwable t) {
      this.logger.log(Level.INFO, msg, t);
   }

   @Override
   public boolean isWarnEnabled() {
      return this.logger.isLoggable(Level.WARNING);
   }

   @Override
   public void warn(String msg) {
      this.logger.log(Level.WARNING, msg);
   }

   @Override
   public void warn(String format, Object... arguments) {
      this.logger.log(Level.WARNING, this.format(format, arguments));
   }

   @Override
   public void warn(String msg, Throwable t) {
      this.logger.log(Level.WARNING, msg, t);
   }

   @Override
   public boolean isErrorEnabled() {
      return this.logger.isLoggable(Level.SEVERE);
   }

   @Override
   public void error(String msg) {
      this.logger.log(Level.SEVERE, msg);
   }

   @Override
   public void error(String format, Object... arguments) {
      this.logger.log(Level.SEVERE, this.format(format, arguments));
   }

   @Override
   public void error(String msg, Throwable t) {
      this.logger.log(Level.SEVERE, msg, t);
   }

   final String format(String from, Object... arguments) {
      if (from == null) {
         return null;
      } else {
         String computed = from;

         for (Object argument : arguments) {
            computed = computed.replaceFirst("\\{}", Matcher.quoteReplacement(String.valueOf(argument)));
         }

         return computed;
      }
   }
}
