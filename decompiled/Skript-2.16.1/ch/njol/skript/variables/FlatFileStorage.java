package ch.njol.skript.variables;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Version;
import ch.njol.util.NotifyingReference;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;

public class FlatFileStorage extends VariablesStorage {
   public static final Charset FILE_CHARSET = StandardCharsets.UTF_8;
   private static final long SAVE_TASK_DELAY = 6000L;
   private static final long SAVE_TASK_PERIOD = 6000L;
   private final NotifyingReference<PrintWriter> changesWriter = new NotifyingReference<>();
   private volatile boolean loaded = false;
   private static int REQUIRED_CHANGES_FOR_RESAVE = 1000;
   private final AtomicInteger changes = new AtomicInteger(0);
   @Nullable
   private Task saveTask;
   private boolean loadError = false;
   private static final Pattern CSV_LINE_PATTERN = Pattern.compile("(?<=^|,)\\s*(?:([^\",]*)|\"((?:[^\"]+|\"\")*)\")\\s*(?:,|$)");
   private static final Pattern CONTAINS_WHITESPACE = Pattern.compile("\\s");

   FlatFileStorage(String type) {
      super(type);
   }

   @Override
   protected boolean load_i(SectionNode sectionNode) {
      SkriptLogger.setNode(null);
      if (this.file == null) {
         assert false : this;

         return false;
      } else {
         IOException ioException = null;
         int unsuccessfulVariableCount = 0;
         StringBuilder invalid = new StringBuilder();
         Version v2_1 = new Version(2, 1);
         boolean update2_1 = false;

         try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(this.file.toPath()), FILE_CHARSET))) {
            int lineNum = 0;

            String line;
            while ((line = reader.readLine()) != null) {
               lineNum++;
               line = line.trim();
               if (!line.isEmpty() && !line.startsWith("#")) {
                  String[] split = splitCSV(line);
                  if (split == null || split.length != 3) {
                     Skript.error("invalid amount of commas in line " + lineNum + " ('" + line + "')");
                     if (invalid.length() != 0) {
                        invalid.append(", ");
                     }

                     invalid.append(split == null ? "<unknown>" : split[0]);
                     unsuccessfulVariableCount++;
                  } else if (split[1].equals("null")) {
                     Variables.variableLoaded(split[0], null, this);
                  } else {
                     Object deserializedValue;
                     if (update2_1) {
                        deserializedValue = Classes.deserialize(split[1], split[2]);
                     } else {
                        deserializedValue = Classes.deserialize(split[1], decode(split[2]));
                     }

                     if (deserializedValue == null) {
                        if (invalid.length() != 0) {
                           invalid.append(", ");
                        }

                        invalid.append(split[0]);
                        unsuccessfulVariableCount++;
                     } else {
                        Variables.variableLoaded(split[0], deserializedValue, this);
                     }
                  }
               } else if (line.startsWith("# version:")) {
                  try {
                     Version csvSkriptVersion = new Version(line.substring("# version:".length()).trim());
                     update2_1 = csvSkriptVersion.isSmallerThan(v2_1);
                  } catch (IllegalArgumentException var15) {
                  }
               }
            }
         } catch (IOException var17) {
            this.loadError = true;
            ioException = var17;
         }

         if (ioException != null || unsuccessfulVariableCount > 0 || update2_1) {
            if (unsuccessfulVariableCount > 0) {
               Skript.error(unsuccessfulVariableCount + " variable" + (unsuccessfulVariableCount == 1 ? "" : "s") + " could not be loaded!");
               Skript.error("Affected variables: " + invalid.toString());
            }

            if (ioException != null) {
               Skript.error("An I/O error occurred while loading the variables: " + ExceptionUtils.toString(ioException));
               Skript.error("This means that some to all variables could not be loaded!");
            }

            try {
               if (update2_1) {
                  Skript.info("[2.1] updating " + this.file.getName() + " to the new format...");
               }

               File backupFile = FileUtils.backup(this.file);
               Skript.info("Created a backup of " + this.file.getName() + " as " + backupFile.getName());
               this.loadError = false;
            } catch (IOException var13) {
               Skript.error("Could not backup " + this.file.getName() + ": " + var13.getMessage());
            }
         }

         if (update2_1) {
            this.saveVariables(false);
            Skript.info(this.file.getName() + " successfully updated.");
         }

         this.connect();
         this.saveTask = new Task(Skript.getInstance(), 6000L, 6000L, true) {
            @Override
            public void run() {
               if (FlatFileStorage.this.changes.get() >= FlatFileStorage.REQUIRED_CHANGES_FOR_RESAVE) {
                  FlatFileStorage.this.saveVariables(false);
                  FlatFileStorage.this.changes.set(0);
               }
            }
         };
         return ioException == null;
      }
   }

   @Override
   protected void allLoaded() {
   }

   @Override
   protected boolean requiresFile() {
      return true;
   }

   @Override
   protected File getFile(String fileName) {
      return new File(fileName);
   }

   @Override
   protected final void disconnect() {
      synchronized (this.connectionLock) {
         this.clearChangesQueue();
         synchronized (this.changesWriter) {
            PrintWriter printWriter = this.changesWriter.get();
            if (printWriter != null) {
               printWriter.close();
               this.changesWriter.set(null);
            }
         }
      }
   }

   @Override
   protected final boolean connect() {
      synchronized (this.connectionLock) {
         boolean var10000;
         synchronized (this.changesWriter) {
            assert this.file != null;

            if (this.changesWriter.get() != null) {
               return true;
            }

            try (FileOutputStream fos = new FileOutputStream(this.file, true)) {
               this.changesWriter.set(new PrintWriter(new OutputStreamWriter(fos, FILE_CHARSET)));
               this.loaded = true;
               return true;
            } catch (IOException var10) {
               Skript.exception(var10);
               var10000 = false;
            }
         }

         return var10000;
      }
   }

   @Override
   public void close() {
      this.clearChangesQueue();
      super.close();
      this.saveVariables(true);
   }

   @Override
   protected boolean save(String name, @Nullable String type, @Nullable byte[] value) {
      synchronized (this.connectionLock) {
         synchronized (this.changesWriter) {
            if (!this.loaded && type == null) {
               return true;
            }

            PrintWriter printWriter;
            while ((printWriter = this.changesWriter.get()) == null) {
               try {
                  this.changesWriter.wait();
               } catch (InterruptedException var10) {
                  Thread.currentThread().interrupt();
               }
            }

            writeCSV(printWriter, name, type, value == null ? "" : encode(value));
            printWriter.flush();
            this.changes.incrementAndGet();
         }

         return true;
      }
   }

   public final void saveVariables(boolean finalSave) {
      if (finalSave) {
         if (this.saveTask != null) {
            this.saveTask.cancel();
         }

         if (this.backupTask != null) {
            this.backupTask.cancel();
         }
      }

      try {
         Variables.getReadLock().lock();
         synchronized (this.connectionLock) {
            try {
               if (this.file == null) {
                  assert false : this;

                  return;
               }

               this.disconnect();
               if (this.loadError) {
                  try {
                     File backup = FileUtils.backup(this.file);
                     Skript.info("Created a backup of the old " + this.file.getName() + " as " + backup.getName());
                     this.loadError = false;
                  } catch (IOException var80) {
                     Skript.error("Could not backup the old " + this.file.getName() + ": " + ExceptionUtils.toString(var80));
                     Skript.error("No variables are saved!");
                     return;
                  }
               }

               File tempFile = new File(this.file.getParentFile(), this.file.getName() + ".temp");

               try (PrintWriter pw = new PrintWriter(tempFile, "UTF-8")) {
                  pw.println("# === Skript's variable storage ===");
                  pw.println("# Please do not modify this file manually!");
                  pw.println("#");
                  pw.println("# version: " + Skript.getVersion());
                  pw.println();
                  this.save(pw, "", Variables.getVariables());
                  pw.println();
                  pw.flush();
                  pw.close();
                  FileUtils.move(tempFile, this.file, true);
               } catch (IOException var79) {
                  Skript.error(
                     "Unable to make a final save of the database '"
                        + this.getUserConfigurationName()
                        + "' (no variables are lost): "
                        + ExceptionUtils.toString(var79)
                  );
               }
            } finally {
               if (!finalSave) {
                  this.connect();
               }
            }
         }
      } finally {
         Variables.getReadLock().unlock();
         boolean gotWriteLock = Variables.variablesLock.writeLock().tryLock();
         if (gotWriteLock) {
            try {
               Variables.processChangeQueue();
            } finally {
               Variables.variablesLock.writeLock().unlock();
            }
         }
      }
   }

   private void save(PrintWriter pw, String parent, TreeMap<String, Object> map) {
      if (!parent.startsWith("-")) {
         for (Entry<String, Object> childEntry : map.entrySet()) {
            Object childNode = childEntry.getValue();
            String childKey = childEntry.getKey();
            if (childNode != null) {
               if (childNode instanceof TreeMap) {
                  this.save(pw, parent + childKey + "::", (TreeMap<String, Object>)childNode);
               } else {
                  String name = childKey == null ? parent.substring(0, parent.length() - "::".length()) : parent + childKey;
                  if (!name.startsWith("-")) {
                     try {
                        for (VariablesStorage storage : Variables.STORAGES) {
                           if (storage.accept(name)) {
                              if (storage == this) {
                                 SerializedVariable.Value serializedValue = Classes.serialize(childNode);
                                 if (serializedValue != null) {
                                    writeCSV(pw, name, serializedValue.type, encode(serializedValue.data));
                                 }
                              }
                              break;
                           }
                        }
                     } catch (Exception var12) {
                        Skript.exception(var12, "Error saving variable named " + name);
                     }
                  }
               }
            }
         }
      }
   }

   static String encode(byte[] data) {
      char[] encoded = new char[data.length * 2];

      for (int i = 0; i < data.length; i++) {
         encoded[2 * i] = Character.toUpperCase(Character.forDigit((data[i] & 240) >>> 4, 16));
         encoded[2 * i + 1] = Character.toUpperCase(Character.forDigit(data[i] & 15, 16));
      }

      return new String(encoded);
   }

   static byte[] decode(String hex) {
      byte[] decoded = new byte[hex.length() / 2];

      for (int i = 0; i < decoded.length; i++) {
         decoded[i] = (byte)((Character.digit(hex.charAt(2 * i), 16) << 4) + Character.digit(hex.charAt(2 * i + 1), 16));
      }

      return decoded;
   }

   @Nullable
   static String[] splitCSV(String line) {
      Matcher matcher = CSV_LINE_PATTERN.matcher(line);
      int lastEnd = 0;

      ArrayList<String> result;
      for (result = new ArrayList<>(); matcher.find(); lastEnd = matcher.end()) {
         if (lastEnd != matcher.start()) {
            return null;
         }

         if (matcher.group(1) != null) {
            result.add(matcher.group(1).trim());
         } else {
            result.add(matcher.group(2).replace("\"\"", "\""));
         }
      }

      return lastEnd != line.length() ? null : result.toArray(new String[0]);
   }

   private static void writeCSV(PrintWriter printWriter, String... values) {
      assert values.length == 3;

      for (int i = 0; i < values.length; i++) {
         if (i != 0) {
            printWriter.print(", ");
         }

         String value = values[i];
         boolean escapingNeeded = value != null
            && (value.contains(",") || value.contains("\"") || value.contains("#") || CONTAINS_WHITESPACE.matcher(value).find());
         if (escapingNeeded) {
            value = "\"" + value.replace("\"", "\"\"") + "\"";
         }

         printWriter.print(value);
      }

      printWriter.println();
   }

   public static void setRequiredChangesForResave(int value) {
      if (value <= 0) {
         Skript.warning("Variable changes until save cannot be zero or less. Using default of 1000.");
         value = 1000;
      }

      REQUIRED_CHANGES_FOR_RESAVE = value;
   }
}
