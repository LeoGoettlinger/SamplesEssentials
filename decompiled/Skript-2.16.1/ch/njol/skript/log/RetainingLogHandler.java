package ch.njol.skript.log;

import ch.njol.skript.Skript;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Level;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class RetainingLogHandler extends LogHandler {
   private final Deque<LogEntry> log = new LinkedList<>();
   private int numErrors = 0;
   boolean printedErrorOrLog = false;

   @Internal
   @Contract("-> new")
   public RetainingLogHandler backup() {
      RetainingLogHandler copy = new RetainingLogHandler();
      copy.numErrors = this.numErrors;
      copy.printedErrorOrLog = this.printedErrorOrLog;
      copy.log.addAll(this.log);
      return copy;
   }

   @Internal
   public void restore(RetainingLogHandler copy) {
      this.numErrors = copy.numErrors;
      this.log.clear();
      this.log.addAll(copy.log);
   }

   @Override
   public LogHandler.LogResult log(LogEntry entry) {
      this.log.add(entry);
      if (entry.getLevel().intValue() >= Level.SEVERE.intValue()) {
         this.numErrors++;
      }

      this.printedErrorOrLog = false;
      return LogHandler.LogResult.CACHED;
   }

   @Override
   public void onStop() {
      if (!this.printedErrorOrLog && Skript.testing()) {
         SkriptLogger.LOGGER.warning("Retaining log wasn't instructed to print anything at " + SkriptLogger.getCaller());
      }
   }

   public RetainingLogHandler start() {
      SkriptLogger.startLogHandler(this);
      return this;
   }

   public final boolean printErrors() {
      return this.printErrors(null);
   }

   public final boolean printErrors(@Nullable String def) {
      return this.printErrors(def, ErrorQuality.SEMANTIC_ERROR);
   }

   public final boolean printErrors(@Nullable String def, ErrorQuality quality) {
      assert !this.printedErrorOrLog;

      this.printedErrorOrLog = true;
      this.stop();
      boolean hasError = false;

      for (LogEntry e : this.log) {
         if (e.getLevel().intValue() >= Level.SEVERE.intValue()) {
            SkriptLogger.log(e);
            hasError = true;
         } else {
            e.discarded("not printed");
         }
      }

      if (!hasError && def != null) {
         SkriptLogger.log(SkriptLogger.SEVERE, def);
      }

      return hasError;
   }

   public final boolean printErrors(CommandSender recipient, @Nullable String def) {
      assert !this.printedErrorOrLog;

      this.printedErrorOrLog = true;
      this.stop();
      boolean hasError = false;

      for (LogEntry e : this.log) {
         if (e.getLevel().intValue() >= Level.SEVERE.intValue()) {
            SkriptLogger.sendFormatted(recipient, e.toFormattedString());
            e.logged();
            hasError = true;
         } else {
            e.discarded("not printed");
         }
      }

      if (!hasError && def != null) {
         SkriptLogger.sendFormatted(recipient, def);
      }

      return hasError;
   }

   public final void printLog() {
      assert !this.printedErrorOrLog;

      this.printedErrorOrLog = true;
      this.stop();
      SkriptLogger.logAll(this.log);
   }

   public boolean hasErrors() {
      return this.numErrors != 0;
   }

   @Nullable
   public LogEntry getFirstError() {
      for (LogEntry e : this.log) {
         if (e.getLevel().intValue() >= Level.SEVERE.intValue()) {
            return e;
         }
      }

      return null;
   }

   public LogEntry getFirstError(String def) {
      for (LogEntry e : this.log) {
         if (e.getLevel().intValue() >= Level.SEVERE.intValue()) {
            return e;
         }
      }

      return new LogEntry(SkriptLogger.SEVERE, def);
   }

   public void clear() {
      for (LogEntry e : this.log) {
         e.discarded("cleared");
      }

      this.log.clear();
      this.numErrors = 0;
   }

   public int size() {
      return this.log.size();
   }

   public Collection<LogEntry> getLog() {
      this.printedErrorOrLog = true;
      return Collections.unmodifiableCollection(this.log);
   }

   public Collection<LogEntry> getErrors() {
      Collection<LogEntry> r = new ArrayList<>();

      for (LogEntry e : this.log) {
         if (e.getLevel().intValue() >= Level.SEVERE.intValue()) {
            r.add(e);
         }
      }

      return r;
   }

   public int getNumErrors() {
      return this.numErrors;
   }
}
