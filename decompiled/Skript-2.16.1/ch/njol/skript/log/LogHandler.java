package ch.njol.skript.log;

import ch.njol.util.OpenCloseable;
import java.io.Closeable;

public abstract class LogHandler implements Closeable, OpenCloseable {
   public abstract LogHandler.LogResult log(LogEntry var1);

   protected void onStop() {
   }

   public final void stop() {
      SkriptLogger.removeHandler(this);
      this.onStop();
   }

   public boolean isStopped() {
      return SkriptLogger.isStopped(this);
   }

   public LogHandler start() {
      SkriptLogger.startLogHandler(this);
      return this;
   }

   @Override
   public void open() {
      this.start();
   }

   @Override
   public void close() {
      this.stop();
   }

   public static enum LogResult {
      LOG,
      CACHED,
      DO_NOT_LOG;
   }
}
