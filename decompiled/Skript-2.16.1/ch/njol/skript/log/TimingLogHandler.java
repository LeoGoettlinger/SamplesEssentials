package ch.njol.skript.log;

public class TimingLogHandler extends LogHandler {
   private final long start = System.currentTimeMillis();

   @Override
   public LogHandler.LogResult log(LogEntry entry) {
      return LogHandler.LogResult.LOG;
   }

   public TimingLogHandler start() {
      return SkriptLogger.startLogHandler(this);
   }

   public long getStart() {
      return this.start;
   }

   public long getTimeTaken() {
      return System.currentTimeMillis() - this.start;
   }
}
