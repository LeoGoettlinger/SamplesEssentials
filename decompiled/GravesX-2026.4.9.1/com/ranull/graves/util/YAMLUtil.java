package com.ranull.graves.util;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class YAMLUtil {
   private YAMLUtil() {
   }

   public static boolean isValidYAML(File file) {
      return !file.getName().startsWith(".") && file.getName().endsWith(".yml");
   }

   @Nullable
   public static YAMLUtil.YamlParseError validateWithBukkit(@NotNull Plugin plugin, @NotNull File diskFile, @Nullable String jarResource) {
      List<String> diskLinesRaw = readDiskLines(diskFile);

      try {
         YamlConfiguration cfg = new YamlConfiguration();
         cfg.load(diskFile);
         return null;
      } catch (InvalidConfigurationException var13) {
         String msg = var13.getMessage() != null ? var13.getMessage() : var13.toString();
         YAMLUtil.LineCol lc = extractLineCol(msg);
         int line = lc.line1;
         int col = lc.col1;
         List<String> diskLines = expandTabs(diskLinesRaw);
         String diskSnippet = line > 0 ? buildSnippet(diskLines, line - 1, col > 0 ? col - 1 : -1, 2, 2) : null;
         String nearKey = line > 0 ? guessNearestKey(diskLinesRaw, line - 1) : null;
         String expected = null;
         if (jarResource != null && nearKey != null) {
            expected = buildJarContextAroundKey(plugin, jarResource, nearKey, 5, 5);
         }

         return new YAMLUtil.YamlParseError(line, col, msg, nearKey, diskSnippet, expected);
      } catch (Exception var14) {
         String msg = var14.getMessage() != null ? var14.getMessage() : var14.toString();
         return new YAMLUtil.YamlParseError(-1, -1, msg, null, null, null);
      }
   }

   public static void logParseError(@NotNull Plugin plugin, @NotNull String fileName, @NotNull YAMLUtil.YamlParseError err) {
      String where = err.nearKey != null ? " (near/at key '" + err.nearKey + "')" : "";
      String at = err.line > 0 ? " at line " + err.line + ", column " + err.column : "";
      plugin.getLogger().severe("Cannot load " + fileName + where + at + ".");
      if (err.diskSnippet != null) {
         plugin.getLogger().severe("--- Disk (" + fileName + ") ---\n" + err.diskSnippet);
      }

      if (err.expectedJarSnippet != null) {
         plugin.getLogger().severe("--- Expected ---\n" + err.expectedJarSnippet);
      } else if (err.nearKey != null) {
         plugin.getLogger().severe("--- Expected ---\n(Could not locate key '" + err.nearKey + "' in bundled resource)");
      }
   }

   @Nullable
   public static String buildSnippet(@NotNull List<String> lines, int line0, int col0, int above, int below) {
      if (!lines.isEmpty() && line0 >= 0) {
         int start = Math.max(0, line0 - Math.max(0, above));
         int end = Math.min(lines.size() - 1, line0 + Math.max(0, below));
         String prefix = "     |";
         StringBuilder sb = new StringBuilder();

         for (int i = start; i <= end; i++) {
            int lineNo = i + 1;
            sb.append(String.format("%4d |%s%n", lineNo, lines.get(i)));
            if (i == line0 && col0 >= 0) {
               sb.append("     |");
               sb.append(" ".repeat(col0));
               sb.append("^\n\n");
            }
         }

         return sb.toString();
      } else {
         return null;
      }
   }

   @Nullable
   public static String guessNearestKey(@NotNull List<String> rawLines, int errorLine0) {
      if (!rawLines.isEmpty() && errorLine0 >= 0) {
         Pattern keyPattern = Pattern.compile("^\\s*([A-Za-z0-9_.-]+)\\s*:(?:\\s*.*)?$");

         for (int i = Math.min(errorLine0, rawLines.size() - 1); i >= 0; i--) {
            String raw = rawLines.get(i);
            String t = raw.trim();
            if (!t.isEmpty() && !t.startsWith("#")) {
               Matcher m = keyPattern.matcher(raw);
               if (m.find()) {
                  return m.group(1);
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   @Nullable
   public static String buildJarContextAroundKey(@NotNull Plugin plugin, @NotNull String resourcePath, @NotNull String key, int above, int below) {
      String text = readResourceText(plugin, resourcePath);
      if (text == null) {
         return null;
      } else {
         List<String> rawLines = Arrays.asList(text.split("\\r?\\n", -1));
         List<String> lines = expandTabs(rawLines);
         int keyLine0 = findFirstKeyLine(rawLines, key);
         return keyLine0 < 0 ? null : buildSnippet(lines, keyLine0, -1, above, below);
      }
   }

   private static int findFirstKeyLine(@NotNull List<String> rawLines, @NotNull String key) {
      for (int i = 0; i < rawLines.size(); i++) {
         String raw = rawLines.get(i);
         String t = raw.trim();
         if (!t.isEmpty() && !t.startsWith("#") && t.startsWith(key + ":")) {
            return i;
         }
      }

      return -1;
   }

   @NotNull
   private static YAMLUtil.LineCol extractLineCol(@NotNull String msg) {
      Pattern p = Pattern.compile("line\\s+(\\d+)\\s*,\\s*column\\s+(\\d+)", 2);
      Matcher m = p.matcher(msg);
      if (m.find()) {
         int line = safeParseInt(m.group(1));
         int col = safeParseInt(m.group(2));
         return new YAMLUtil.LineCol(line, col);
      } else {
         return new YAMLUtil.LineCol(-1, -1);
      }
   }

   private static int safeParseInt(@Nullable String s) {
      if (s == null) {
         return -1;
      } else {
         try {
            return Integer.parseInt(s);
         } catch (NumberFormatException var2) {
            return -1;
         }
      }
   }

   @NotNull
   private static List<String> readDiskLines(@NotNull File file) {
      try {
         return Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
      } catch (Exception var2) {
         return List.of();
      }
   }

   @Nullable
   private static String readResourceText(@NotNull Plugin plugin, @NotNull String resourcePath) {
      try {
         String var4;
         try (InputStream in = plugin.getResource(resourcePath)) {
            if (in == null) {
               return null;
            }

            byte[] bytes = in.readAllBytes();
            var4 = new String(bytes, StandardCharsets.UTF_8);
         }

         return var4;
      } catch (Exception var7) {
         return null;
      }
   }

   @NotNull
   private static List<String> expandTabs(@NotNull List<String> rawLines) {
      return rawLines.stream().map(s -> s.replace("\t", "    ")).toList();
   }

   private static final class LineCol {
      final int line1;
      final int col1;

      LineCol(int line1, int col1) {
         this.line1 = line1;
         this.col1 = col1;
      }
   }

   public static class YamlParseError {
      public final int line;
      public final int column;
      @NotNull
      public final String message;
      @Nullable
      public final String nearKey;
      @Nullable
      public final String diskSnippet;
      @Nullable
      public final String expectedJarSnippet;

      public YamlParseError(
         int line, int column, @NotNull String message, @Nullable String nearKey, @Nullable String diskSnippet, @Nullable String expectedJarSnippet
      ) {
         this.line = line;
         this.column = column;
         this.message = message;
         this.nearKey = nearKey;
         this.diskSnippet = diskSnippet;
         this.expectedJarSnippet = expectedJarSnippet;
      }
   }
}
