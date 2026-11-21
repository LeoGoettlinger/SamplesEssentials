package com.artillexstudios.axtrade;

import com.artillexstudios.axtrade.commands.CommandManager;
import com.artillexstudios.axtrade.hooks.HookManager;
import com.artillexstudios.axtrade.lang.LanguageManager;
import com.artillexstudios.axtrade.libs.axapi.AxPlugin;
import com.artillexstudios.axtrade.libs.axapi.config.Config;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.dvs.versioning.BasicVersioning;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.settings.dumper.DumperSettings;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.settings.general.GeneralSettings;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.settings.loader.LoaderSettings;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.settings.updater.UpdaterSettings;
import com.artillexstudios.axtrade.libs.axapi.metrics.AxMetrics;
import com.artillexstudios.axtrade.libs.axapi.utils.MessageUtils;
import com.artillexstudios.axtrade.libs.axapi.utils.StringUtils;
import com.artillexstudios.axtrade.libs.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axtrade.libs.axapi.utils.file.FileUtils;
import com.artillexstudios.axtrade.libs.axapi.utils.logging.LoggerNameFormat;
import com.artillexstudios.axtrade.libs.bstats.bukkit.Metrics;
import com.artillexstudios.axtrade.listeners.EntityInteractListener;
import com.artillexstudios.axtrade.listeners.TradeListeners;
import com.artillexstudios.axtrade.safety.SafetyManager;
import com.artillexstudios.axtrade.trade.TradeTicker;
import com.artillexstudios.axtrade.utils.NumberUtils;
import com.artillexstudios.axtrade.utils.UpdateNotifier;
import java.io.File;
import org.bukkit.Bukkit;

public final class AxTrade extends AxPlugin {
   public static Config CONFIG;
   public static Config LANG;
   public static Config GUIS;
   public static Config HOOKS;
   public static Config TOGGLED;
   public static MessageUtils MESSAGEUTILS;
   private static AxPlugin instance;
   private static AxMetrics metrics;

   public static AxPlugin getInstance() {
      return instance;
   }

   @Override
   public void load() {
      File libs = new File(this.getDataFolder(), "libs");
      if (libs.exists()) {
         FileUtils.deleteNested(libs.toPath());
      }

      File lib = new File(this.getDataFolder(), "lib");
      if (lib.exists()) {
         FileUtils.deleteNested(lib.toPath());
      }
   }

   @Override
   public void enable() {
      instance = this;
      new Metrics(this, 21500);
      CONFIG = new Config(
         new File(this.getDataFolder(), "config.yml"),
         this.getResource("config.yml"),
         GeneralSettings.builder().setUseDefaults(false).build(),
         LoaderSettings.builder().setAutoUpdate(true).build(),
         DumperSettings.DEFAULT,
         UpdaterSettings.builder().setKeepAll(true).setVersioning(new BasicVersioning("version")).build()
      );
      GUIS = new Config(
         new File(this.getDataFolder(), "guis.yml"),
         this.getResource("guis.yml"),
         GeneralSettings.builder().setUseDefaults(false).build(),
         LoaderSettings.builder().setAutoUpdate(true).build(),
         DumperSettings.DEFAULT,
         UpdaterSettings.builder().setKeepAll(true).setVersioning(new BasicVersioning("version")).build()
      );
      LANG = new Config(
         new File(this.getDataFolder(), "lang.yml"),
         this.getResource("lang.yml"),
         GeneralSettings.builder().setUseDefaults(false).build(),
         LoaderSettings.builder().setAutoUpdate(true).build(),
         DumperSettings.DEFAULT,
         UpdaterSettings.builder().setKeepAll(true).setVersioning(new BasicVersioning("version")).build()
      );
      HOOKS = new Config(
         new File(this.getDataFolder(), "currencies.yml"),
         this.getResource("currencies.yml"),
         GeneralSettings.builder().setUseDefaults(false).build(),
         LoaderSettings.builder().setAutoUpdate(true).build(),
         DumperSettings.DEFAULT,
         UpdaterSettings.builder().setKeepAll(true).setVersioning(new BasicVersioning("version")).build()
      );
      TOGGLED = new Config(
         new File(this.getDataFolder(), "toggled.yml"),
         this.getResource("toggled.yml"),
         GeneralSettings.builder().setUseDefaults(false).build(),
         LoaderSettings.DEFAULT,
         DumperSettings.DEFAULT,
         UpdaterSettings.DEFAULT
      );
      LanguageManager.reload();
      MESSAGEUTILS = new MessageUtils(LANG.getBackingDocument(), "prefix", CONFIG.getBackingDocument());
      this.getServer().getPluginManager().registerEvents(new EntityInteractListener(), this);
      this.getServer().getPluginManager().registerEvents(new TradeListeners(), this);
      HookManager.setupHooks();
      NumberUtils.reload();
      TradeTicker.start();
      SafetyManager.start();
      CommandManager.load();
      Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00FFDD[AxTrade] Loaded plugin!"));
      metrics = new AxMetrics(this, 8L);
      metrics.start();
      UpdateNotifier.init(CONFIG, LANG);
      if (CONFIG.getBoolean("update-notifier.enabled", true)) {
         new UpdateNotifier();
      }
   }

   @Override
   public void disable() {
      if (metrics != null) {
         metrics.cancel();
      }

      SafetyManager.stop();
   }

   @Override
   public void updateFlags() {
      FeatureFlags.PLACEHOLDER_API_HOOK.set(true);
      FeatureFlags.PLACEHOLDER_API_IDENTIFIER.set("axtrade");
      FeatureFlags.ENABLE_PACKET_LISTENERS.set(true);
      FeatureFlags.LOGGER_NAME_FORMAT.set(LoggerNameFormat.NAMEABLE);
   }
}
