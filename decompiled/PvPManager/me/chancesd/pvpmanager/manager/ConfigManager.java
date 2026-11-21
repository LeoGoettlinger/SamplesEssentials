package me.chancesd.pvpmanager.manager;

import com.google.common.collect.Lists;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.sdutils.config.ConfigUpdater;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import me.chancesd.pvpmanager.sdutils.utils.Utils;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.LogFile;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager {
   private static final String CONFIG_VERSION = "Config Version";
   private static final String CONFIG_NAME = "config.yml";
   private static final String HOOKS_CONFIG_NAME = "plugin-hooks.yml";
   private final PvPManager plugin;
   private final File configFile;
   private final LogFile log;
   private final File pluginHooksFile;
   private final YamlConfiguration pluginHooksConfig;
   private int oldVersion;

   public ConfigManager(PvPManager plugin) {
      this.plugin = plugin;
      this.configFile = new File(plugin.getDataFolder(), "config.yml");
      this.pluginHooksFile = new File(plugin.getDataFolder(), "plugin-hooks.yml");
      this.prepareFile(this.pluginHooksFile, "plugin-hooks.yml");
      this.loadConfig();
      this.log = new LogFile(new File(plugin.getDataFolder(), "combatlogs.log"));
      this.pluginHooksConfig = this.loadYamlConfiguration(this.pluginHooksFile);
   }

   private void loadConfig() {
      this.checkConfigVersion();
      this.initConfig();
   }

   private void checkConfigVersion() {
      if (this.configFile.exists()) {
         this.plugin.reloadConfig();
         Configuration defaults = this.plugin.getConfig().getDefaults();
         this.oldVersion = this.plugin.getConfig().getInt("Config Version", 0);
         int currentVersion = defaults != null ? defaults.getInt("Config Version") : 0;
         if (this.oldVersion == 0) {
            Utils.renameFile(this.configFile, "config_with_errors.yml");
            Log.warning("Due to an error reading the config, it was reset to default settings");
            Log.warning("This was likely caused by a mistake while you changed settings, like an extra space or missing quotes");
            Log.warning("The broken config was renamed to config_with_errors.yml, you can copy your old settings manually if you need them");
            Lang.queueAdminMsg(
               Lang.PREFIX
                  + " §cDue to an error reading the config, it was reset to default settings\n§cThis was likely caused by a mistake while you changed settings, like an extra space or missing quotes"
            );
            Lang.queueAdminMsg(Lang.PREFIX + " §cThe broken config was renamed to config_with_errors.yml, you can copy your old settings manually if needed");
         } else if (this.isMajorVersionUpgrade()) {
            Utils.renameFile(this.configFile, "config_v3_old.yml");
            Log.warning("The config and messages file were restored to default since v4.0 is a major version update");
            Log.warning("You can find your old files in the plugin folder with _v3_old at the end, like config_v3_old.yml");
            Lang.queueAdminMsg(Lang.PREFIX + " §aThe config and messages were upgraded and set to default since §6v4.0 is a major version update");
            Lang.queueAdminMsg("");
            Lang.queueAdminMsg("§aYou can find your old files in the plugin folder with §e_v3_old§a at the end, like §econfig_v3_old.yml");
         } else {
            if (this.oldVersion < currentVersion) {
               try {
                  ConfigUpdater.update(
                     this.plugin,
                     "config.yml",
                     this.configFile,
                     Arrays.asList("Item Cooldowns.Combat", "Item Cooldowns.Global"),
                     Arrays.asList("Config Version", "Metrics", "Update Check.Enabled")
                  );
                  ConfigUpdater.update(this.plugin, "plugin-hooks.yml", this.pluginHooksFile, Lists.newArrayList(), Lists.newArrayList());
                  Log.infoColor("§aConfig file updated from version §c" + this.oldVersion + " §ato version §c" + currentVersion);
                  Log.warning("Checking the config file and adjusting the new settings is highly recommended");
                  Lang.queueAdminMsg(Lang.PREFIX + " §aConfiguration updated from version §c" + this.oldVersion + " §ato §c" + currentVersion);
                  Lang.queueAdminMsg(Lang.PREFIX + " §aChecking the config file and adjusting the new settings is highly recommended");
               } catch (IOException var4) {
                  Log.severe("Error reading the config file!", var4);
                  Utils.renameFile(this.configFile, "config_with_errors.yml");
               }
            }
         }
      }
   }

   private void initConfig() {
      if (!this.configFile.exists()) {
         this.prepareFile(this.configFile, "config.yml");
         Log.infoColor(ChatColor.DARK_GREEN + "New config file created successfully!");
      }

      YamlConfiguration yamlConfig = this.loadYamlConfiguration(this.configFile);
      Configuration defaults = this.plugin.getConfig().getDefaults();
      if (yamlConfig != null && defaults != null) {
         yamlConfig.setDefaults(defaults);
      }

      Conf.initialize(this.plugin, yamlConfig);
   }

   private YamlConfiguration loadYamlConfiguration(File file) {
      try {
         YamlConfiguration var3;
         try (InputStreamReader inputStream = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            var3 = YamlConfiguration.loadConfiguration(inputStream);
         }

         return var3;
      } catch (Exception var7) {
         Log.severe("Error reading the config, did you type something wrong?", var7);
         return null;
      }
   }

   private void prepareFile(File file, String resource) {
      try (InputStream stream = this.plugin.getResource(resource)) {
         file.getParentFile().mkdirs();
         if (file.createNewFile() && resource != null && !resource.isEmpty()) {
            this.copyResource(stream, file);
         }
      } catch (IOException var8) {
         Log.severe("Error creating config file", var8);
      }
   }

   private void copyResource(InputStream resource, File file1) {
      try (OutputStream out = new FileOutputStream(file1)) {
         byte[] buf = new byte[1024];

         int lenght;
         while ((lenght = resource.read(buf)) > 0) {
            out.write(buf, 0, lenght);
         }

         resource.close();
      } catch (Exception var8) {
         Log.severe("Error copying config file", var8);
      }
   }

   public boolean isMajorVersionUpgrade() {
      return this.oldVersion != 0 && this.oldVersion < 200;
   }

   public int getOldVersion() {
      return this.oldVersion;
   }

   public YamlConfiguration getHooksConfig() {
      return this.pluginHooksConfig;
   }

   public LogFile getLog() {
      return this.log;
   }
}
