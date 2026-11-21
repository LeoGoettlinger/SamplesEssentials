package github.nighter.smartspawner.updates;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.language.file.LanguageFiles;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Generated;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class LanguageUpdater {
   private final SmartSpawner plugin;
   private final Set<LanguageUpdater.LanguageFileType> activeFileTypes = new HashSet<>();
   private static final Set<String> MESSAGE_COMPONENTS = Set.of("message", "title", "subtitle", "action_bar", "sound", "enabled");

   public LanguageUpdater(SmartSpawner plugin) {
      this(plugin, LanguageUpdater.LanguageFileType.values());
   }

   public LanguageUpdater(SmartSpawner plugin, LanguageUpdater.LanguageFileType... fileTypes) {
      this.plugin = plugin;
      this.activeFileTypes.addAll(Arrays.asList(fileTypes));
      this.checkAndUpdateLanguageFiles();
   }

   public void checkAndUpdateLanguageFiles() {
      for (String language : LanguageFiles.SUPPORTED_LANGUAGES) {
         File langDir = new File(this.plugin.getDataFolder(), "language/" + language);
         langDir.mkdirs();

         for (LanguageUpdater.LanguageFileType type : this.activeFileTypes) {
            File langFile = new File(langDir, type.getFileName());
            String resource = "language/" + language + "/" + type.getFileName();
            List<YamlMigrator.Rename> renames = ConfigMigrations.forLanguageFile(type);
            if (type == LanguageUpdater.LanguageFileType.ITEMS) {
               YamlMigrator.migrate(langFile, this.plugin.getResource(resource), renames, ConfigMigrations.ITEM_DEFAULTS, false, this.plugin.getLogger());
            } else if (type != LanguageUpdater.LanguageFileType.MESSAGES && type != LanguageUpdater.LanguageFileType.COMMAND_MESSAGES) {
               YamlMigrator.migrate(langFile, this.plugin.getResource(resource), renames, this.plugin.getLogger());
            } else {
               YamlMigrator.migrate(
                  langFile,
                  this.plugin.getResource(resource),
                  renames,
                  null,
                  true,
                  YamlMigrator.OwnedSection.restoredWhenAbsent(LanguageUpdater::isMessageEntry),
                  this.plugin.getLogger()
               );
            }
         }
      }
   }

   private static boolean isMessageEntry(YamlConfiguration defaults, String path) {
      ConfigurationSection section = defaults.getConfigurationSection(path);
      if (section == null) {
         return false;
      } else {
         for (String child : section.getKeys(false)) {
            if (MESSAGE_COMPONENTS.contains(child) && !section.isConfigurationSection(child)) {
               return true;
            }
         }

         return false;
      }
   }

   public static enum LanguageFileType {
      MESSAGES("messages.yml"),
      GUI("gui.yml"),
      COMMAND_GUI("command_gui.yml"),
      FORMATTING("formatting.yml"),
      ITEMS("items.yml"),
      COMMAND_MESSAGES("command_messages.yml");

      private final String fileName;

      private LanguageFileType(String fileName) {
         this.fileName = fileName;
      }

      @Generated
      public String getFileName() {
         return this.fileName;
      }
   }
}
