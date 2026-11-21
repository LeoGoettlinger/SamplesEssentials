package de.oliver.fancyanalytics.logger.appender;

import de.oliver.fancyanalytics.logger.LogEntry;
import java.util.ArrayList;
import java.util.List;

public class MockAppender implements Appender {
   private final List<LogEntry> logEntries = new ArrayList<>();

   @Override
   public void append(LogEntry logEntry) {
      this.logEntries.add(logEntry);
   }

   @Override
   public void close() {
   }

   public List<LogEntry> getLogEntries() {
      return this.logEntries;
   }
}
