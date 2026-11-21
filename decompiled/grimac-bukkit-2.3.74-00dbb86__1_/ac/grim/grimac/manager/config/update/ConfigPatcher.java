package ac.grim.grimac.manager.config.update;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ConfigPatcher {
   private final File file;
   private final List<String> lines;
   private final Map<String, ConfigPatcher.NodePosition> locationMap;
   private static final Set<String> YAML_KEYWORDS_TO_QUOTE = new HashSet<>(
      Arrays.asList(
         "y",
         "Y",
         "yes",
         "Yes",
         "YES",
         "n",
         "N",
         "no",
         "No",
         "NO",
         "true",
         "True",
         "TRUE",
         "false",
         "False",
         "FALSE",
         "on",
         "On",
         "ON",
         "off",
         "Off",
         "OFF",
         "null",
         "Null",
         "NULL",
         "~"
      )
   );
   private static final String YAML_SPECIAL_CHARS = ":{}[]#|>&*!%@`'\",-?!^$";

   public ConfigPatcher(File configFile) throws IOException {
      this.file = configFile;
      this.lines = Files.readAllLines(configFile.toPath(), StandardCharsets.UTF_8);
      this.locationMap = new HashMap<>();
      this.mapFile();
   }

   public ConfigPatcher.NodePosition getNodePosition(String path) {
      return this.locationMap.get(path);
   }

   public void applyChange(ConfigPatcher.PendingChange change) {
      if (change.value() instanceof List) {
         this.replaceListBlock(change.position(), (List<?>)change.value());
      } else {
         this.replaceScalarValue(change.position(), change.value());
      }
   }

   private void mapFile() {
      List<String> pathStack = new ArrayList<>();
      List<Integer> indentStack = new ArrayList<>();
      Pattern keyPattern = Pattern.compile("^(\\s*)([^#:]+):(.*)");

      for (int i = 0; i < this.lines.size(); i++) {
         String line = this.lines.get(i);
         if (!line.trim().isEmpty() && !line.trim().startsWith("#")) {
            Matcher matcher = keyPattern.matcher(line);
            if (matcher.matches()) {
               int currentIndent = matcher.group(1).length();
               String key = matcher.group(2).trim();

               while (!indentStack.isEmpty() && currentIndent <= indentStack.get(indentStack.size() - 1)) {
                  pathStack.remove(pathStack.size() - 1);
                  indentStack.remove(indentStack.size() - 1);
               }

               pathStack.add(key);
               indentStack.add(currentIndent);
               String currentPath = String.join(".", pathStack);
               this.locationMap.put(currentPath, new ConfigPatcher.NodePosition(i, currentIndent));
            }
         }
      }
   }

   private void replaceScalarValue(ConfigPatcher.NodePosition position, Object value) {
      String line = this.lines.get(position.lineIndex);
      String valueStr = this.formatValue(value);
      Pattern linePattern = Pattern.compile("^(\\s*[^:]+:\\s*)(.*?)(\\s*#.*)?$");
      Matcher matcher = linePattern.matcher(line);
      if (matcher.matches()) {
         this.lines.set(position.lineIndex, matcher.group(1) + valueStr + (matcher.group(3) != null ? matcher.group(3) : ""));
      }
   }

   private void replaceListBlock(ConfigPatcher.NodePosition keyPosition, List<?> newList) {
      this.scrubInlineListFromKeyLine(keyPosition.lineIndex);
      int blockStartLine = -1;
      int blockEndLine = keyPosition.lineIndex + 1;
      int listIndent = -1;

      for (int i = keyPosition.lineIndex + 1; i < this.lines.size(); i++) {
         String line = this.lines.get(i);
         if (!line.trim().isEmpty() && !line.trim().startsWith("#")) {
            int currentIndent = this.getIndentation(line);
            if (currentIndent <= keyPosition.indent) {
               blockEndLine = i;
               break;
            }

            if (line.trim().startsWith("-")) {
               if (blockStartLine == -1) {
                  blockStartLine = i;
                  listIndent = currentIndent;
               }

               blockEndLine = i + 1;
            }
         } else if (blockStartLine != -1) {
            blockEndLine = i + 1;
         }
      }

      if (blockStartLine == -1) {
         blockStartLine = keyPosition.lineIndex + 1;
         blockEndLine = blockStartLine;
         listIndent = keyPosition.indent + 2;
      }

      String newBlock = this.generateListBlock(newList, listIndent);
      if (blockEndLine > blockStartLine) {
         this.lines.subList(blockStartLine, blockEndLine).clear();
      }

      if (!newBlock.isEmpty()) {
         this.lines.addAll(blockStartLine, Arrays.asList(newBlock.split("\r?\n")));
      }
   }

   private String generateListBlock(List<?> list, int indentation) {
      if (list.isEmpty()) {
         return "";
      } else {
         StringBuilder block = new StringBuilder();
         String indentStr = " ".repeat(indentation);

         for (int i = 0; i < list.size(); i++) {
            block.append(indentStr).append("- ").append(this.formatValue(list.get(i)));
            if (i < list.size() - 1) {
               block.append(System.lineSeparator());
            }
         }

         return block.toString();
      }
   }

   private String formatValue(Object value) {
      if (value == null) {
         return "null";
      } else if (!(value instanceof Boolean) && !(value instanceof Number)) {
         String s = value.toString();
         if (s.isEmpty()) {
            return "\"\"";
         } else {
            boolean needsQuotes = false;
            if (!YAML_KEYWORDS_TO_QUOTE.contains(s) && !s.startsWith(" ") && !s.endsWith(" ")) {
               try {
                  if (s.matches("^[+-]?([0-9]*[.])?[0-9]+$")) {
                     Double.parseDouble(s);
                     needsQuotes = true;
                  }
               } catch (NumberFormatException var8) {
               }
            } else {
               needsQuotes = true;
            }

            if (!needsQuotes) {
               for (char c : ":{}[]#|>&*!%@`'\",-?!^$".toCharArray()) {
                  if (s.indexOf(c) != -1) {
                     needsQuotes = true;
                     break;
                  }
               }
            }

            return needsQuotes ? "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"" : s;
         }
      } else {
         return value.toString();
      }
   }

   private int getIndentation(String line) {
      for (int i = 0; i < line.length(); i++) {
         if (line.charAt(i) != ' ') {
            return i;
         }
      }

      return 0;
   }

   private void scrubInlineListFromKeyLine(int lineIndex) {
      String line = this.lines.get(lineIndex);
      int colonIdx = line.indexOf(58);
      if (colonIdx >= 0) {
         String afterColon = line.substring(colonIdx + 1);
         int hashIdx = -1;
         boolean inString = false;
         char quote = 0;

         for (int i = 0; i < afterColon.length(); i++) {
            char c = afterColon.charAt(i);
            if (inString) {
               if (c == quote && (i == 0 || afterColon.charAt(i - 1) != '\\')) {
                  inString = false;
               }
            } else if (c != '"' && c != '\'') {
               if (c == '#') {
                  hashIdx = i;
                  break;
               }
            } else {
               inString = true;
               quote = c;
            }
         }

         String valuePart = (hashIdx >= 0 ? afterColon.substring(0, hashIdx) : afterColon).trim();
         String trailingComment = hashIdx >= 0 ? "  " + afterColon.substring(hashIdx).trim() : "";
         if (valuePart.startsWith("[") && valuePart.endsWith("]")) {
            this.lines.set(lineIndex, line.substring(0, colonIdx + 1) + trailingComment);
         }
      }
   }

   public void save() throws IOException {
      Files.write(this.file.toPath(), this.lines, StandardCharsets.UTF_8);
   }

   public record NodePosition(int lineIndex, int indent) {
   }

   public record PendingChange(ConfigPatcher.NodePosition position, Object value) implements Comparable<ConfigPatcher.PendingChange> {
      public int compareTo(ConfigPatcher.PendingChange other) {
         return Integer.compare(other.position.lineIndex(), this.position.lineIndex());
      }
   }
}
