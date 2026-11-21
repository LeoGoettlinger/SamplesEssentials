package github.nighter.smartspawner.libs.mariadb.util.log;

import java.util.HashMap;

public final class Loggers {
   public static final String FALLBACK_PROPERTY = "mariadb.logging.fallback";
   public static final String CONSOLE_DEBUG_PROPERTY = "mariadb.logging.fallback.console.debug";
   public static final String TEST_ENABLE_SLF4J = "mariadb.logging.slf4j.enable";
   public static final String NO_LOGGER_PROPERTY = "mariadb.logging.disable";
   private static Loggers.LoggerFactory LOGGER_FACTORY;

   public static Logger getLogger(String name) {
      return LOGGER_FACTORY.getLogger(name);
   }

   public static Logger getLogger(Class<?> cls) {
      return LOGGER_FACTORY.getLogger(cls.getName());
   }

   public static void init() {
      String name = Loggers.LoggerFactory.class.getName();
      Loggers.LoggerFactory loggerFactory = null;
      if (Boolean.parseBoolean(System.getProperty("mariadb.logging.disable", "false"))) {
         loggerFactory = new Loggers.NoLoggerFactory();
      } else {
         try {
            if (Boolean.parseBoolean(System.getProperty("mariadb.logging.slf4j.enable", "true"))) {
               Class.forName("org.slf4j.LoggerFactory");
               loggerFactory = new Loggers.Slf4JLoggerFactory();
            }
         } catch (ClassNotFoundException var4) {
         }

         if (loggerFactory == null) {
            if ("JDK".equalsIgnoreCase(System.getProperty("mariadb.logging.fallback"))) {
               loggerFactory = new Loggers.JdkLoggerFactory();
            } else {
               loggerFactory = new Loggers.ConsoleLoggerFactory();
            }
         }

         try {
            loggerFactory.getLogger(name);
         } catch (Throwable var3) {
         }
      }

      LOGGER_FACTORY = loggerFactory;
   }

   static {
      init();
   }

   private static final class ConsoleLoggerFactory implements Loggers.LoggerFactory {
      private static final HashMap<String, Logger> consoleLoggers = new HashMap<>();

      private ConsoleLoggerFactory() {
      }

      @Override
      public Logger getLogger(String name) {
         return consoleLoggers.computeIfAbsent(name, n -> new ConsoleLogger(n, System.getProperty("mariadb.logging.fallback.console.debug") != null));
      }
   }

   private static class JdkLoggerFactory implements Loggers.LoggerFactory {
      private JdkLoggerFactory() {
      }

      @Override
      public Logger getLogger(String name) {
         return new JdkLogger(java.util.logging.Logger.getLogger(name));
      }
   }

   private interface LoggerFactory {
      Logger getLogger(String var1);
   }

   private static class NoLoggerFactory implements Loggers.LoggerFactory {
      private NoLoggerFactory() {
      }

      @Override
      public Logger getLogger(String name) {
         return new NoLogger();
      }
   }

   private static class Slf4JLoggerFactory implements Loggers.LoggerFactory {
      private Slf4JLoggerFactory() {
      }

      @Override
      public Logger getLogger(String name) {
         return new Slf4JLogger(org.slf4j.LoggerFactory.getLogger(name));
      }
   }
}
