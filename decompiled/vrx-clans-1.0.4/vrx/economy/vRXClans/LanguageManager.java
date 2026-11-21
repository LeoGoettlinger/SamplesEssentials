package vrx.economy.vRXClans;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class LanguageManager {
   private int playerCache;
   private final VRXClans plugin;
   private FileConfiguration langConfig;

   public LanguageManager(VRXClans plugin) {
      this.plugin = plugin;
      this.reload();
   }

   public void reload() {
      this.playerCache = this.initializePlayerCache();
      String lang = this.plugin.getConfig().getString("language", "en_US");
      File langFile = new File(this.plugin.getDataFolder() + "/Languages", lang + ".yml");
      if (!langFile.exists()) {
         this.plugin.getLogger().warning("Language file " + lang + ".yml not found! Falling back to en_US.yml");
         langFile = new File(this.plugin.getDataFolder() + "/Languages", "en_US.yml");
      }

      this.langConfig = YamlConfiguration.loadConfiguration(langFile);
   }

   public String get(String key, String... replacements) {
      String message = this.langConfig.getString(key, "&cMissing lang key: " + key);
      String prefix = this.langConfig.getString("prefix", "&8[&bVRX Clans&8] ");
      message = message.replace("%prefix%", prefix);

      for (int i = 0; i < replacements.length; i += 2) {
         if (i + 1 < replacements.length) {
            message = message.replace(replacements[i], replacements[i + 1]);
         }
      }

      return ChatColor.translateAlternateColorCodes('&', message);
   }

   public List<String> getList(String key, String... replacements) {
      List<String> list = this.langConfig.getStringList(key);
      List<String> colored = new ArrayList<>();

      for (String s : list) {
         for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
               s = s.replace(replacements[i], replacements[i + 1]);
            }
         }

         colored.add(ChatColor.translateAlternateColorCodes('&', s));
      }

      return colored;
   }
}
