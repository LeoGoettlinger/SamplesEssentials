package de.oliver.fancylib.logging;

import de.oliver.fancyanalytics.logger.LogEntry;
import de.oliver.fancyanalytics.logger.middleware.Middleware;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginMiddleware implements Middleware {
   private static final Logger log = LoggerFactory.getLogger(PluginMiddleware.class);
   private final Plugin plugin;

   public PluginMiddleware(Plugin plugin) {
      this.plugin = plugin;
   }

   @Nullable
   @Override
   public LogEntry process(LogEntry logEntry) {
      if (this.plugin == null) {
         return logEntry;
      } else {
         logEntry.addProperty("server_software", this.plugin.getServer().getName());
         logEntry.addProperty("server_version", this.plugin.getServer().getBukkitVersion());
         logEntry.addProperty("is_online_mode", this.plugin.getServer().getOnlineMode());
         logEntry.addProperty("plugin_name", this.plugin.getDescription().getName());
         logEntry.addProperty("plugin_version", this.plugin.getDescription().getVersion());
         return logEntry;
      }
   }
}
