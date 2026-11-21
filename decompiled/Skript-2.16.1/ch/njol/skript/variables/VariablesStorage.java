package ch.njol.skript.variables;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.jetbrains.annotations.Nullable;

public abstract class VariablesStorage implements Closeable {
   private static final int QUEUE_SIZE = 1000;
   private static final int FIRST_WARNING = 300;
   final LinkedBlockingQueue<SerializedVariable> changesQueue = new LinkedBlockingQueue<>(1000);
   protected volatile boolean closed = false;
   private String databaseName;
   private final String databaseType;
   @Nullable
   protected File file;
   @Nullable
   private Pattern variableNamePattern;
   private final Thread writeThread;
   private static final Set<File> registeredFiles = new HashSet<>();
   protected final Object connectionLock = new Object();
   @Nullable
   protected Task backupTask = null;
   private static final int WARNING_INTERVAL = 10;
   private static final int ERROR_INTERVAL = 10;
   private long lastWarning = Long.MIN_VALUE;
   private long lastError = Long.MIN_VALUE;

   protected VariablesStorage(String type) {
      assert type != null;

      this.databaseType = type;
      this.writeThread = Skript.newThread(() -> {
         while (!this.closed) {
            try {
               SerializedVariable variable = this.changesQueue.take();
               SerializedVariable.Value value = variable.value;
               if (value != null) {
                  this.save(variable.name, value.type, value.data);
               } else {
                  this.save(variable.name, null, null);
               }
            } catch (InterruptedException var3) {
            }
         }
      }, "Skript variable save thread for database '" + type + "'");
   }

   protected final String getUserConfigurationName() {
      return this.databaseName;
   }

   protected final String getDatabaseType() {
      return this.databaseType;
   }

   @Nullable
   protected String getValue(SectionNode sectionNode, String key) {
      return this.getValue(sectionNode, key, String.class);
   }

