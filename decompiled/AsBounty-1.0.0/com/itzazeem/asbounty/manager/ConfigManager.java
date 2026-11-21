package com.itzazeem.asbounty.manager;

import com.itzazeem.asbounty.AsBounty;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager {
   private final AsBounty plugin;
   private YamlConfiguration langConfig;
   private YamlConfiguration mainGuiConfig;
   private YamlConfiguration confirmGuiConfig;
   private double minAmount;
   private double maxAmount;
   private boolean killMyselfGetReward;
   private boolean prefixEnabled;
   private String prefix;
   private final Map<String, Object> soundConfig;

   public ConfigManager(AsBounty plugin) {
      this.plugin = plugin;
      this.langConfig = new YamlConfiguration();
      this.mainGuiConfig = new YamlConfiguration();
      this.confirmGuiConfig = new YamlConfiguration();
      this.soundConfig = new HashMap<>();
      this.reload();
   }

   public void reload() {
      this.plugin.reloadConfig();
      this.plugin.getConfig().options().copyDefaults(true);
      this.plugin.saveConfig();

      try {
         File langFile = new File(this.plugin.getDataFolder(), "lang.yml");
         if (langFile.exists()) {
            this.langConfig.load(langFile);
         } else {
            this.plugin.saveResource("lang.yml", false);
            this.langConfig.load(langFile);
         }
      } catch (Exception var4) {
         this.plugin.getLogger().warning("Failed to load lang.yml: " + var4.getMessage());
      }

      try {
         File mainGuiFile = new File(this.plugin.getDataFolder(), "gui/main-gui.yml");
         if (mainGuiFile.exists()) {
            this.mainGuiConfig.load(mainGuiFile);
         } else {
            this.plugin.saveResource("gui/main-gui.yml", false);
            this.mainGuiConfig.load(mainGuiFile);
         }
      } catch (Exception var3) {
         this.plugin.getLogger().warning("Failed to load gui/main-gui.yml: " + var3.getMessage());
      }

      try {
         File confirmGuiFile = new File(this.plugin.getDataFolder(), "gui/confirm-gui.yml");
         if (confirmGuiFile.exists()) {
            this.confirmGuiConfig.load(confirmGuiFile);
         } else {
            this.plugin.saveResource("gui/confirm-gui.yml", false);
            this.confirmGuiConfig.load(confirmGuiFile);
         }
      } catch (Exception var2) {
         this.plugin.getLogger().warning("Failed to load gui/confirm-gui.yml: " + var2.getMessage());
      }

      this.minAmount = this.plugin.getConfig().getDouble("limits.min-amount", 10.0);
      this.maxAmount = this.plugin.getConfig().getDouble("limits.max-amount", 1.0E9);
      this.killMyselfGetReward = this.plugin.getConfig().getBoolean("if-kill-myself-get-reward", false);
      this.soundConfig.clear();
      if (this.plugin.getConfig().contains("sounds")) {
         this.soundConfig.put("bounty-added.enabled", this.plugin.getConfig().getBoolean("sounds.bounty-added.enabled", true));
         this.soundConfig.put("bounty-added.sound", this.plugin.getConfig().getString("sounds.bounty-added.sound", "ENTITY_PLAYER_LEVELUP"));
         this.soundConfig.put("no-perm.enabled", this.plugin.getConfig().getBoolean("sounds.no-perm.enabled", true));
         this.soundConfig.put("no-perm.sound", this.plugin.getConfig().getString("sounds.no-perm.sound", "BLOCK_ANVIL_LAND"));
         this.soundConfig.put("gui-click.enabled", this.plugin.getConfig().getBoolean("sounds.gui-click.enabled", true));
         this.soundConfig.put("gui-click.sound", this.plugin.getConfig().getString("sounds.gui-click.sound", "UI_BUTTON_CLICK"));
      }

      this.prefixEnabled = this.langConfig.getBoolean("prefix-enable", true);
      this.prefix = this.langConfig.getString("prefix", "&#00fc88&lAsBounty &7»");
   }

   public String getMessage(String key) {
      String message = this.langConfig.getString("messages." + key);
      return message != null ? message : "&cMessage not found: " + key;
   }

   public String getActionBar(String key) {
      String message = this.langConfig.getString("action-bars." + key);
      return message != null ? message : "&cActionBar not found: " + key;
   }

   public YamlConfiguration getMainGuiConfig() {
      return this.mainGuiConfig;
   }

   public YamlConfiguration getConfirmGuiConfig() {
      return this.confirmGuiConfig;
   }

   public double getMinAmount() {
      return this.minAmount;
   }

   public double getMaxAmount() {
      return this.maxAmount;
   }

   public boolean isKillMyselfGetReward() {
      return this.killMyselfGetReward;
   }

   public Map<String, Object> getSoundConfig() {
      return this.soundConfig;
   }

   public boolean isPrefixEnabled() {
      return this.prefixEnabled;
   }

   public String getPrefix() {
      return this.prefix != null ? this.prefix : "";
   }
}
