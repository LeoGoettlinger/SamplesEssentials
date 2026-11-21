package ch.njol.skript.log;

import java.util.logging.Level;

public class CountingLogHandler extends LogHandler {
   private final int minimum;
   private int count;

   public CountingLogHandler(Level minimum) {
      this.minimum = minimum.intValue();
   }

   @Override
   public LogHandler.LogResult log(LogEntry entry) {
      if (entry.level.intValue() >= this.minimum) {
         this.count++;
      }

      return LogHandler.LogResult.LOG;
   }

   public CountingLogHandler start() {
      SkriptLogger.startLogHandler(this);
      return this;
   }

   public int getCount() {
      return this.count;
   }
}
