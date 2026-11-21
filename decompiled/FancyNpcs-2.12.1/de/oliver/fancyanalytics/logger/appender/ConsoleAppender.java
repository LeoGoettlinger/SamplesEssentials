package de.oliver.fancyanalytics.logger.appender;

import de.oliver.fancyanalytics.logger.LogEntry;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import org.jetbrains.annotations.NotNull;

public class ConsoleAppender implements Appender {
   private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
   private final PrintStream out;
   @NotNull
   private final String format;

   public ConsoleAppender(@NotNull String format) {
      this.format = format;
      this.out = new PrintStream(new OutputStream() {
         @Override
         public void write(int b) {
            System.out.write(b);
         }
      });
   }

   public ConsoleAppender() {
      this("[{loggerName}] {timestamp} {logLevel}: {message}");
   }

   @Override
   public void append(LogEntry logEntry) {
      String message = this.format;
      message = message.replace("{loggerName}", logEntry.loggerName());
      message = message.replace("{timestamp}", sdf.format(logEntry.timestamp()));
      message = message.replace("{threadName}", logEntry.properties().get("thread_name").toString());
      message = message.replace("{logLevel}", logEntry.logLevel().name());
      message = message.replace("{message}", logEntry.message());
      if (logEntry.properties().get("throwable") != null) {
         message = message + " | Exception: " + logEntry.properties().get("throwable");
      }

      this.out.println(message);
   }

   @Override
   public void close() {
   }
}
