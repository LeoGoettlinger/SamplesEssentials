package com.sk89q.worldedit.util.translation;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.renderer.TranslatableComponentRenderer;
import com.sk89q.worldedit.util.io.ResourceLoader;
import com.sk89q.worldedit.util.io.file.ArchiveUnpacker;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TranslationManager {
   private static final Gson gson = new GsonBuilder().create();
   private static final Type STRING_MAP_TYPE = (new TypeToken<Map<String, String>>() {}).getType();
   private final Map<Locale, Map<String, String>> translationMap = new ConcurrentHashMap<>();
   private final TranslatableComponentRenderer<Locale> friendlyComponentRenderer = TranslatableComponentRenderer.from((locale, key) -> {
      String translation = this.getTranslationMap(locale).get(key);
      return translation == null ? null : new MessageFormat(translation, locale);
   });
   private Locale defaultLocale = Locale.ENGLISH;
   private final ResourceLoader resourceLoader;
   private final Set<Locale> checkedLocales = new HashSet<>();

   public static String makeTranslationKey(String type, String id) {
      String[] parts = id.split(":", 2);
      return type + "." + parts[0] + "." + parts[1].replace('/', '.');
   }

   public TranslationManager(ResourceLoader resourceLoader) {
      Preconditions.checkNotNull(resourceLoader);
      this.resourceLoader = resourceLoader;
   }

   public TranslationManager(ArchiveUnpacker archiveUnpacker, ResourceLoader resourceLoader) {
      this(resourceLoader);
   }

   public void setDefaultLocale(Locale defaultLocale) {
      this.defaultLocale = defaultLocale;
   }

   private Map<String, String> filterTranslations(Map<String, String> translations) {
      return translations.entrySet()
         .stream()
         .filter(e -> !e.getValue().isEmpty())
         .map(e -> Maps.immutableEntry(e.getKey(), e.getValue().replace("'", "''")))
         .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
   }

   private Map<String, String> parseTranslationFile(InputStream inputStream) throws IOException {
      Map var3;
      try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
         var3 = this.filterTranslations((Map<String, String>)gson.fromJson(reader, STRING_MAP_TYPE));
      }

      return var3;
   }

   private Optional<Map<String, String>> loadTranslationFile(String filename) {
      Map<String, String> baseTranslations = new ConcurrentHashMap<>();

      try {
         URL resource = this.resourceLoader.getRootResource("lang/" + filename);
         if (resource != null) {
            try (InputStream stream = resource.openStream()) {
               baseTranslations = this.parseTranslationFile(stream);
            }
         }
      } catch (IOException var12) {
      }

      Path localFile = this.resourceLoader.getLocalResource("lang/" + filename);
      if (Files.exists(localFile)) {
         try (InputStream stream = Files.newInputStream(localFile)) {
            baseTranslations.putAll(this.parseTranslationFile(stream));
         } catch (IOException var10) {
            var10.printStackTrace();
         }
      }

      return baseTranslations.size() == 0 ? Optional.empty() : Optional.of(baseTranslations);
   }

   private boolean tryLoadTranslations(Locale locale) {
      if (this.checkedLocales.contains(locale)) {
         return false;
      } else {
         this.checkedLocales.add(locale);
         Map<String, String> baseTranslations = new ConcurrentHashMap<>();
         if (!locale.equals(this.defaultLocale)) {
            baseTranslations.putAll(this.getTranslationMap(this.defaultLocale));
         }

         Optional<Map<String, String>> langData = Optional.empty();
         if (!locale.getCountry().isEmpty()) {
            langData = this.loadTranslationFile(locale.getLanguage() + "-" + locale.getCountry() + "/strings.json");
         }

         if (!langData.isPresent()) {
            langData = this.loadTranslationFile(locale.getLanguage() + "/strings.json");
         }

         if (langData.isPresent()) {
            baseTranslations.putAll(langData.get());
            this.translationMap.put(locale, baseTranslations);
            return true;
         } else if (locale.equals(this.defaultLocale)) {
            this.translationMap
               .put(Locale.ENGLISH, this.loadTranslationFile("strings.json").orElseThrow(() -> new RuntimeException("Failed to load WorldEdit strings!")));
            return true;
         } else {
            return false;
         }
      }
   }

   public Map<String, String> getTranslationMap(Locale locale) {
      Map<String, String> translations = this.translationMap.get(locale);
      if (translations == null) {
         if (this.tryLoadTranslations(locale)) {
            return this.getTranslationMap(locale);
         }

         if (!locale.equals(this.defaultLocale)) {
            translations = this.getTranslationMap(this.defaultLocale);
         }
      }

      return translations;
   }

   public Component convertText(Component component, Locale locale) {
      return this.friendlyComponentRenderer.render(component, locale);
   }

   public Locale getDefaultLocale() {
      return this.defaultLocale;
   }
}
