package de.theredend2000.advancedhunt.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class ConfigUpdater {
   private static final int INDENTATION_SIZE = 2;
   private static final int MARK_BUFFER_SIZE = 8192;
   private static final VersionComparator VERSION_COMPARATOR = new VersionComparator();

   public static void update(Plugin plugin, String resourceName, File file) {
      update(plugin, resourceName, file, null);
   }

   public static void update(Plugin plugin, String resourceName, File file, ConfigUpdater.MigrationCallback migrator) {
      FileConfiguration userConfig = YamlConfiguration.loadConfiguration(file);
      if (!file.exists()) {
         plugin.saveResource(resourceName, false);
      } else {
         String currentVersion = resolveVersion(plugin, userConfig.get("config-version"), file.getName(), "0");

         String newVersion;
         try {
            InputStream resource = plugin.getResource(resourceName);

            label144: {
               try {
                  if (resource != null) {
                     FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(resource, StandardCharsets.UTF_8));
                     newVersion = resolveVersion(plugin, defaultConfig.get("config-version"), resourceName, "1");
                     break label144;
                  }
               } catch (Throwable var20) {
                  if (resource != null) {
                     try {
                        resource.close();
                     } catch (Throwable var13) {
                        var20.addSuppressed(var13);
                     }
                  }

                  throw var20;
               }

               if (resource != null) {
                  resource.close();
               }

               return;
            }

            if (resource != null) {
               resource.close();
            }
         } catch (IOException var21) {
            plugin.getLogger().severe("Failed to read default config version: " + var21.getMessage());
            return;
         }

         if (VERSION_COMPARATOR.compare(currentVersion, newVersion) < 0) {
            if (migrator != null) {
               migrator.migrate(userConfig, currentVersion, newVersion);
            }

            plugin.getLogger().info("Updating " + resourceName + " from version " + currentVersion + " to " + newVersion + "...");
            createBackup(plugin, file);
            File tempFile = new File(file.getParentFile(), file.getName() + ".tmp");

            try {
               InputStream resource = plugin.getResource(resourceName);

               label155: {
                  try {
                     if (resource == null) {
                        plugin.getLogger().severe("Resource " + resourceName + " not found in JAR");
                        break label155;
                     }

                     List<String> newLines = updateLines(resource, userConfig);
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8));

                     try {
                        for (String line : newLines) {
                           writer.write(line);
                           writer.newLine();
                        }
                     } catch (Throwable var17) {
                        try {
                           writer.close();
                        } catch (Throwable var15) {
                           var17.addSuppressed(var15);
                        }

                        throw var17;
                     }

                     writer.close();

                     try {
                        Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                     } catch (AtomicMoveNotSupportedException var16) {
                        Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                     }

                     plugin.getLogger().info("Successfully updated " + resourceName);
                  } catch (Throwable var18) {
                     if (resource != null) {
                        try {
                           resource.close();
                        } catch (Throwable var14) {
                           var18.addSuppressed(var14);
                        }
                     }

                     throw var18;
                  }

                  if (resource != null) {
                     resource.close();
                  }

                  return;
               }

               if (resource != null) {
                  resource.close();
               }

               return;
            } catch (IOException var19) {
               plugin.getLogger().severe("Failed to update " + resourceName + ": " + var19.getMessage());
               if (tempFile.exists()) {
                  tempFile.delete();
               }
            }
         }
      }
   }

   private static void createBackup(Plugin plugin, File file) {
      try {
         File backup = new File(file.getParentFile(), file.getName() + ".bak");
         if (backup.exists()) {
            backup.delete();
         }

         Files.copy(file.toPath(), backup.toPath());
      } catch (IOException var3) {
         plugin.getLogger().warning("Failed to create backup for " + file.getName());
      }
   }

   private static String resolveVersion(Plugin plugin, Object rawValue, String sourceName, String fallback) {
      if (rawValue == null) {
         return fallback;
      } else {
         String normalized = String.valueOf(rawValue).trim();
         if (normalized.matches("\\d+(?:\\.\\d+)*")) {
            return normalizeVersion(normalized);
         } else {
            plugin.getLogger().warning("Invalid config-version '" + rawValue + "' in " + sourceName + "; using " + fallback + " instead.");
            return fallback;
         }
      }
   }

   private static String normalizeVersion(String version) {
      String normalized = version;

      while (normalized.endsWith(".0")) {
         normalized = normalized.substring(0, normalized.length() - 2);
      }

      return normalized;
   }

   private static List<String> updateLines(InputStream resource, FileConfiguration userConfig) throws IOException {
      List<String> lines = new ArrayList<>();
      BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8));

      try {
         List<String> pathStack = new ArrayList<>();

         String line;
         while ((line = reader.readLine()) != null) {
            if (shouldSkipLine(line)) {
               lines.add(line);
            } else {
               int indentation = getIndentation(line);
               int depth = indentation / 2;

               while (pathStack.size() > depth) {
                  pathStack.remove(pathStack.size() - 1);
               }

               String key = getKeyFromLine(line);
               if (key == null) {
                  lines.add(line);
               } else {
                  pathStack.add(key);
                  String fullPath = String.join(".", pathStack);
                  if (fullPath.equals("config-version")) {
                     lines.add(line);
                  } else if (userConfig.contains(fullPath)) {
                     Object userValue = userConfig.get(fullPath);
                     if (userValue instanceof ConfigurationSection) {
                        lines.add(line);
                     } else if (userValue instanceof List) {
                        writeList(lines, indentation, key, (List<?>)userValue);
                        skipListInResource(reader, indentation);
                     } else {
                        lines.add(getIndentationString(indentation) + key + ": " + formatValue(userValue));
                     }
                  } else {
                     lines.add(line);
                  }
               }
            }
         }
      } catch (Throwable var12) {
         try {
            reader.close();
         } catch (Throwable var11) {
            var12.addSuppressed(var11);
         }

         throw var12;
      }

      reader.close();
      return lines;
   }

   private static boolean shouldSkipLine(String line) {
      String trimmed = line.trim();
      return trimmed.isEmpty() || trimmed.startsWith("#");
   }

   private static void writeList(List<String> lines, int indentation, String key, List<?> list) {
      lines.add(getIndentationString(indentation) + key + ":");

      for (Object item : list) {
         if (item instanceof String) {
            lines.add(getIndentationString(indentation) + "  - \"" + escapeString((String)item) + "\"");
         } else {
            lines.add(getIndentationString(indentation) + "  - " + item);
         }
      }
   }

   private static void skipListInResource(BufferedReader reader, int indentation) throws IOException {
      reader.mark(8192);

      String nextLine;
      while ((nextLine = reader.readLine()) != null) {
         int nextIndent = getIndentation(nextLine);
         if (!nextLine.trim().isEmpty() && !nextLine.trim().startsWith("-") && nextIndent <= indentation) {
            reader.reset();
            break;
         }

         reader.mark(8192);
      }
   }

   private static String formatValue(Object value) {
      return value instanceof String ? "\"" + escapeString((String)value) + "\"" : String.valueOf(value);
   }

   private static int getIndentation(String line) {
      int count = 0;

      for (char c : line.toCharArray()) {
         if (c != ' ') {
            break;
         }

         count++;
      }

      return count;
   }

   private static String getIndentationString(int n) {
      return new String(new char[n]).replace("\u0000", " ");
   }

   private static String getKeyFromLine(String line) {
      String trimmed = line.trim();
      if (!trimmed.startsWith("-") && !trimmed.startsWith("#")) {
         int colonIndex = trimmed.indexOf(58);
         return colonIndex == -1 ? null : trimmed.substring(0, colonIndex);
      } else {
         return null;
      }
   }

   private static String escapeString(String str) {
      return str.replace("\"", "\\\"");
   }

   @FunctionalInterface
   public interface MigrationCallback {
      void migrate(FileConfiguration var1, String var2, String var3);
   }
}
