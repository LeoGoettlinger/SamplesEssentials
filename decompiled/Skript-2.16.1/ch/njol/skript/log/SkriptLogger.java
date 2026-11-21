package ch.njol.skript.log;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.parser.ParserInstance;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.text.TextComponentParser;

public abstract class SkriptLogger {
   public static final Level SEVERE = Level.SEVERE;
   private static Verbosity verbosity = Verbosity.NORMAL;
   private static boolean debug;
   public static final Level DEBUG = Level.INFO;
   public static final Logger LOGGER = Bukkit.getServer() != null ? Bukkit.getLogger() : Logger.getLogger("global");

   private static HandlerList getHandlers() {
      return ParserInstance.get().getHandlers();
   }

   public static RetainingLogHandler startRetainingLog() {
      return new RetainingLogHandler().start();
   }

   public static ParseLogHandler startParseLogHandler() {
      return new ParseLogHandler().start();
   }

   public static <T extends LogHandler> T startLogHandler(T h) {
      getHandlers().add(h);
      return h;
   }

   static void removeHandler(LogHandler h) {
      HandlerList handlers = getHandlers();
      if (handlers.contains(h)) {
         if (!h.equals(handlers.remove())) {
            int i = 1;

            while (!h.equals(handlers.remove())) {
               i++;
            }

            LOGGER.severe(
               "[Skript] "
                  + i
                  + " log handler"
                  + (i == 1 ? " was" : "s were")
                  + " not stopped properly! (at "
                  + getCaller()
                  + ") [if you're a server admin and you see this message please file a bug report at https://github.com/SkriptLang/skript/issues if there is not already one]"
            );
         }
      }
   }

   static boolean isStopped(LogHandler h) {
      return !getHandlers().contains(h);
   }

   @Nullable
   static StackTraceElement getCaller() {
      for (StackTraceElement e : new Exception().getStackTrace()) {
         if (!e.getClassName().startsWith(SkriptLogger.class.getPackage().getName())) {
            return e;
         }
      }

      return null;
   }

   public static void setVerbosity(Verbosity v) {
      verbosity = v;
      debug = v.compareTo(Verbosity.DEBUG) >= 0;
   }

   public static boolean debug() {
      return debug;
   }

   public static void setNode(@Nullable Node node) {
      ParserInstance.get().setNode(node);
   }

   @Nullable
   public static Node getNode() {
      return ParserInstance.get().getNode();
   }

   public static void log(Level level, String message) {
      log(new LogEntry(level, message, getNode()));
   }

   public static void log(@Nullable LogEntry entry) {
      if (entry != null) {
         if (Skript.testing() && getNode() != null && getNode().debug()) {
            System.out.print("---> " + entry.level + "/" + ErrorQuality.get(entry.quality) + ": " + entry + " ::" + LogEntry.findCaller());
         }

         for (LogHandler h : getHandlers()) {
            LogHandler.LogResult r = h.log(entry);
            switch (r) {
               case CACHED:
                  return;
               case DO_NOT_LOG:
                  entry.discarded("denied by " + h);
                  return;
               case LOG:
            }
         }

         entry.logged();
         sendFormatted(Bukkit.getConsoleSender(), Skript.getSkriptPrefix() + entry.toFormattedString());
      }
   }

   public static void logAll(Collection<LogEntry> entries) {
      entries.forEach(SkriptLogger::log);
   }

   public static void logTracked(Level level, String message, ErrorQuality quality) {
      log(new LogEntry(level, quality.quality(), message, getNode(), true));
   }

   public static boolean log(Verbosity minVerb) {
      return minVerb.compareTo(verbosity) <= 0;
   }

   public static void sendFormatted(CommandSender commandSender, String message) {
      if (commandSender instanceof ConsoleCommandSender) {
         for (String s : message.split("\n")) {
            commandSender.sendMessage(TextComponentParser.instance().parse(s));
         }
      } else {
         commandSender.sendMessage(TextComponentParser.instance().parse(message));
      }
   }
}
