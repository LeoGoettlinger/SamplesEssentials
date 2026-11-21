package de.oliver.fancyanalytics.logger;

import de.oliver.fancyanalytics.logger.appender.Appender;
import de.oliver.fancyanalytics.logger.appender.ConsoleAppender;
import de.oliver.fancyanalytics.logger.middleware.Middleware;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FancyLogger {
   private static final FancyLogger GLOBAL_LOGGER = new FancyLogger("FancyLogger-Global");
   private static final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "FancyLogger");
      t.setDaemon(true);
      return t;
   });
   protected final String name;
   protected final List<Appender> defaultAppenders;
   protected final List<Middleware> middlewares;
   protected LogLevel currentLevel;

   public FancyLogger(String name) {
      this.name = name;
      this.currentLevel = LogLevel.INFO;
      this.defaultAppenders = new ArrayList<>();
      this.defaultAppenders.add(new ConsoleAppender());
      this.middlewares = new ArrayList<>();
   }

   public FancyLogger(String name, LogLevel currentLevel, List<Appender> appenders, List<Middleware> middlewares) {
      this.currentLevel = currentLevel;
      this.middlewares = new ArrayList<>(middlewares);
      this.defaultAppenders = new ArrayList<>(appenders);
      this.name = name;
   }

   public static FancyLogger getGlobalLogger() {
      return GLOBAL_LOGGER;
   }

   public void log(LogLevel logLevel, LogEntry logEntry) {
      this.log(logLevel, logEntry, this.defaultAppenders);
   }

   public void log(LogLevel logLevel, LogEntry logEntry, List<Appender> appender) {
      LogEntry entry = logEntry;

      for (Middleware middleware : this.middlewares) {
         entry = middleware.process(entry);
         if (entry == null) {
            return;
         }
      }

      if (logLevel.ordinal() >= this.currentLevel.ordinal()) {
         try {
            for (Appender a : appender) {
               if (!executor.isShutdown() && !executor.isTerminated()) {
                  executor.submit(() -> a.append(logEntry));
               } else {
                  a.append(logEntry);
               }
            }
         } catch (Exception var7) {
            var7.printStackTrace();
         }
      }
   }

   public String getName() {
      return this.name;
   }

   public void addAppender(Appender appender) {
      this.defaultAppenders.add(appender);
   }

   public void addMiddlware(Middleware middleware) {
      this.middlewares.add(middleware);
   }

   public void setCurrentLevel(LogLevel currentLevel) {
      this.currentLevel = currentLevel;
   }

   static {
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
         executor.shutdown();

         try {
            if (!executor.awaitTermination(5L, TimeUnit.SECONDS)) {
               executor.shutdownNow();
            }
         } catch (InterruptedException var1) {
            executor.shutdownNow();
         }
      }));
   }
}
