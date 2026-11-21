package de.oliver.fancylib.translations;

import de.oliver.fancylib.translations.message.Message;
import de.oliver.fancylib.translations.message.MultiMessage;
import de.oliver.fancylib.translations.message.SimpleMessage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class Translator {
   private final TextConfig textConfig;
   private final List<Language> languages;
   private Language selectedLanguage;
   private Language fallbackLanguage;

   public Translator(TextConfig textConfig) {
      this.textConfig = textConfig;
      this.languages = new ArrayList<>();
   }

   public void loadLanguages(String pluginFolderPath) {
      this.languages.clear();
      this.selectedLanguage = null;
      this.fallbackLanguage = null;
      File langFolder = new File(pluginFolderPath + File.separator + "languages");
      if (!langFolder.exists() && !langFolder.mkdirs()) {
         throw new RuntimeException("Could not create languages folder");
      } else {
         File defaultFile = new File(langFolder, "default.yml");

         try {
            InputStream defaultStream = this.getClass().getResourceAsStream("/languages/default.yml");
            if (defaultStream == null) {
               throw new RuntimeException("Could not find default language file");
            }

            Files.copy(defaultStream, defaultFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
         } catch (IOException var9) {
            throw new RuntimeException("Could not copy default language file");
         }

         File[] langFiles = langFolder.listFiles();
         if (langFiles == null) {
            throw new RuntimeException("Could not list language files");
         } else {
            for (File langFile : langFiles) {
               if (langFile.isFile() && langFile.getName().endsWith(".yml")) {
                  this.languages.add(this.loadLanguageFile(langFile));
               }
            }

            this.fallbackLanguage = this.languages.stream().filter(language -> language.getLanguageCode().equals("default")).findFirst().orElse(null);
            if (this.fallbackLanguage == null) {
               throw new RuntimeException("Could not find fallback language");
            }
         }
      }
   }

   private Language loadLanguageFile(File langFile) {
      String fileName = langFile.getName();
      String languageCode = fileName.substring(0, fileName.lastIndexOf(46));
      YamlConfiguration lang = YamlConfiguration.loadConfiguration(langFile);
      String languageName = lang.getString("language_name", languageCode);
      Language language = new Language(languageCode, languageName);
      ConfigurationSection messages = lang.getConfigurationSection("messages");
      if (messages == null) {
         throw new RuntimeException("Language file " + langFile.getName() + " does not contain a messages section");
      } else {
         for (String key : messages.getKeys(true)) {
            if (messages.isString(key)) {
               SimpleMessage message = new SimpleMessage(this.textConfig, messages.getString(key));
               language.addMessage(key, message);
            } else if (messages.isList(key)) {
               List<String> list = messages.getStringList(key);
               language.addMessage(key, new MultiMessage(this.textConfig, list));
            }
         }

         return language;
      }
   }

   public Message translate(String key) {
      Message message = this.selectedLanguage.getMessage(key);
      if (message == null) {
         message = this.fallbackLanguage.getMessage(key);
      }

      return (Message)(message == null ? new SimpleMessage(this.textConfig, "<red>Missing translation for key <i>" + key) : message.copy());
   }

   public List<Language> getLanguages() {
      return this.languages;
   }

   public Language getSelectedLanguage() {
      return this.selectedLanguage;
   }

   public Translator setSelectedLanguage(Language selectedLanguage) {
      this.selectedLanguage = selectedLanguage;
      return this;
   }

   public Language getFallbackLanguage() {
      return this.fallbackLanguage;
   }
}
