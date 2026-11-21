package com.artillexstudios.axafkzone;

import com.artillexstudios.axafkzone.commands.CommandManager;
import com.artillexstudios.axafkzone.libs.axapi.AxPlugin;
import com.artillexstudios.axafkzone.libs.axapi.config.Config;
import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.dvs.versioning.BasicVersioning;
import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.settings.dumper.DumperSettings;
import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.settings.general.GeneralSettings;
import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.settings.loader.LoaderSettings;
import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.settings.updater.UpdaterSettings;
import com.artillexstudios.axafkzone.libs.axapi.metrics.AxMetrics;
import com.artillexstudios.axafkzone.libs.axapi.utils.MessageUtils;
import com.artillexstudios.axafkzone.libs.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axafkzone.libs.axapi.utils.file.FileUtils;
import com.artillexstudios.axafkzone.libs.axapi.utils.logging.LoggerNameFormat;
import com.artillexstudios.axafkzone.libs.bstats.bukkit.Metrics;
import com.artillexstudios.axafkzone.listeners.WandListeners;
import com.artillexstudios.axafkzone.listeners.WorldListeners;
import com.artillexstudios.axafkzone.schedulers.TickZones;
import com.artillexstudios.axafkzone.utils.NumberUtils;
import com.artillexstudios.axafkzone.utils.UpdateNotifier;
import com.artillexstudios.axafkzone.zones.Zone;
import com.artillexstudios.axafkzone.zones.Zones;
import java.io.File;

public final class AxAFKZone extends AxPlugin {
   public static Config CONFIG;
   public static Config LANG;
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
      new Metrics(this, 22054);
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
         DumperSettings.DEFAULT,
         UpdaterSettings.builder().setVersioning(new BasicVersioning("version")).build()
      );
      NumberUtils.reload();
      TickZones.start();
      MESSAGEUTILS = new MessageUtils(LANG.getBackingDocument(), "prefix", CONFIG.getBackingDocument());
      CommandManager.load();
      com.artillexstudios.axafkzone.utils.FileUtils.loadAll();
      this.getServer().getPluginManager().registerEvents(new WandListeners(), this);
      this.getServer().getPluginManager().registerEvents(new WorldListeners(), this);
      metrics = new AxMetrics(this, 9L);
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

      TickZones.stop();

      for (Zone zone : Zones.getZones().values()) {
         zone.disable();
      }
   }

   @Override
   public void updateFlags() {
      FeatureFlags.LOGGER_NAME_FORMAT.set(LoggerNameFormat.NAMEABLE);
   }
}
