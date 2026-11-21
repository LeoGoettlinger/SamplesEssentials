package dev.demora.demoralegendaryweapons;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class LanguageManager {
   private final DemoraLegendaryWeapons plugin;
   private FileConfiguration languageConfig;
   private String currentLanguage;
   private final Set<UUID> playersNeedingLanguageSelection;

   public LanguageManager(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.playersNeedingLanguageSelection = new HashSet<>();
      this.initializeLanguageSystem();
   }

   private void initializeLanguageSystem() {
      this.createLanguageFolder();
      this.copyLanguageFiles();
      this.currentLanguage = this.plugin.getConfig().getString("language", "en");
      if (this.currentLanguage.isEmpty()) {
         this.currentLanguage = "en";
      }

      this.loadLanguageFile(this.currentLanguage);
   }

   private void createLanguageFolder() {
      File langFolder = new File(this.plugin.getDataFolder(), "lang");
      if (!langFolder.exists()) {
         langFolder.mkdirs();
      }
   }

   private void copyLanguageFiles() {
      String[] languages = new String[]{"ru.yml", "en.yml", "vn.yml", "de.yml"};

      for (String lang : languages) {
         File langFile = new File(this.plugin.getDataFolder(), "lang/" + lang);
         if (!langFile.exists()) {
            try (InputStream inputStream = this.plugin.getResource("lang/" + lang)) {
               if (inputStream != null) {
                  Files.copy(inputStream, langFile.toPath());
               }
            } catch (IOException var12) {
               this.plugin.getLogger().warning("Could not copy language file: " + lang);
            }
         }
      }
   }

   private void loadLanguageFile(String language) {
      File langFile = new File(this.plugin.getDataFolder(), "lang/" + language + ".yml");
      if (langFile.exists()) {
         this.languageConfig = YamlConfiguration.loadConfiguration(langFile);
         this.currentLanguage = language;
      } else {
         this.plugin.getLogger().warning("Language file not found: " + language + ".yml, using default (en)");
         langFile = new File(this.plugin.getDataFolder(), "lang/en.yml");
         if (langFile.exists()) {
            this.languageConfig = YamlConfiguration.loadConfiguration(langFile);
            this.currentLanguage = "en";
         }
      }
   }

   public String getMessage(String path) {
      if (this.languageConfig == null) {
         return "Language not loaded";
      } else {
         String message = this.languageConfig.getString("messages." + path, "");
         return message.replace('&', '§');
      }
   }

   public String getMessageWithPrefix(String path) {
      return "§8[§6DemoraLegendary§8] §r" + this.getMessage(path);
   }

   public void setLanguage(String language) {
      this.loadLanguageFile(language);
      this.plugin.getConfig().set("language", language);
      this.plugin.saveConfig();
   }

   public void showLanguageSelection(Player player) {
      if (player.isOp()) {
         LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
         player.sendMessage("");
         player.sendMessage(serializer.deserialize("§aSelect plugin language:"));
         player.sendMessage("");
         Component russian = ((TextComponent)serializer.deserialize("§a[Русский]")
               .hoverEvent(HoverEvent.showText(serializer.deserialize("§7Click to select Russian language"))))
            .clickEvent(ClickEvent.runCommand("/demorahop lang ru"));
         Component english = ((TextComponent)serializer.deserialize("§b[English]")
               .hoverEvent(HoverEvent.showText(serializer.deserialize("§7Click to select English language"))))
            .clickEvent(ClickEvent.runCommand("/demorahop lang en"));
         Component vietnamese = ((TextComponent)serializer.deserialize("§e[Tiếng Việt]")
               .hoverEvent(HoverEvent.showText(serializer.deserialize("§7Click to select Vietnamese language"))))
            .clickEvent(ClickEvent.runCommand("/demorahop lang vn"));
         Component german = ((TextComponent)serializer.deserialize("§c[Deutsch]")
               .hoverEvent(HoverEvent.showText(serializer.deserialize("§7Click to select German language"))))
            .clickEvent(ClickEvent.runCommand("/demorahop lang de"));
         player.sendMessage(
            russian.append(Component.text("  ")).append(english).append(Component.text("  ")).append(vietnamese).append(Component.text("  ")).append(german)
         );
         player.sendMessage("");
      }
   }

   public void addPlayerNeedingSelection(UUID playerId) {
      this.playersNeedingLanguageSelection.add(playerId);
   }

   public void removePlayerNeedingSelection(UUID playerId) {
      this.playersNeedingLanguageSelection.remove(playerId);
   }

   public boolean needsLanguageSelection(UUID playerId) {
      return this.playersNeedingLanguageSelection.contains(playerId);
   }

   public String getCurrentLanguage() {
      return this.currentLanguage;
   }

   public boolean isLanguageNotSet() {
      return this.plugin.getConfig().getString("language", "").isEmpty();
   }

   public FileConfiguration getLanguageConfig() {
      return this.languageConfig;
   }

   public List<String> getWeaponLore(String weaponId) {
      List<String> result = new ArrayList<>();
      if (this.languageConfig == null) {
         return result;
      } else {
         for (String line : this.languageConfig.getStringList("messages.weapon-lore." + weaponId)) {
            result.add(line.replace('&', '§'));
         }

         return result;
      }
   }

   public String getWeaponName(String weaponId) {
      String path = "weapon-names." + weaponId;
      String translated = this.getMessage(path);
      if (translated != null && !translated.isEmpty() && !translated.equals("Language not loaded")) {
         return translated;
      } else {
         if (this.languageConfig != null) {
            String directPath = "messages.weapon-names." + weaponId;
            String directValue = this.languageConfig.getString(directPath, "");
            if (!directValue.isEmpty()) {
               return directValue.replace('&', '§');
            }
         }

         return "";
      }
   }
}
