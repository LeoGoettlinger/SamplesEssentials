package com.lishid.openinv.util.config;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public record ConfigUpdater(@NotNull Plugin plugin) {
   public void checkForUpdates() {
      int version = this.plugin.getConfig().getInt("config-version", 1);
      ConfigurationSection defaults = this.plugin.getConfig().getDefaults();
      if (defaults != null && version < defaults.getInt("config-version")) {
         this.plugin.getLogger().info("Configuration update found! Performing update...");

         try {
            this.plugin.getConfig().save(new File(this.plugin.getDataFolder(), "config_old.yml"));
            this.plugin.getLogger().info("Backed up config.yml to config_old.yml before updating.");
         } catch (IOException var4) {
            this.plugin.getLogger().warning("Could not back up config.yml before updating!");
         }

         if (version < 2) {
            this.updateConfig1To2();
         }

         if (version < 3) {
            this.updateConfig2To3();
         }

         if (version < 4) {
            this.updateConfig3To4();
         }

         if (version < 5) {
            this.updateConfig4To5();
         }

         if (version < 6) {
            this.updateConfig5To6();
         }

         if (version < 7) {
            this.updateConfig6To7();
         }

         if (version < 8) {
            this.updateConfig7To8();
         }

         this.plugin.saveConfig();
         this.plugin.getLogger().info("Configuration update complete!");
      }
   }

   private void updateConfig7To8() {
      FileConfiguration config = this.plugin.getConfig();
      config.set("settings.equal-access", "view");
      config.set("config-version", 8);
   }

   private void updateConfig6To7() {
      FileConfiguration config = this.plugin.getConfig();
      config.set("toggles", null);
      String consoleLocale = config.getString("settings.locale", "en");
      if (consoleLocale.isBlank() || consoleLocale.equalsIgnoreCase("en_us")) {
         consoleLocale = "en";
      }

      config.set("settings.console-locale", consoleLocale);
      config.set("settings.locale", null);
      config.set("config-version", 7);
   }

   private void updateConfig5To6() {
      FileConfiguration config = this.plugin.getConfig();
      config.set("settings.command.open.no-args-opens-self", false);
      config.set("settings.command.searchcontainer.max-radius", 10);
      config.set("config-version", 6);
   }

   private void updateConfig4To5() {
      FileConfiguration config = this.plugin.getConfig();
      config.set("settings.disable-offline-access", false);
      config.set("config-version", 5);
   }

   private void updateConfig3To4() {
      FileConfiguration config = this.plugin.getConfig();
      config.set("notify", null);
      config.set("config-version", 4);
   }

   private void updateConfig2To3() {
      FileConfiguration config = this.plugin.getConfig();
      config.set("items", null);
      config.set("ItemOpenInv", null);
      config.set("toggles", null);
      config.set("settings.disable-saving", config.getBoolean("DisableSaving", false));
      config.set("DisableSaving", null);
      config.set("config-version", 3);
   }

   private void updateConfig1To2() {
      FileConfiguration config = this.plugin.getConfig();
      config.set("ItemOpenInvItemID", null);
      config.set("NotifySilentChest", null);
      config.set("NotifyAnyChest", null);
      config.set("AnyChest", null);
      config.set("SilentChest", null);
      config.set("config-version", 2);
   }
}
