package ch.njol.skript.log;

public class BlockingLogHandler extends LogHandler {
   @Override
   public LogHandler.LogResult log(LogEntry entry) {
      return LogHandler.LogResult.DO_NOT_LOG;
   }

   public BlockingLogHandler start() {
      SkriptLogger.startLogHandler(this);
      return this;
   }
}
