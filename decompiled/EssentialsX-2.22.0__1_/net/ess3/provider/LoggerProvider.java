package net.ess3.provider;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;

public abstract class LoggerProvider extends PluginLogger {
   public LoggerProvider(final Plugin plugin) {
      super(plugin);
   }

   protected abstract void doTheLog(Level level, String message, Throwable throwable);

   protected abstract void doTheLog(Level level, String message);

   public void log(Level level, String msg) {
      this.doTheLog(level, msg);
   }

   public void log(Level level, String msg, Throwable thrown) {
      this.doTheLog(level, msg, thrown);
   }

   public void log(LogRecord logRecord) {
      this.doTheLog(logRecord.getLevel(), logRecord.getMessage(), logRecord.getThrown());
   }

   public void warning(String message) {
      this.doTheLog(Level.WARNING, message);
   }

   public void info(String message) {
      this.doTheLog(Level.INFO, message);
   }

   public void severe(String message) {
      this.doTheLog(Level.SEVERE, message);
   }
}
