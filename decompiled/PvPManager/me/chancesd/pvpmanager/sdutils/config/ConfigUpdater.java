package me.chancesd.pvpmanager.sdutils.config;

import com.google.common.base.Preconditions;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.LineBreak;

public class ConfigUpdater {
   private static final char SEPARATOR = '.';

   public static void update(Plugin plugin, String resourceName, File toUpdate, String... ignoredSections) throws IOException {
      update(plugin, resourceName, toUpdate, Arrays.asList(ignoredSections), Collections.emptyList());
   }

   public static void update(Plugin plugin, String resourceName, File toUpdate, List<String> ignoredSections, List<String> overrideSections) throws IOException {
      Preconditions.checkArgument(toUpdate.exists(), "The toUpdate file doesn't exist!");
      FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource(resourceName), StandardCharsets.UTF_8));
      FileConfiguration currentConfig = YamlConfiguration.loadConfiguration(toUpdate);
      Map<String, String> comments = parseComments(plugin, resourceName, defaultConfig);
      Map<String, String> ignoredSectionsValues = parseIgnoredSections(toUpdate, comments, ignoredSections == null ? Collections.emptyList() : ignoredSections);
      List<String> overrideSectionsArrayList = new ArrayList<>(overrideSections);
      overrideSectionsArrayList.removeIf(overrideSection -> defaultConfig.get(overrideSection) == null);
      StringWriter writer = new StringWriter();
      write(defaultConfig, currentConfig, new BufferedWriter(writer), comments, ignoredSectionsValues, overrideSectionsArrayList);
      String value = writer.toString();
      Path toUpdatePath = toUpdate.toPath();
      if (!value.equals(new String(Files.readAllBytes(toUpdatePath), StandardCharsets.UTF_8))) {
         Files.write(toUpdatePath, value.getBytes(StandardCharsets.UTF_8));
      }
   }

   private static void write(
      FileConfiguration defaultConfig,
      FileConfiguration currentConfig,
      BufferedWriter writer,
      Map<String, String> comments,
      Map<String, String> ignoredSectionsValues,
      List<String> ignoredSections
   ) throws IOException {
      FileConfiguration parserConfig = new YamlConfiguration();

      label52:
      for (String fullKey : defaultConfig.getKeys(true)) {
         String indents = KeyUtils.getIndents(fullKey, '.');
         if (ignoredSectionsValues.isEmpty() || !writeIgnoredSectionValueIfExists(ignoredSectionsValues, writer, fullKey)) {
            writeCommentIfExists(comments, writer, fullKey, indents);
            Object currentValue = currentConfig.get(fullKey);
            Object defaultValue = defaultConfig.get(fullKey);
            if (currentValue == null || !defaultValue.getClass().equals(currentValue.getClass())) {
               currentValue = defaultConfig.get(fullKey);
            }

            String[] splitFullKey = fullKey.split("[.]");
            String trailingKey = splitFullKey[splitFullKey.length - 1];

            for (String ignoredSection : ignoredSections) {
               if (fullKey.startsWith(ignoredSection)) {
                  Object newObj = defaultConfig.get(fullKey);
                  if (newObj instanceof ConfigurationSection) {
                     writeConfigurationSection(writer, indents, trailingKey, (ConfigurationSection)newObj);
                  } else if (newObj != null) {
                     writeYamlValue(parserConfig, writer, indents, trailingKey, newObj);
                  }
                  continue label52;
               }
            }

            if (currentValue instanceof ConfigurationSection) {
               writeConfigurationSection(writer, indents, trailingKey, (ConfigurationSection)currentValue);
            } else {
               writeYamlValue(parserConfig, writer, indents, trailingKey, currentValue);
            }
         }
      }

      String danglingComments = comments.get(null);
      if (danglingComments != null) {
         writer.write(danglingComments);
      }

      writer.close();
   }

   private static Map<String, String> parseComments(Plugin plugin, String resourceName, FileConfiguration defaultConfig) throws IOException {
      List<String> keys = new ArrayList<>(defaultConfig.getKeys(true));
      BufferedReader reader = new BufferedReader(new InputStreamReader(plugin.getResource(resourceName)));
      Map<String, String> comments = new LinkedHashMap<>();
      StringBuilder commentBuilder = new StringBuilder();
      KeyBuilder keyBuilder = new KeyBuilder(defaultConfig, '.');
      String currentValidKey = null;

      String line;
      while ((line = reader.readLine()) != null) {
         String trimmedLine = line.trim();
         if (!trimmedLine.startsWith("-")) {
            if (!trimmedLine.isEmpty() && !trimmedLine.startsWith("#")) {
               if (!line.startsWith(" ")) {
                  keyBuilder.clear();
                  currentValidKey = trimmedLine;
               }

               keyBuilder.parseLine(trimmedLine, true);
               String key = keyBuilder.toString();
               if (commentBuilder.length() > 0) {
                  comments.put(key, commentBuilder.toString());
                  commentBuilder.setLength(0);
               }

               int nextKeyIndex = keys.indexOf(keyBuilder.toString()) + 1;
               if (nextKeyIndex < keys.size()) {
                  String nextKey = keys.get(nextKeyIndex);

                  while (!keyBuilder.isEmpty() && !nextKey.startsWith(keyBuilder.toString())) {
                     keyBuilder.removeLastKey();
                  }

                  if (keyBuilder.isEmpty()) {
                     keyBuilder.parseLine(currentValidKey, false);
                  }
               }
            } else {
               commentBuilder.append(trimmedLine).append("\n");
            }
         }
      }

      reader.close();
      if (commentBuilder.length() > 0) {
         comments.put(null, commentBuilder.toString());
      }

      return comments;
   }

   private static Map<String, String> parseIgnoredSections(File toUpdate, Map<String, String> comments, List<String> ignoredSections) throws IOException {
      Map<String, String> ignoredSectionValues = new LinkedHashMap<>(ignoredSections.size());
      DumperOptions options = new DumperOptions();
      options.setLineBreak(LineBreak.UNIX);
      options.setDefaultFlowStyle(FlowStyle.BLOCK);
      Yaml yaml = new Yaml(new YamlConstructor(), new YamlRepresenter(), options);
      Map<Object, Object> root = (Map<Object, Object>)yaml.load(new FileReader(toUpdate));
      ignoredSections.forEach(section -> {
         String[] split = section.split("[.]");
         String key = split[split.length - 1];
         Map<Object, Object> map = getSection(section, root);
         if (!map.isEmpty()) {
            StringBuilder keyBuilder = new StringBuilder();

            for (int i = 0; i < split.length; i++) {
               if (i != split.length - 1) {
                  if (keyBuilder.length() > 0) {
                     keyBuilder.append('.');
                  }

                  keyBuilder.append(split[i]);
               }
            }

            ignoredSectionValues.put(section, buildIgnored(key, map, comments, keyBuilder, new StringBuilder(), yaml));
         }
      });
      return ignoredSectionValues;
   }

   private static Map<Object, Object> getSection(String fullKey, Map<Object, Object> root) {
      String[] keys = fullKey.split("[.]", 2);
      String key = keys[0];
      Object value = root.get(getKeyAsObject(key, root));
      if (!(value instanceof Map)) {
         return Collections.emptyMap();
      } else if (keys.length == 1) {
         if (value instanceof Map) {
            return root;
         } else {
            throw new IllegalArgumentException("Ignored sections must be a ConfigurationSection not a value!");
         }
      } else if (!(value instanceof Map)) {
         throw new IllegalArgumentException("Invalid ignored ConfigurationSection specified!");
      } else {
         return getSection(keys[1], (Map<Object, Object>)value);
      }
   }

   private static String buildIgnored(
      String fullKey, Map<Object, Object> ymlMap, Map<String, String> comments, StringBuilder keyBuilder, StringBuilder ignoredBuilder, Yaml yaml
   ) {
      String[] keys = fullKey.split("[.]", 2);
      String key = keys[0];
      Object originalKey = getKeyAsObject(key, ymlMap);
      if (keyBuilder.length() > 0) {
         keyBuilder.append(".");
      }

      keyBuilder.append(key);
      if (!ymlMap.containsKey(originalKey)) {
         if (keys.length == 1) {
            throw new IllegalArgumentException("Invalid ignored section: " + keyBuilder);
         } else {
            throw new IllegalArgumentException("Invalid ignored section: " + keyBuilder + "." + keys[1]);
         }
      } else {
         String comment = comments.get(keyBuilder.toString());
         String indents = KeyUtils.getIndents(keyBuilder.toString(), '.');
         if (comment != null) {
            ignoredBuilder.append(addIndentation(comment, indents)).append("\n");
         }

         ignoredBuilder.append(addIndentation(key, indents)).append(":");
         Object obj = ymlMap.get(originalKey);
         if (obj instanceof Map<Object, Object> map) {
            if (map.isEmpty()) {
               ignoredBuilder.append(" {}\n");
            } else {
               ignoredBuilder.append("\n");
            }

            StringBuilder preLoopKey = new StringBuilder(keyBuilder);

            for (Object o : map.keySet()) {
               buildIgnored(o.toString(), map, comments, keyBuilder, ignoredBuilder, yaml);
               keyBuilder = new StringBuilder(preLoopKey);
            }
         } else {
            writeIgnoredValue(yaml, obj, ignoredBuilder, indents);
         }

         return ignoredBuilder.toString();
      }
   }

   private static void writeIgnoredValue(Yaml yaml, Object toWrite, StringBuilder ignoredBuilder, String indents) {
      String yml = yaml.dump(toWrite);
      if (toWrite instanceof Collection && !((Collection)toWrite).isEmpty()) {
         ignoredBuilder.append("\n").append(addIndentation(yml, indents)).append("\n");
      } else {
         ignoredBuilder.append(" ").append(yml);
      }
   }

   private static String addIndentation(String s, String indents) {
      StringBuilder builder = new StringBuilder();
      String[] split = s.split("\n");

      for (String value : split) {
         if (builder.length() > 0) {
            builder.append("\n");
         }

         builder.append(indents).append(value);
      }

      return builder.toString();
   }

   private static void writeCommentIfExists(Map<String, String> comments, BufferedWriter writer, String fullKey, String indents) throws IOException {
      String comment = comments.get(fullKey);
      if (comment != null) {
         writer.write(indents + comment.substring(0, comment.length() - 1).replace("\n", "\n" + indents) + "\n");
      }
   }

   private static Object getKeyAsObject(String key, Map<Object, Object> sectionContext) {
      if (sectionContext.containsKey(key)) {
         return key;
      } else {
         try {
            Float keyFloat = Float.parseFloat(key);
            if (sectionContext.containsKey(keyFloat)) {
               return keyFloat;
            }
         } catch (NumberFormatException var6) {
         }

         try {
            Double keyDouble = Double.parseDouble(key);
            if (sectionContext.containsKey(keyDouble)) {
               return keyDouble;
            }
         } catch (NumberFormatException var5) {
         }

         try {
            Integer keyInteger = Integer.parseInt(key);
            if (sectionContext.containsKey(keyInteger)) {
               return keyInteger;
            }
         } catch (NumberFormatException var4) {
         }

         try {
            Long longKey = Long.parseLong(key);
            if (sectionContext.containsKey(longKey)) {
               return longKey;
            }
         } catch (NumberFormatException var3) {
         }

         return null;
      }
   }

   private static void writeYamlValue(FileConfiguration parserConfig, BufferedWriter bufferedWriter, String indents, String trailingKey, Object currentValue) throws IOException {
      if (currentValue instanceof List<String> list) {
         writeList(list, trailingKey, indents, new Yaml(), bufferedWriter);
      } else {
         parserConfig.set(trailingKey, currentValue);
         String yaml = parserConfig.saveToString();
         yaml = yaml.substring(0, yaml.length() - 1).replace("\n", "\n" + indents);
         String toWrite = indents + yaml + "\n";
         parserConfig.set(trailingKey, null);
         bufferedWriter.write(toWrite);
      }
   }

   private static void writeList(List<?> list, String actualKey, String prefixSpaces, Yaml yaml, BufferedWriter writer) throws IOException {
      writer.write(getListAsString(list, actualKey, prefixSpaces, yaml));
   }

   private static String getListAsString(List<?> list, String actualKey, String prefixSpaces, Yaml yaml) {
      StringBuilder builder = new StringBuilder(prefixSpaces).append(actualKey).append(":");
      if (list.isEmpty()) {
         builder.append(" []\n");
         return builder.toString();
      } else {
         builder.append("\n");

         for (int i = 0; i < list.size(); i++) {
            Object o = list.get(i);
            if (o instanceof String || o instanceof Character) {
               builder.append(prefixSpaces).append("  - '").append(o).append("'");
            } else if (o instanceof List) {
               builder.append(prefixSpaces).append("- ").append(yaml.dump(o));
            } else {
               builder.append(prefixSpaces).append("- ").append(o);
            }

            if (i != list.size()) {
               builder.append("\n");
            }
         }

         return builder.toString();
      }
   }

   private static boolean writeIgnoredSectionValueIfExists(Map<String, String> ignoredSectionsValues, BufferedWriter bufferedWriter, String fullKey) throws IOException {
      String ignored = ignoredSectionsValues.get(fullKey);
      if (ignored != null) {
         bufferedWriter.write(ignored);
         return true;
      } else {
         for (Entry<String, String> entry : ignoredSectionsValues.entrySet()) {
            if (KeyUtils.isSubKeyOf(entry.getKey(), fullKey, '.')) {
               return true;
            }
         }

         return false;
      }
   }

   private static void writeConfigurationSection(BufferedWriter bufferedWriter, String indents, String trailingKey, ConfigurationSection configurationSection) throws IOException {
      bufferedWriter.write(indents + trailingKey + ":");
      if (!configurationSection.getKeys(false).isEmpty()) {
         bufferedWriter.write("\n");
      } else {
         bufferedWriter.write(" {}\n");
      }
   }
}
