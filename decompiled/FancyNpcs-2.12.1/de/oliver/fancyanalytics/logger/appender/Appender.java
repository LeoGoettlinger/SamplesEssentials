package de.oliver.fancyanalytics.logger.appender;

import de.oliver.fancyanalytics.logger.LogEntry;

public interface Appender {
   void append(LogEntry var1);

   void close();
}
