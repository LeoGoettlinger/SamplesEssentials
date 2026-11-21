package github.nighter.smartspawner.language.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class LanguageFiles {
   public static final List<String> SUPPORTED_LANGUAGES = Arrays.asList("en_US", "en_US_DonutSMP", "en_US_DonutSMP_v2", "tr_TR", "vi_VN", "es_ES");
   private final JavaPlugin plugin;
   private final String defaultLocale;
   private final Set<LanguageFileType> activeFileTypes;

   public LanguageFiles(JavaPlugin plugin, String defaultLocale, Set<LanguageFileType> activeFileTypes) {
      this.plugin = plugin;
      this.defaultLocale = defaultLocale;
      this.activeFileTypes = activeFileTypes;
   }

   public void saveDefaultFiles() {
      for (String locale : SUPPORTED_LANGUAGES) {
         for (LanguageFileType fileType : LanguageFileType.values()) {
            this.saveResource(String.format("language/%s/%s", locale, fileType.getFileName()));
         }
      }
   }

   public void loadLocale(String locale, Map<String, LocaleData> localeMap) {
      File localeDir = new File(this.plugin.getDataFolder(), "language/" + locale);
      if (!localeDir.exists() && !localeDir.mkdirs()) {
         this.plugin.getLogger().severe("Failed to create locale directory for " + locale);
      } else {
         YamlConfiguration messages = this.activeFileTypes.contains(LanguageFileType.MESSAGES)
            ? this.loadFile(locale, LanguageFileType.MESSAGES)
            : new YamlConfiguration();
         YamlConfiguration gui = this.activeFileTypes.contains(LanguageFileType.GUI) ? this.loadFile(locale, LanguageFileType.GUI) : new YamlConfiguration();
         YamlConfiguration commandGui = this.activeFileTypes.contains(LanguageFileType.COMMAND_GUI)
            ? this.loadFile(locale, LanguageFileType.COMMAND_GUI)
            : new YamlConfiguration();
         YamlConfiguration formatting = this.activeFileTypes.contains(LanguageFileType.FORMATTING)
            ? this.loadFile(locale, LanguageFileType.FORMATTING)
            : new YamlConfiguration();
         YamlConfiguration items = this.activeFileTypes.contains(LanguageFileType.ITEMS)
            ? this.loadFile(locale, LanguageFileType.ITEMS)
            : new YamlConfiguration();
         YamlConfiguration commandMessages = this.activeFileTypes.contains(LanguageFileType.COMMAND_MESSAGES)
            ? this.loadFile(locale, LanguageFileType.COMMAND_MESSAGES)
            : new YamlConfiguration();
         YamlConfiguration hologram = this.activeFileTypes.contains(LanguageFileType.HOLOGRAM)
            ? this.loadFile(locale, LanguageFileType.HOLOGRAM)
            : new YamlConfiguration();
         localeMap.put(locale, new LocaleData(messages, gui, commandGui, formatting, items, commandMessages, hologram));
      }
   }

   public YamlConfiguration loadOrCreateFile(String locale, LanguageFileType fileType, boolean forceReload) {
      String fileName = fileType.getFileName();
      File file = new File(this.plugin.getDataFolder(), "language/" + locale + "/" + fileName);
      YamlConfiguration defaultConfig = new YamlConfiguration();
      new YamlConfiguration();
      boolean defaultResourceExists = this.plugin.getResource("language/" + this.defaultLocale + "/" + fileName) != null;
      if (defaultResourceExists) {
         try (InputStream inputStream = this.plugin.getResource("language/" + this.defaultLocale + "/" + fileName)) {
            if (inputStream != null) {
               defaultConfig.loadFromString(new String(inputStream.readAllBytes()));
            }
         } catch (Exception var18) {
            this.plugin.getLogger().severe("Failed to load default " + fileName);
         }
      }

      if (!file.exists() && defaultResourceExists) {
         try (InputStream inputStreamx = this.plugin.getResource("language/" + this.defaultLocale + "/" + fileName)) {
            if (inputStreamx != null) {
               file.getParentFile().mkdirs();
               Files.copy(inputStreamx, file.toPath());
            }
         } catch (IOException var16) {
            this.plugin.getLogger().severe("Failed to create " + fileName + " for locale " + locale);
            return new YamlConfiguration();
         }
      }

      if (file.exists()) {
         try {
            YamlConfiguration userConfig = YamlConfiguration.loadConfiguration(file);
            boolean updated = false;

            for (String key : defaultConfig.getKeys(false)) {
               if (!userConfig.contains(key)) {
                  userConfig.set(key, defaultConfig.get(key));
                  updated = true;
               }
            }

            if (updated) {
               userConfig.save(file);
               this.plugin.getLogger().info("Updated " + fileName + " for locale " + locale);
            }

            return userConfig;
         } catch (Exception var13) {
            this.plugin.getLogger().warning("Failed to load " + fileName + " for locale " + locale + ". Using defaults.");
            return defaultConfig;
         }
      } else {
         return new YamlConfiguration();
      }
   }

   public YamlConfiguration loadFile(String locale, LanguageFileType fileType) {
      return this.loadOrCreateFile(locale, fileType, false);
   }

   private void saveResource(String resourcePath) {
      File resourceFile = new File(this.plugin.getDataFolder(), resourcePath);
      if (!resourceFile.exists()) {
         try {
            label53: {
               try (InputStream inputStream = this.plugin.getResource(resourcePath)) {
                  if (inputStream != null) {
                     break label53;
                  }

                  this.plugin.getLogger().warning("Language resource not found in JAR: " + resourcePath);
               }

               return;
            }
         } catch (IOException var8) {
            this.plugin.getLogger().warning("Failed to check language resource: " + resourcePath);
            return;
         }

         resourceFile.getParentFile().mkdirs();
         this.plugin.saveResource(resourcePath, false);
      }
   }
}