   @Nullable
   protected <T> T getValue(SectionNode sectionNode, String key, Class<T> type) {
      String rawValue = sectionNode.getValue(key);
      if (rawValue == null) {
         Skript.error("The config is missing the entry for '" + key + "' in the database '" + this.databaseName + "'");
         return null;
      } else {
         Object var7;
         try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
            T parsedValue = Classes.parse(rawValue, type, ParseContext.CONFIG);
            if (parsedValue == null) {
               log.printError(
                  "The entry for '"
                     + key
                     + "' in the database '"
                     + this.databaseName
                     + "' must be "
                     + Classes.getSuperClassInfo(type).getName().withIndefiniteArticle()
               );
            } else {
               log.printLog();
            }

            var7 = parsedValue;
         }

         return (T)var7;
      }
   }

   public final boolean load(SectionNode sectionNode) {
      this.databaseName = sectionNode.getKey();
      String pattern = this.getValue(sectionNode, "pattern");
      if (pattern == null) {
         return false;
      } else {
         try {
            this.variableNamePattern = !pattern.equals(".*") && !pattern.equals(".+") ? Pattern.compile(pattern) : null;
         } catch (PatternSyntaxException var11) {
            Skript.error("Invalid pattern '" + pattern + "': " + var11.getLocalizedMessage());
            return false;
         }

         if (this.requiresFile()) {
            String fileName = this.getValue(sectionNode, "file");
            if (fileName == null) {
               return false;
            }

            this.file = this.getFile(fileName).getAbsoluteFile();
            if (this.file.exists() && !this.file.isFile()) {
               Skript.error("The database file '" + this.file.getName() + "' must be an actual file, not a directory.");
               return false;
            }

            try {
               this.file.createNewFile();
            } catch (IOException var10) {
               Skript.error("Cannot create the database file '" + this.file.getName() + "': " + var10.getLocalizedMessage());
               return false;
            }

            if (!this.file.canWrite()) {
               Skript.error("Cannot write to the database file '" + this.file.getName() + "'!");
               return false;
            }

            if (!this.file.canRead()) {
               Skript.error("Cannot read from the database file '" + this.file.getName() + "'!");
               return false;
            }

            if (registeredFiles.contains(this.file)) {
               Skript.error("Database `" + this.databaseName + "` failed to load. The file `" + fileName + "` is already registered to another database.");
               return false;
            }

            registeredFiles.add(this.file);
            if (!"0".equals(this.getValue(sectionNode, "backup interval"))) {
               Timespan backupInterval = this.getValue(sectionNode, "backup interval", Timespan.class);
               int toKeep = this.getValue(sectionNode, "backups to keep", Integer.class);
               boolean removeBackups = false;
               boolean startBackup = true;
               if (backupInterval != null) {
                  if (toKeep == 0) {
                     startBackup = false;
                  } else if (toKeep >= 1) {
                     removeBackups = true;
                  }
               }

               if (startBackup) {
                  this.startBackupTask(backupInterval, removeBackups, toKeep);
               } else {
                  try {
                     FileUtils.backupPurge(this.file, toKeep);
                  } catch (IOException var9) {
                     Skript.error("Variables backup wipe failed: " + var9.getLocalizedMessage());
                  }
               }
            }
         }

         if (!this.load_i(sectionNode)) {
            return false;
         } else {
            this.writeThread.start();
            Skript.closeOnDisable(this);
            return true;
         }
      }
   }

   protected abstract boolean load_i(SectionNode var1);

   protected abstract void allLoaded();

   protected abstract boolean requiresFile();

   protected abstract File getFile(String var1);

   protected abstract boolean connect();

   protected abstract void disconnect();

   public void startBackupTask(Timespan backupInterval, final boolean removeBackups, final int toKeep) {
      if (this.file != null && backupInterval.getAs(Timespan.TimePeriod.TICK) != 0L) {
         this.backupTask = new Task(Skript.getInstance(), backupInterval.getAs(Timespan.TimePeriod.TICK), backupInterval.getAs(Timespan.TimePeriod.TICK), true) {
            @Override
            public void run() {
               synchronized (VariablesStorage.this.connectionLock) {
                  VariablesStorage.this.disconnect();

                  try {
                     FileUtils.backup(VariablesStorage.this.file);
                     if (removeBackups) {
                        try {
                           FileUtils.backupPurge(VariablesStorage.this.file, toKeep);
                        } catch (IllegalArgumentException | IOException var9) {
                           Skript.error("Automatic variables backup purge failed: " + var9.getLocalizedMessage());
                        }
                     }
                  } catch (IOException var10) {
                     Skript.error("Automatic variables backup failed: " + var10.getLocalizedMessage());
                  } finally {
                     VariablesStorage.this.connect();
                  }
               }
            }
         };
      }
   }

   boolean accept(@Nullable String var) {
      return var == null ? false : this.variableNamePattern == null || this.variableNamePattern.matcher(var).matches();
   }

   @Nullable
   public Pattern getNamePattern() {
      return this.variableNamePattern;
   }

   final void save(SerializedVariable var) {
      if (this.changesQueue.size() > 300 && this.lastWarning < System.currentTimeMillis() - 10000L) {
         Skript.warning(
            "Cannot write variables to the database '"
               + this.databaseName
               + "' at sufficient speed; server performance may suffer and many variables will be lost if the server crashes. (this warning will be repeated at most once every 10 seconds)"
         );
         this.lastWarning = System.currentTimeMillis();
      }

      if (!this.changesQueue.offer(var)) {
         if (this.lastError < System.currentTimeMillis() - 10000L) {
            Skript.error(
               "Skript cannot save any variables to the database '"
                  + this.databaseName
                  + "'. The server will hang and may crash if no more variables can be saved."
            );
            this.lastError = System.currentTimeMillis();
         }

         while (true) {
            try {
               this.changesQueue.put(var);
               break;
            } catch (InterruptedException var3) {
            }
         }
      }
   }

   @Override
   public void close() {
      while (this.changesQueue.size() > 0) {
         try {
            Thread.sleep(10L);
         } catch (InterruptedException var2) {
         }
      }

      this.closed = true;
      this.writeThread.interrupt();
   }

   protected void clearChangesQueue() {
      this.changesQueue.clear();
   }

   protected abstract boolean save(String var1, @Nullable String var2, @Nullable byte[] var3);
}
