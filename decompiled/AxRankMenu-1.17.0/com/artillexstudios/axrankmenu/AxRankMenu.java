package com.artillexstudios.axrankmenu;

import com.artillexstudios.axrankmenu.commands.Commands;
import com.artillexstudios.axrankmenu.gui.GuiUpdater;
import com.artillexstudios.axrankmenu.hooks.HookManager;
import com.artillexstudios.axrankmenu.libs.axapi.AxPlugin;
import com.artillexstudios.axrankmenu.libs.axapi.config.Config;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.dvs.versioning.BasicVersioning;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.common.ScalarStyle;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.settings.dumper.DumperSettings;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.settings.general.GeneralSettings;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.settings.loader.LoaderSettings;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.settings.updater.UpdaterSettings;
import com.artillexstudios.axrankmenu.libs.axapi.metrics.AxMetrics;
import com.artillexstudios.axrankmenu.libs.axapi.scheduler.Scheduler;
import com.artillexstudios.axrankmenu.libs.axapi.utils.MessageUtils;
import com.artillexstudios.axrankmenu.libs.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axrankmenu.libs.axapi.utils.file.FileUtils;
import com.artillexstudios.axrankmenu.libs.axapi.utils.logging.LoggerNameFormat;
import com.artillexstudios.axrankmenu.libs.bstats.bukkit.Metrics;
import com.artillexstudios.axrankmenu.utils.UpdateNotifier;
import java.io.File;

public final class AxRankMenu extends AxPlugin {
   public static Config CONFIG;
   public static Config LANG;
   public static Config RANKS;
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
      new Metrics(this, 20079);
      CONFIG = new Config(
         new File(this.getDataFolder(), "config.yml"),
         this.getResource("config.yml"),
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
         DumperSettings.builder().setScalarStyle(ScalarStyle.DOUBLE_QUOTED).build(),
         UpdaterSettings.builder().setKeepAll(true).setVersioning(new BasicVersioning("version")).build()
      );
      RANKS = new Config(
         new File(this.getDataFolder(), "ranks.yml"),
         this.getResource("ranks.yml"),
         GeneralSettings.builder().setUseDefaults(false).build(),
         LoaderSettings.builder().setAutoUpdate(true).build(),
         DumperSettings.DEFAULT,
         UpdaterSettings.builder().setKeepAll(true).setVersioning(new BasicVersioning("version")).build()
      );
      if (CONFIG.getSection("menu") != null) {
         new ConfigMigrator();
      }

      MESSAGEUTILS = new MessageUtils(LANG.getBackingDocument(), "prefix", CONFIG.getBackingDocument());
      Commands.registerCommand();
      Scheduler.get().run(task -> new HookManager().updateHooks());
      GuiUpdater.start();
      metrics = new AxMetrics(this, 16L);
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

      GuiUpdater.stop();
   }

   @Override
   public void updateFlags() {
      FeatureFlags.LOGGER_NAME_FORMAT.set(LoggerNameFormat.NAMEABLE);
   }
}
