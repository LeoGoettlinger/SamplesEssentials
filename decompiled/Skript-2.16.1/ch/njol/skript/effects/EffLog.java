package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.util.Kleenean;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Level;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

@Name("Log")
@Description({"Writes text into a .log file. Skript will write these files to /plugins/Skript/logs.", "NB: Using 'server.log' as the log file will write to the default server log. Omitting the log file altogether will log the message as '[Skript] [&lt;script&gt;.sk] &lt;message&gt;' in the server log."})
@Example.Examples({@Example("on join:\n\tlog \"%player% has just joined the server!\"\n"), @Example("on world change:\n\tlog \"Someone just went to %event-world%!\" to file \"worldlog/worlds.log\"\n"), @Example("on command:\n\tlog \"%player% just executed %full command%!\" to file \"server/commands.log\" with a severity of warning\n")})
@Since({"2.0, 2.9.0 (severities)"})
public class EffLog extends Effect {
   private static final File logsFolder = new File(Skript.getInstance().getDataFolder(), "logs");
   static final HashMap<String, PrintWriter> writers = new HashMap<>();
   private Expression<String> messages;
   @Nullable
   private Expression<String> files;
   private Level logLevel = Level.INFO;

   private static String getLogPrefix(Level logLevel) {
      String timestamp = SkriptConfig.formatDate(System.currentTimeMillis());
      return logLevel == Level.INFO ? "[" + timestamp + "]" : "[" + timestamp + " " + logLevel + "]";
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
      this.messages = (Expression<String>)exprs[0];
      this.files = (Expression<String>)exprs[1];
      if (parser.mark == 1) {
         this.logLevel = Level.WARNING;
      } else if (parser.mark == 2) {
         this.logLevel = Level.SEVERE;
      }

      return true;
   }

   @Override
   protected void execute(Event event) {
      for (String message : this.messages.getArray(event)) {
         if (this.files != null) {
            for (String logFile : this.files.getArray(event)) {
               logFile = logFile.toLowerCase(Locale.ENGLISH);
               if (!logFile.endsWith(".log")) {
                  logFile = logFile + ".log";
               }

               if (logFile.equals("server.log")) {
                  SkriptLogger.LOGGER.log(this.logLevel, message);
               } else {
                  PrintWriter logWriter = writers.get(logFile);
                  if (logWriter == null) {
                     File logFolder = new File(logsFolder, logFile);

                     try {
                        logFolder.getParentFile().mkdirs();
                        logWriter = new PrintWriter(new BufferedWriter(new FileWriter(logFolder, true)));
                        writers.put(logFile, logWriter);
                     } catch (IOException var13) {
                        Skript.error("Cannot write to log file '" + logFile + "' (" + logFolder.getPath() + "): " + ExceptionUtils.toString(var13));
                        return;
                     }
                  }

                  logWriter.println(getLogPrefix(this.logLevel) + " " + message);
                  logWriter.flush();
               }
            }
         } else {
            Trigger t = this.getTrigger();
            String scriptName = "---";
            if (t != null) {
               Script script = t.getScript();
               if (script != null) {
                  scriptName = script.getConfig().getFileName();
               }
            }

            SkriptLogger.LOGGER.log(this.logLevel, "[" + scriptName + "] " + message);
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "log "
         + this.messages.toString(event, debug)
         + (this.files != null ? " to " + this.files.toString(event, debug) : "")
         + (this.logLevel != Level.INFO ? "with severity " + this.logLevel.toString().toLowerCase(Locale.ENGLISH) : "");
   }

   static {
      Skript.registerEffect(EffLog.class, "log %strings% [(to|in) [file[s]] %-strings%] [with [the|a] severity [of] (1:warning|2:severe)]");
      Skript.closeOnDisable(() -> {
         for (PrintWriter pw : writers.values()) {
            pw.close();
         }
      });
   }
}
