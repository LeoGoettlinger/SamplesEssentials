package net.ess3.provider.providers;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.ess3.provider.LoggerProvider;
import org.bukkit.plugin.Plugin;

public class BaseLoggerProvider extends LoggerProvider {
   private final Logger logger;

   public BaseLoggerProvider(final Plugin plugin, final Logger logger) {
      super(plugin);
      this.logger = logger;
   }

   @Override
   protected void doTheLog(Level level, String message, Throwable throwable) {
      this.logger.log(level, message, throwable);
   }

   @Override
   protected void doTheLog(Level level, String message) {
      this.logger.log(level, message);
   }
}
