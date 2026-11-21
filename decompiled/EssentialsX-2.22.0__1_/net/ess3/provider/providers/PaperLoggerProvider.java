package net.ess3.provider.providers;

import java.util.logging.Level;
import net.ess3.provider.LoggerProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.plugin.Plugin;

public class PaperLoggerProvider extends LoggerProvider {
   private final ComponentLogger logger;

   public PaperLoggerProvider(final Plugin plugin) {
      super(plugin);
      this.logger = plugin.getComponentLogger();
   }

   @Override
   protected void doTheLog(Level level, String message, Throwable throwable) {
      if (message != null) {
         Component component = LegacyComponentSerializer.legacySection().deserialize(message);
         if (level == Level.SEVERE) {
            this.logger.error(component, throwable);
         } else if (level == Level.WARNING) {
            this.logger.warn(component, throwable);
         } else if (level == Level.INFO) {
            this.logger.info(component, throwable);
         } else {
            if (level != Level.FINE) {
               throw new IllegalArgumentException("Unknown level: " + level);
            }

            this.logger.trace(component, throwable);
         }
      }
   }

   @Override
   protected void doTheLog(Level level, String message) {
      if (message != null) {
         Component component = LegacyComponentSerializer.legacySection().deserialize(message);
         if (level == Level.SEVERE) {
            this.logger.error(component);
         } else if (level == Level.WARNING) {
            this.logger.warn(component);
         } else if (level == Level.INFO) {
            this.logger.info(component);
         } else {
            if (level != Level.FINE) {
               throw new IllegalArgumentException("Unknown level: " + level);
            }

            this.logger.trace(component);
         }
      }
   }
}
