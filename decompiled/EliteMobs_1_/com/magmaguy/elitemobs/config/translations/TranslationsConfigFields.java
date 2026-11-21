package com.magmaguy.elitemobs.config.translations;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class TranslationsConfigFields {
   private static final String TRANSLATIONS_DATA_SUFFIX = "_data.csv";
   private static final String ENGLISH_COLUMN = "en";
   private static final int REPORTED_KEY_SAMPLE_SIZE = 5;
   private static final int MAX_FLUSH_DEFERRALS = 20;
   private final String languageName;
   private final String languageCode;
   private final List<String> refreshedSample = new ArrayList<>();
   private final List<String> staleSample = new ArrayList<>();
   private final List<String> handWrittenSample = new ArrayList<>();
   private final List<String> customizedSample = new ArrayList<>();
   private TranslationCsvParser.TranslationData translationData;
   private TranslationCsvParser.TranslationData baselineData;
   private Path translationsPath;
   private Path baselinePath;
   private boolean saving = false;
   private boolean dirty = false;
   private boolean baselineDirty = false;
   private boolean writable = true;
   private int addedKeyCount = 0;
   private int adoptedKeyCount = 0;
   private int refreshedKeyCount = 0;
   private int staleTranslationCount = 0;
   private int handWrittenTranslationCount = 0;
   private int customizedEnglishCount = 0;
   private boolean reported = false;
   private int changeGeneration = 0;
   private final Object writeLock = new Object();
   private volatile boolean stopped;
   private BukkitTask pendingFlush;
   private int writeFailures;
   private int shapeConflictCount;

   public TranslationsConfigFields() {
      String configLang = DefaultConfig.getLanguage();
      this.languageName = configLang.replace(".yml", "").replace(".csv", "");
      this.languageCode = this.getLanguageCode(this.languageName.toLowerCase());
      this.initialize();
   }

   static TranslationsConfigFields.ReconciliationOutcome reconcile(
      TranslationCsvParser.TranslationData live, TranslationCsvParser.TranslationData baseline, String key, Object shippedDefault, String languageCode
   ) {
      Object liveEnglish = live.get(key, "en");

      for (String language : live.getLanguages()) {
         Object translated = live.get(key, language);
         if (!"en".equals(language) && translated != null && translated instanceof List != (shippedDefault instanceof List)) {
            return TranslationsConfigFields.ReconciliationOutcome.SHAPE_CONFLICT;
         }
      }

      if (liveEnglish == null) {
         live.set(key, "en", shippedDefault);
         if (!"en".equals(languageCode) && live.get(key, languageCode) == null) {
            live.set(key, languageCode, shippedDefault);
         }

         baseline.set(key, "en", shippedDefault);
         recordTranslationBaseline(live, baseline, key, languageCode);
         return TranslationsConfigFields.ReconciliationOutcome.ADDED;
      } else {
         Object baselineEnglish = baseline.get(key, "en");
         boolean adopting = baselineEnglish == null;
         if (adopting) {
            baselineEnglish = liveEnglish;
            baseline.set(key, "en", liveEnglish);
            recordTranslationBaseline(live, baseline, key, languageCode);
         }

         int paddingWidth = Math.max(otherLanguageWidth(live, key), otherLanguageWidth(baseline, key));
         if (csvValuesEqual(shippedDefault, baselineEnglish, paddingWidth)) {
            return adopting ? TranslationsConfigFields.ReconciliationOutcome.BASELINE_ADOPTED : TranslationsConfigFields.ReconciliationOutcome.UNCHANGED;
         } else if (!csvValuesEqual(liveEnglish, baselineEnglish, paddingWidth)) {
            return TranslationsConfigFields.ReconciliationOutcome.ENGLISH_CUSTOMIZED;
         } else {
            live.set(key, "en", shippedDefault);
            baseline.set(key, "en", shippedDefault);
            return TranslationsConfigFields.ReconciliationOutcome.ENGLISH_REFRESHED;
         }
      }
   }

   private static int otherLanguageWidth(TranslationCsvParser.TranslationData data, String key) {
      int width = 0;

      for (String language : data.getLanguages()) {
         if (!"en".equals(language)) {
            List<String> values = data.getList(key, language);
            if (values != null) {
               width = Math.max(width, values.size());
            }
         }
      }

      return width;
   }

   private static boolean csvValuesEqual(Object first, Object second, int paddingWidth) {
      if (Objects.equals(first, second)) {
         return true;
      } else if (first instanceof List<?> a && second instanceof List<?> b) {
         int length = Math.max(a.size(), b.size());
         if (length > paddingWidth) {
            return false;
         } else {
            for (int i = 0; i < length; i++) {
               if (!Objects.equals(i < a.size() ? a.get(i) : "", i < b.size() ? b.get(i) : "")) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public static void discardBaseline(Path translationsFolder, String languageName) {
      try {
         Files.deleteIfExists(translationsFolder.resolve(languageName + "_data.csv"));
      } catch (IOException var3) {
         Logger.warn("Could not remove the stale translation baselines for " + languageName + ": " + var3.getMessage());
      }
   }

   private static void recordTranslationBaseline(
      TranslationCsvParser.TranslationData live, TranslationCsvParser.TranslationData baseline, String key, String languageCode
   ) {
      if (!"en".equals(languageCode)) {
         Object translation = live.get(key, languageCode);
         if (translation != null) {
            baseline.set(key, languageCode, translation);
         }
      }
   }

   private String getLanguageCode(String languageName) {
      return switch (languageName) {
         case "english" -> "en";
         case "custom" -> "custom";
         case "french" -> "fr";
         case "german" -> "de";
         case "spanish" -> "es";
         case "italian" -> "it";
         case "brazilianportuguese", "portuguesebrazilian" -> "pt_br";
         case "russian" -> "ru";
         case "chinese", "chinesesimplified" -> "zh_cn";
         case "chinesetraditional" -> "zh_tw";
         case "japanese" -> "ja";
         case "korean" -> "ko";
         case "polish" -> "pl";
         case "dutch" -> "nl";
         case "czech" -> "cs";
         case "hungarian" -> "hu";
         case "romanian" -> "ro";
         case "turkish" -> "tr";
         case "vietnamese" -> "vi";
         case "indonesian" -> "id";
         default -> languageName;
      };
   }

   private void initialize() {
      Path folder = Paths.get(MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath(), "translations");
      String csvFilename = this.languageName + ".csv";
      this.translationsPath = folder.resolve(csvFilename);
      this.baselinePath = folder.resolve(this.languageName + "_data.csv");

      try {
         Files.createDirectories(folder);
      } catch (IOException var4) {
         Logger.warn("Failed to create the translations folder: " + var4.getMessage());
      }

      this.translationData = this.loadTranslations(csvFilename);
      this.translationData.addLanguage("en");
      this.translationData.addLanguage(this.languageCode);
      this.baselineData = this.loadBaseline();
      this.baselineData.addLanguage("en");
      this.baselineData.addLanguage(this.languageCode);
   }

   private TranslationCsvParser.TranslationData loadTranslations(String csvFilename) {
      if (Files.exists(this.translationsPath)) {
         try {
            TranslationCsvParser.TranslationData parsed = TranslationCsvParser.parse(this.translationsPath);
            Logger.info("Loaded translations from " + csvFilename);
            return parsed;
         } catch (IOException var5) {
            this.writable = false;
            Logger.warn(
               "Could not read translations/"
                  + csvFilename
                  + " ("
                  + var5.getMessage()
                  + "). EliteMobs is falling back to English text and will not modify the file. Fix or delete it and run '/em language "
                  + this.languageName
                  + "' to restore translations."
            );
            return new TranslationCsvParser.TranslationData(List.of("en", this.languageCode));
         }
      } else {
         try (InputStream in = MetadataHandler.PLUGIN.getResource("translations/" + csvFilename)) {
            if (in != null) {
               Files.copy(in, this.translationsPath);
               TranslationCsvParser.TranslationData parsed = TranslationCsvParser.parse(this.translationsPath);
               Logger.info("Copied bundled translation: " + csvFilename);
               return parsed;
            }
         } catch (IOException var8) {
            Logger.warn("Failed to install the bundled translation " + csvFilename + ": " + var8.getMessage());
         }

         Logger.info("Created new empty translations for: " + this.languageName);
         return new TranslationCsvParser.TranslationData(List.of("en", this.languageCode));
      }
   }

   private TranslationCsvParser.TranslationData loadBaseline() {
      if (!Files.exists(this.baselinePath)) {
         return new TranslationCsvParser.TranslationData(List.of("en", this.languageCode));
      } else {
         try {
            return TranslationCsvParser.parse(this.baselinePath);
         } catch (IOException var2) {
            Logger.warn(
               "Could not read translations/" + this.languageName + "_data.csv (" + var2.getMessage() + "); rebuilding it from the current translations."
            );
            return new TranslationCsvParser.TranslationData(List.of("en", this.languageCode));
         }
      }
   }

   public synchronized void add(String filename, String key, Object value) {
      if (value != null && !this.stopped) {
         String filteredFilename = filename.replace(".yml", "");
         String realKey = filteredFilename + "." + key;
         if (value instanceof String s) {
            String fixed = this.fixConfigColors(s);
            if (fixed.isEmpty()) {
               return;
            }

            value = fixed;
         } else if (value instanceof List<?> list) {
            List<String> fixedList = this.fixConfigColors((List<String>)list);
            if (fixedList.isEmpty()) {
               return;
            }

            value = fixedList;
         }

         TranslationsConfigFields.ReconciliationOutcome outcome = reconcile(this.translationData, this.baselineData, realKey, value, this.languageCode);
         switch (outcome) {
            case UNCHANGED:
               return;
            case ADDED:
               this.addedKeyCount++;
               this.dirty = true;
               this.baselineDirty = true;
               break;
            case BASELINE_ADOPTED:
               this.adoptedKeyCount++;
               this.baselineDirty = true;
               break;
            case ENGLISH_REFRESHED:
               this.refreshedKeyCount++;
               this.recordSample(this.refreshedSample, realKey);
               this.recordTranslationStaleness(realKey, value);
               this.dirty = true;
               this.baselineDirty = true;
               break;
            case ENGLISH_CUSTOMIZED:
               this.customizedEnglishCount++;
               this.recordSample(this.customizedSample, realKey);
               break;
            case SHAPE_CONFLICT:
               this.shapeConflictCount++;
         }

         this.changeGeneration++;
         this.scheduleSave();
      }
   }

   private void recordTranslationStaleness(String realKey, Object refreshedEnglish) {
      if (!"en".equals(this.languageCode)) {
         Object translated = this.translationData.get(realKey, this.languageCode);
         if (!this.isMissingTranslation(translated)) {
            if (!Objects.equals(translated, refreshedEnglish)) {
               if (Objects.equals(translated, this.baselineData.get(realKey, this.languageCode))) {
                  this.staleTranslationCount++;
                  this.recordSample(this.staleSample, realKey);
               } else {
                  this.handWrittenTranslationCount++;
                  this.recordSample(this.handWrittenSample, realKey);
               }
            }
         }
      }
   }

   private void recordSample(List<String> sample, String realKey) {
      if (sample.size() < 5) {
         sample.add(realKey);
      }
   }

   public synchronized Object get(String filename, String key) {
      String filteredFilename = filename.replace(".yml", "");
      String realKey = filteredFilename + "." + key;
      Object value = null;
      if (!"en".equals(this.languageCode)) {
         value = this.translationData.get(realKey, this.languageCode);
         if (this.isMissingTranslation(value)) {
            value = null;
         }
      }

      if (value == null) {
         value = this.translationData.get(realKey, "en");
      }

      if (value instanceof String s) {
         return ChatColorConverter.convert(s);
      } else {
         return value instanceof List<?> list ? ChatColorConverter.convert(list) : value;
      }
   }

   private boolean isMissingTranslation(Object value) {
      if (value == null) {
         return true;
      } else if (value instanceof String string) {
         return string.isBlank();
      } else {
         return !(value instanceof List<?> list) ? false : list.isEmpty() || list.stream().allMatch(item -> item == null || item.toString().isBlank());
      }
   }

   private void scheduleSave() {
      if (!this.saving && !this.stopped) {
         this.saving = true;
         this.scheduleFlush(0);
      }
   }

   private void scheduleFlush(int attempt) {
      int generation = this.changeGeneration;
      this.pendingFlush = Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> {
         synchronized (this) {
            if (!this.stopped) {
               this.pendingFlush = null;
               if (this.changeGeneration != generation && attempt < 20) {
                  this.scheduleFlush(attempt + 1);
               } else {
                  this.startFlush();
               }
            }
         }
      }, 100L);
   }

   private void startFlush() {
      if (this.writable && (this.dirty || this.baselineDirty)) {
         TranslationsConfigFields.SaveSnapshot snapshot = this.snapshot();
         Bukkit.getScheduler().runTaskAsynchronously(MetadataHandler.PLUGIN, () -> {
            boolean success;
            synchronized (this.writeLock) {
               if (this.stopped) {
                  return;
               }

               success = this.persist(snapshot);
            }

            synchronized (this) {
               if (!this.stopped) {
                  this.acknowledge(snapshot, success);
                  this.saving = false;
                  if ((this.dirty || this.baselineDirty) && (success || ++this.writeFailures < 2)) {
                     this.scheduleSave();
                  }
               }
            }
         });
      } else {
         this.saving = false;
         this.report();
      }
   }

   private TranslationsConfigFields.SaveSnapshot snapshot() {
      return new TranslationsConfigFields.SaveSnapshot(
         this.changeGeneration, this.dirty ? this.translationData.snapshot() : null, this.baselineDirty ? this.baselineData.snapshot() : null
      );
   }

   private boolean persist(TranslationsConfigFields.SaveSnapshot snapshot) {
      try {
         if (snapshot.live != null) {
            TranslationCsvParser.write(snapshot.live, this.translationsPath);
         }

         if (snapshot.baseline != null) {
            TranslationCsvParser.write(snapshot.baseline, this.baselinePath);
         }

         return true;
      } catch (RuntimeException | IOException var3) {
         Logger.warn("Could not save translations/" + this.languageName + ".csv and its baselines; unsaved changes are retained: " + var3.getMessage());
         return false;
      }
   }

   private void acknowledge(TranslationsConfigFields.SaveSnapshot snapshot, boolean success) {
      if (success && snapshot.generation == this.changeGeneration) {
         if (snapshot.live != null) {
            this.dirty = false;
         }

         if (snapshot.baseline != null) {
            this.baselineDirty = false;
         }

         this.writeFailures = 0;
         this.report();
      }
   }

   private void report() {
      if (!this.reported && this.writable) {
         this.reported = true;
         if (this.shapeConflictCount > 0) {
            Logger.warn(
               this.shapeConflictCount
                  + " translation keys in "
                  + this.languageName
                  + ".csv changed between text and lists. Their existing rows were preserved; update their shapes manually."
            );
         }

         if (this.adoptedKeyCount > 0 && this.refreshedKeyCount == 0 && this.customizedEnglishCount == 0 && this.addedKeyCount == 0) {
            Logger.info(
               "Recorded translation baselines for "
                  + this.adoptedKeyCount
                  + " keys in "
                  + this.languageName
                  + ".csv - future corrections to the English defaults will now be applied automatically."
            );
         } else if (this.addedKeyCount != 0 || this.refreshedKeyCount != 0 || this.customizedEnglishCount != 0) {
            Logger.info(
               this.languageName
                  + ".csv: "
                  + this.addedKeyCount
                  + " new keys, "
                  + this.refreshedKeyCount
                  + " English defaults corrected, "
                  + this.customizedEnglishCount
                  + " left alone because the English column was edited by hand"
                  + (this.refreshedSample.isEmpty() ? "" : " (e.g. " + String.join(", ", this.refreshedSample) + ")")
            );
            if (this.staleTranslationCount > 0) {
               Logger.warn(
                  this.staleTranslationCount
                     + " shipped translations in "
                     + this.languageName
                     + ".csv now describe English text that has been corrected"
                     + (this.staleSample.isEmpty() ? "" : " (e.g. " + String.join(", ", this.staleSample) + ")")
                     + ". Nothing was overwritten. To pull the current translation, delete translations/"
                     + this.languageName
                     + ".csv and translations/"
                     + this.languageName
                     + "_data.csv, then run '/em language "
                     + this.languageName
                     + "'."
               );
            }

            if (this.handWrittenTranslationCount > 0) {
               Logger.warn(
                  this.handWrittenTranslationCount
                     + " translations you edited yourself in "
                     + this.languageName
                     + ".csv now describe English text that has been corrected"
                     + (this.handWrittenSample.isEmpty() ? "" : " (e.g. " + String.join(", ", this.handWrittenSample) + ")")
                     + ". They were left exactly as you wrote them and need reviewing by hand - re-downloading the language would discard them."
               );
            }

            if (this.customizedEnglishCount > 0 && !this.customizedSample.isEmpty()) {
               Logger.warn(
                  "Hand-edited English cells in "
                     + this.languageName
                     + ".csv were not refreshed (e.g. "
                     + String.join(", ", this.customizedSample)
                     + "). Clear those cells to let EliteMobs manage them again."
               );
            }
         }
      }
   }

   private String fixConfigColors(String value) {
      return value == null ? null : value.replace("§", "&");
   }

   private List<String> fixConfigColors(List<String> values) {
      return values.stream().map(this::fixConfigColors).toList();
   }

   public String getLanguageName() {
      return this.languageName;
   }

   public String getLanguageCode() {
      return this.languageCode;
   }

   public void shutdown() {
      TranslationsConfigFields.SaveSnapshot snapshot;
      synchronized (this) {
         if (this.stopped) {
            return;
         }

         this.stopped = true;
         if (this.pendingFlush != null) {
            this.pendingFlush.cancel();
         }

         this.pendingFlush = null;
         if (!this.writable) {
            return;
         }

         snapshot = this.snapshot();
      }

      boolean success;
      synchronized (this.writeLock) {
         success = this.persist(snapshot);
      }

      synchronized (this) {
         this.acknowledge(snapshot, success);
      }
   }

   static enum ReconciliationOutcome {
      UNCHANGED,
      ADDED,
      BASELINE_ADOPTED,
      ENGLISH_REFRESHED,
      ENGLISH_CUSTOMIZED,
      SHAPE_CONFLICT;
   }

   private record SaveSnapshot(int generation, TranslationCsvParser.TranslationData live, TranslationCsvParser.TranslationData baseline) {
   }
}
