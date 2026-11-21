package ch.njol.skript.log;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class ParseLogHandler extends LogHandler {
   @Nullable
   private LogEntry error = null;
   private final List<LogEntry> log = new ArrayList<>();
   boolean printedErrorOrLog = false;

   @Internal
   @Contract("-> new")
   public ParseLogHandler backup() {
      ParseLogHandler copy = new ParseLogHandler();
      copy.error = this.error;
      copy.log.addAll(this.log);
      return copy;
   }

   @Internal
   public void restore(ParseLogHandler parseLogHandler) {
      this.error = parseLogHandler.error;
      this.log.clear();
      this.log.addAll(parseLogHandler.log);
   }

   @Override
   public LogHandler.LogResult log(LogEntry entry) {
      if (entry.getLevel().intValue() >= Level.SEVERE.intValue() && (this.error == null || entry.getQuality() > this.error.getQuality())) {
         this.error = entry;
      }

      this.log.add(entry);
      return LogHandler.LogResult.CACHED;
   }

   public ParseLogHandler start() {
      SkriptLogger.startLogHandler(this);
      return this;
   }

   public void error(String error, ErrorQuality quality) {
      this.log(new LogEntry(SkriptLogger.SEVERE, quality, error));
   }

   public void clear() {
      for (LogEntry e : this.log) {
         e.discarded("cleared");
      }

      this.log.clear();
   }

   public void clearError() {
      if (this.error != null) {
         this.error.discarded("cleared");
      }

      this.error = null;
   }

   public void printLog() {
      this.printLog(true);
   }

   public void printLog(boolean includeErrors) {
      this.printedErrorOrLog = true;
      this.stop();

      for (LogEntry logEntry : this.log) {
         if (includeErrors || logEntry.getLevel().intValue() < Level.SEVERE.intValue()) {
            SkriptLogger.log(logEntry);
         }
      }

      if (this.error != null) {
         this.error.discarded("not printed");
      }
   }

   public void printError() {
      this.printError(null);
   }

   public void printError(@Nullable String def) {
      this.printedErrorOrLog = true;
      this.stop();
      LogEntry error = this.error;
      if (error != null) {
         SkriptLogger.log(error);
      } else if (def != null) {
         SkriptLogger.log(new LogEntry(SkriptLogger.SEVERE, ErrorQuality.SEMANTIC_ERROR, def));
      }

      for (LogEntry e : this.log) {
         e.discarded("not printed");
      }
   }

   public void printError(String def, ErrorQuality quality) {
      this.printedErrorOrLog = true;
      this.stop();
      LogEntry error = this.error;
      if (error != null && error.quality >= quality.quality()) {
         SkriptLogger.log(error);
      } else {
         SkriptLogger.log(new LogEntry(SkriptLogger.SEVERE, quality, def));
      }

      for (LogEntry e : this.log) {
         e.discarded("not printed");
      }
   }

   public int getNumErrors() {
      return this.error == null ? 0 : 1;
   }

   public boolean hasError() {
      return this.error != null;
   }

   @Nullable
   public LogEntry getError() {
      return this.error;
   }
}
