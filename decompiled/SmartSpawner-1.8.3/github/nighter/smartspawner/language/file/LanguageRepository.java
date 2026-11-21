package github.nighter.smartspawner.language.file;

import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class LanguageRepository {
   private final JavaPlugin plugin;
   private final Set<LanguageFileType> activeFileTypes;
   private final Map<String, LocaleData> locales = new HashMap<>();
   private final Set<String> loadedLocales = new HashSet<>();
   private String defaultLocale;
   private LocaleData defaultLocaleData;

   public LanguageRepository(JavaPlugin plugin, LanguageFileType... fileTypes) {
      this.plugin = plugin;
      this.activeFileTypes = fileTypes.length == 0 ? EnumSet.noneOf(LanguageFileType.class) : EnumSet.copyOf(Arrays.asList(fileTypes));
      this.defaultLocale = this.configuredLocale();
   }

   public void initialize() {
      this.loadLanguages();
      this.files().saveDefaultFiles();
      this.cacheDefaultLocale();
   }

   public void loadLanguages(LanguageFileType... ignoredFileTypes) {
      File languageDirectory = new File(this.plugin.getDataFolder(), "language");
      if (!languageDirectory.exists() && !languageDirectory.mkdirs()) {
         this.plugin.getLogger().severe("Failed to create language directory!");
      } else {
         this.locales.remove(this.defaultLocale);
         this.files().loadLocale(this.defaultLocale, this.locales);
         this.loadedLocales.add(this.defaultLocale);
         this.cacheDefaultLocale();
      }
   }

   public void reload() {
      this.defaultLocale = this.configuredLocale();
      this.loadedLocales.add(this.defaultLocale);

      for (String locale : Set.copyOf(this.loadedLocales)) {
         this.locales.remove(locale);
         new LanguageFiles(this.plugin, this.defaultLocale, this.activeFileTypes).loadLocale(locale, this.locales);
      }

      this.cacheDefaultLocale();
      this.plugin.getLogger().info("Successfully reloaded language files for language " + this.defaultLocale);
   }

   public String defaultLocale() {
      return this.defaultLocale;
   }

   public LocaleData current() {
      return this.defaultLocaleData;
   }

   public boolean isActive(LanguageFileType fileType) {
      return this.activeFileTypes.contains(fileType);
   }

   private LanguageFiles files() {
      return new LanguageFiles(this.plugin, this.defaultLocale, this.activeFileTypes);
   }

   private String configuredLocale() {
      return this.plugin.getConfig().getString("language", "en_US");
   }

   private void cacheDefaultLocale() {
      this.defaultLocaleData = this.locales.get(this.defaultLocale);
      if (this.defaultLocaleData == null) {
         this.plugin.getLogger().severe("Failed to cache default locale data for " + this.defaultLocale);
         this.defaultLocaleData = this.emptyLocale();
         this.locales.put(this.defaultLocale, this.defaultLocaleData);
      }
   }

   private LocaleData emptyLocale() {
      return new LocaleData(
         new YamlConfiguration(),
         new YamlConfiguration(),
         new YamlConfiguration(),
         new YamlConfiguration(),
         new YamlConfiguration(),
         new YamlConfiguration(),
         new YamlConfiguration()
      );
   }
}
