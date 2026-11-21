package de.oliver.fancyanalytics.logger;

import java.util.Map;
import org.jetbrains.annotations.NotNull;

public record LogEntry(@NotNull String loggerName, @NotNull LogLevel logLevel, @NotNull String message, long timestamp, @NotNull Map<String, Object> properties) {
   public LogEntry addProperty(@NotNull String key, @NotNull Object value) {
      this.properties.put(key, value);
      return this;
   }
}
