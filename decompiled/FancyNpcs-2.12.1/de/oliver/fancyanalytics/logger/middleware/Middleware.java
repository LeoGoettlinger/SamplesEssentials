package de.oliver.fancyanalytics.logger.middleware;

import de.oliver.fancyanalytics.logger.LogEntry;
import org.jetbrains.annotations.Nullable;

public interface Middleware {
   @Nullable
   LogEntry process(LogEntry var1);
}
