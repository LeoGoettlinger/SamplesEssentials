package com.magmaguy.elitemobs.config.translations;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;

public class TranslationsConfig {
   private static TranslationsConfigFields translationsConfigFields;
   private static final Pattern PLACEHOLDER = Pattern.compile("\\$prefix|\\$[A-Za-z][A-Za-z0-9_]*");
   private static final Set<String> reportedStalePlaceholders = ConcurrentHashMap.newKeySet();

   public TranslationsConfig() {
      if (isEnglish()) {
         translationsConfigFields = null;
         Logger.info("Language set to English - using plugin defaults");
      } else {
         translationsConfigFields = new TranslationsConfigFields();
      }
   }

   public static String add(String filename, String key, String value) {
      if (isEnglish()) {
         return ChatColorConverter.convert(value);
      } else if (translationsConfigFields == null) {
         Logger.warn("TranslationsConfig not initialized, defaulting to English! (String)");
         return ChatColorConverter.convert(value);
      } else {
         translationsConfigFields.add(filename, key, value);
         if (translationsConfigFields.get(filename, key) instanceof String translated) {
            return usesUnknownPlaceholders(filename, key, translated, value) ? ChatColorConverter.convert(value) : translated;
         } else {
            return ChatColorConverter.convert(value);
         }
      }
   }

   private static boolean usesUnknownPlaceholders(String filename, String key, String translated, String english) {
      Set<String> known = placeholders(english);
      Set<String> unknown = placeholders(translated);
      unknown.removeAll(known);
      if (unknown.isEmpty()) {
         return false;
      } else {
         if (reportedStalePlaceholders.add(filename + "." + key) && reportedStalePlaceholders.size() == 1) {
            Logger.info(
               "Translation for "
                  + filename.replace(".yml", "")
                  + "."
                  + key
                  + " uses "
                  + String.join(", ", unknown)
                  + ", which the current English text no longer has, so the English text is shown instead. Other lines from an outdated translation file are handled the same way without further messages."
            );
         }

         return true;
      }
   }

   private static Set<String> placeholders(String text) {
      Set<String> found = new TreeSet<>();
      if (text == null) {
         return found;
      } else {
         Matcher matcher = PLACEHOLDER.matcher(text);

         while (matcher.find()) {
            found.add(matcher.group());
         }

         return found;
      }
   }

   public static List<String> add(String filename, String key, List<String> value) {
      if (isEnglish()) {
         return ChatColorConverter.convert(value);
      } else if (translationsConfigFields == null) {
         Logger.warn("TranslationsConfig not initialized, defaulting to English! (List)");
         return ChatColorConverter.convert(value);
      } else {
         translationsConfigFields.add(filename, key, value);
         if (!(translationsConfigFields.get(filename, key) instanceof List<String> translated)) {
            return ChatColorConverter.convert(value);
         } else {
            return usesUnknownPlaceholders(filename, key, String.join("\n", translated), value == null ? null : String.join("\n", value))
               ? ChatColorConverter.convert(value)
               : translated;
         }
      }
   }

   public static boolean isEnglish() {
      String lang = DefaultConfig.getLanguage();
      if (lang == null) {
         return true;
      } else {
         lang = lang.toLowerCase().replace(".yml", "").replace(".csv", "");
         return lang.equals("english") || lang.equals("en");
      }
   }

   public static void shutdown() {
      if (translationsConfigFields != null) {
         translationsConfigFields.shutdown();
      }
   }

   @Generated
   public static TranslationsConfigFields getTranslationsConfigFields() {
      return translationsConfigFields;
   }
}
