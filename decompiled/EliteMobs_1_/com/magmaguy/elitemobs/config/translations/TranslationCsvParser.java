package com.magmaguy.elitemobs.config.translations;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TranslationCsvParser {
   private static final Pattern LIST_INDEX_PATTERN = Pattern.compile("^(.+)\\[(\\d+)]$");
   private static final int MAX_LIST_ENTRIES = 16384;
   private static final long MAX_EXPANDED_CELLS = 1000000L;

   public static TranslationCsvParser.TranslationData parse(File file) throws IOException {
      return parse(file.toPath());
   }

   public static TranslationCsvParser.TranslationData parse(Path path) throws IOException {
      List<String[]> rows = new ArrayList<>();

      try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
         StringBuilder currentRow = new StringBuilder();
         boolean inQuotes = false;
         boolean firstLine = true;

         String line;
         while ((line = reader.readLine()) != null) {
            if (firstLine) {
               firstLine = false;
               if (line.length() > 0 && line.charAt(0) == '\ufeff') {
                  line = line.substring(1);
               }
            }

            if ((currentRow.length() != 0 || !line.trim().isEmpty()) && (currentRow.length() != 0 || !line.trim().startsWith("#"))) {
               currentRow.append(line);

               for (char c : line.toCharArray()) {
                  if (c == '"') {
                     inQuotes = !inQuotes;
                  }
               }

               if (!inQuotes) {
                  String[] fields = parseCSVRow(currentRow.toString());
                  if (fields.length > 0) {
                     rows.add(fields);
                  }

                  currentRow = new StringBuilder();
               } else {
                  currentRow.append("\n");
               }
            }
         }

         if (inQuotes) {
            throw new IOException("CSV contains an unterminated quoted field");
         }
      }

      if (rows.isEmpty()) {
         throw new IOException("CSV file is empty or has no valid rows");
      } else {
         String[] header = rows.get(0);
         if (header.length < 2) {
            throw new IOException("CSV header must have at least 'key' and one language column");
         } else if (!"key".equalsIgnoreCase(header[0].trim())) {
            throw new IOException("CSV header must start with a 'key' column");
         } else {
            List<String> languages = new ArrayList<>();

            for (int i = 1; i < header.length; i++) {
               String language = header[i].trim().toLowerCase(Locale.ROOT);
               if (language.isBlank()) {
                  throw new IOException("CSV contains a blank language column");
               }

               if (languages.contains(language)) {
                  throw new IOException("CSV contains duplicate language column '" + language + "'");
               }

               languages.add(language);
            }

            TranslationCsvParser.TranslationData data = new TranslationCsvParser.TranslationData(languages);
            Map<String, Map<String, TreeMap<Integer, String>>> indexedEntries = new LinkedHashMap<>();

            for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
               String[] row = rows.get(rowIndex);
               if (row.length != 0 && !row[0].trim().isEmpty()) {
                  if (row.length > header.length) {
                     throw new IOException("CSV row " + (rowIndex + 1) + " has more columns than the header");
                  }

                  String rawKey = row[0].trim();
                  Matcher matcher = LIST_INDEX_PATTERN.matcher(rawKey);
                  if (matcher.matches()) {
                     String baseKey = matcher.group(1);
                     if (data.hasKey(baseKey)) {
                        throw new IOException("CSV row " + (rowIndex + 1) + " mixes scalar and list entries for " + baseKey);
                     }

                     int index;
                     try {
                        index = Integer.parseInt(matcher.group(2));
                     } catch (NumberFormatException var20) {
                        throw new IOException("CSV row " + (rowIndex + 1) + " has an invalid list index: " + rawKey, var20);
                     }

                     if (index >= 16384) {
                        throw new IOException("CSV row " + (rowIndex + 1) + " exceeds 16384 entries per translation list: " + rawKey);
                     }

                     for (int langIdx = 0; langIdx < languages.size() && langIdx + 1 < row.length; langIdx++) {
                        String languagex = languages.get(langIdx);
                        String value = row[langIdx + 1];
                        indexedEntries.computeIfAbsent(baseKey, k -> new LinkedHashMap<>()).computeIfAbsent(languagex, k -> new TreeMap<>()).put(index, value);
                     }
                  } else {
                     if (indexedEntries.containsKey(rawKey)) {
                        throw new IOException("CSV row " + (rowIndex + 1) + " mixes scalar and list entries for " + rawKey);
                     }

                     for (int langIdx = 0; langIdx < languages.size() && langIdx + 1 < row.length; langIdx++) {
                        String languagex = languages.get(langIdx);
                        String value = row[langIdx + 1];
                        data.set(rawKey, languagex, value);
                     }
                  }
               }
            }

            long expandedCells = 0L;

            for (Entry<String, Map<String, TreeMap<Integer, String>>> keyEntry : indexedEntries.entrySet()) {
               String baseKeyx = keyEntry.getKey();

               for (Entry<String, TreeMap<Integer, String>> langEntry : keyEntry.getValue().entrySet()) {
                  String languagex = langEntry.getKey();
                  TreeMap<Integer, String> indexMap = langEntry.getValue();
                  expandedCells += indexMap.lastKey().intValue() + 1L;
                  if (expandedCells > 1000000L) {
                     throw new IOException("CSV indexed translations exceed 1000000 expanded cells at " + baseKeyx + " / " + languagex);
                  }

                  List<String> list = new ArrayList<>();

                  for (Entry<Integer, String> indexEntry : indexMap.entrySet()) {
                     int indexx = indexEntry.getKey();

                     while (list.size() < indexx) {
                        list.add("");
                     }

                     list.add(indexEntry.getValue());
                  }

                  data.set(baseKeyx, languagex, list);
               }
            }

            return data;
         }
      }
   }

   private static String[] parseCSVRow(String row) {
      List<String> fields = new ArrayList<>();
      StringBuilder current = new StringBuilder();
      boolean inQuotes = false;
      boolean hadQuotes = false;

      for (int i = 0; i < row.length(); i++) {
         char c = row.charAt(i);
         if (inQuotes) {
            if (c == '"') {
               if (i + 1 < row.length() && row.charAt(i + 1) == '"') {
                  current.append('"');
                  i++;
               } else {
                  inQuotes = false;
               }
            } else {
               current.append(c);
            }
         } else if (c == '"') {
            inQuotes = true;
            hadQuotes = true;
         } else if (c == ',') {
            fields.add(current.toString());
            current = new StringBuilder();
            hadQuotes = false;
         } else {
            current.append(c);
         }
      }

      fields.add(current.toString());
      return fields.toArray(new String[0]);
   }

   public static void write(TranslationCsvParser.TranslationData data, File file) throws IOException {
      write(data, file.toPath());
   }

   public static void write(TranslationCsvParser.TranslationData data, Path path) throws IOException {
      Path destination = path.toAbsolutePath();
      Files.createDirectories(destination.getParent());
      Path staged = Files.createTempFile(destination.getParent(), destination.getFileName().toString(), ".tmp");

      try {
         writeSnapshot(data, staged);
         Files.move(staged, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } finally {
         Files.deleteIfExists(staged);
      }
   }

   private static void writeSnapshot(TranslationCsvParser.TranslationData data, Path path) throws IOException {
      try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
         writer.write(65279);
         List<String> languages = data.getLanguages();
         writer.write(escapeCSV("key"));

         for (String lang : languages) {
            writer.write(",");
            writer.write(escapeCSV(lang));
         }

         writer.newLine();
         List<String> sortedKeys = new ArrayList<>(data.getKeys());
         Collections.sort(sortedKeys);
         long expandedCells = 0L;

         for (String key : sortedKeys) {
            Object firstValue = null;

            for (String lang : languages) {
               firstValue = data.get(key, lang);
               if (firstValue != null) {
                  break;
               }
            }

            for (String langx : languages) {
               Object value = data.get(key, langx);
               if (value != null && value instanceof List != (firstValue instanceof List)) {
                  throw new IOException("Translation '" + key + "' mixes scalar and list values; existing CSV was preserved.");
               }
            }

            if (firstValue instanceof List) {
               int maxSize = 0;

               for (String langxx : languages) {
                  List<String> list = data.getList(key, langxx);
                  if (list != null) {
                     maxSize = Math.max(maxSize, list.size());
                  }
               }

               expandedCells += (long)maxSize * languages.size();
               if (maxSize > 16384 || expandedCells > 1000000L) {
                  throw new IOException("Translation lists exceed the CSV resource limit at " + key);
               }

               for (int i = 0; i < maxSize; i++) {
                  writer.write(escapeCSV(key + "[" + i + "]"));

                  for (String langxxx : languages) {
                     writer.write(",");
                     List<String> list = data.getList(key, langxxx);
                     if (list != null && i < list.size()) {
                        writer.write(escapeCSV(list.get(i)));
                     } else {
                        writer.write("\"\"");
                     }
                  }

                  writer.newLine();
               }
            } else {
               writer.write(escapeCSV(key));

               for (String langxxxx : languages) {
                  writer.write(",");
                  String value = data.getString(key, langxxxx);
                  writer.write(escapeCSV(value != null ? value : ""));
               }

               writer.newLine();
            }
         }
      }
   }

   private static String escapeCSV(String value) {
      if (value == null) {
         return "\"\"";
      } else {
         StringBuilder sb = new StringBuilder();
         sb.append('"');

         for (char c : value.toCharArray()) {
            if (c == '"') {
               sb.append("\"\"");
            } else {
               sb.append(c);
            }
         }

         sb.append('"');
         return sb.toString();
      }
   }

   public static void mergeNewKeys(TranslationCsvParser.TranslationData existing, TranslationCsvParser.TranslationData additions) {
      for (String key : additions.getKeys()) {
         if (!existing.hasKey(key)) {
            for (String lang : additions.getLanguages()) {
               Object value = additions.get(key, lang);
               if (value != null) {
                  existing.addLanguage(lang);
                  existing.set(key, lang, value);
               }
            }
         }
      }
   }

   public static class TranslationData {
      private final List<String> languages;
      private final Map<String, Map<String, Object>> translations;

      public TranslationData(List<String> languages) {
         this.languages = new ArrayList<>(languages);
         this.translations = new LinkedHashMap<>();
      }

      public List<String> getLanguages() {
         return Collections.unmodifiableList(this.languages);
      }

      TranslationCsvParser.TranslationData snapshot() {
         TranslationCsvParser.TranslationData copy = new TranslationCsvParser.TranslationData(this.languages);
         this.translations
            .forEach((key, values) -> values.forEach((language, value) -> copy.set(key, language, value instanceof List<?> list ? new ArrayList(list) : value)));
         return copy;
      }

      public Object get(String key, String language) {
         Map<String, Object> langMap = this.translations.get(key);
         return langMap == null ? null : langMap.get(language);
      }

      public String getString(String key, String language) {
         Object value = this.get(key, language);
         return value instanceof String ? (String)value : null;
      }

      public List<String> getList(String key, String language) {
         Object value = this.get(key, language);
         return value instanceof List ? (List)value : null;
      }

      public void set(String key, String language, Object value) {
         this.translations.computeIfAbsent(key, k -> new LinkedHashMap<>()).put(language, value);
      }

      public boolean hasKey(String key) {
         return this.translations.containsKey(key);
      }

      public Set<String> getKeys() {
         return Collections.unmodifiableSet(this.translations.keySet());
      }

      public void addLanguage(String language) {
         if (!this.languages.contains(language)) {
            this.languages.add(language);
         }
      }
   }
}
