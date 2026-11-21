package org.popcraft.chunky.platform;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.popcraft.chunky.ChunkyBukkit;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.Translator;

public class BukkitConfig implements Config {
   private int taskQueue;
   private static final List<String> HEADER = Arrays.asList("Chunky Configuration", "https://github.com/pop4959/Chunky/wiki/Configuration");
   private final ChunkyBukkit plugin;

   public BukkitConfig(ChunkyBukkit plugin) {
      this.plugin = plugin;
      FileConfigurationOptions options = plugin.getConfig().options();
      options.copyDefaults(true);

      try {
         FileConfigurationOptions.class.getMethod("header", String.class).invoke(options, String.join("\n", HEADER));
      } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException var4) {
         options.setHeader(HEADER);
      }

      plugin.saveConfig();
      Translator.setLanguage(this.getLanguage());
      this.taskQueue = this.initializeTaskQueue();
   }

   @Override
   public Path getDirectory() {
      return this.plugin.getDataFolder().toPath();
   }

   @Override
   public int getVersion() {
      return this.plugin.getConfig().getInt("version", 0);
   }

   @Override
   public String getLanguage() {
      return Input.checkLanguage(this.plugin.getConfig().getString("language", "en"));
   }

   @Override
   public boolean getContinueOnRestart() {
      return this.plugin.getConfig().getBoolean("continue-on-restart", false);
   }

   @Override
   public boolean isForceLoadExistingChunks() {
      return this.plugin.getConfig().getBoolean("force-load-existing-chunks", false);
   }

   @Override
   public boolean isSilent() {
      return this.plugin.getConfig().getBoolean("silent", false);
   }

   @Override
   public void setSilent(boolean silent) {
      this.plugin.getConfig().set("silent", silent);
   }

   @Override
   public int getUpdateInterval() {
      return this.plugin.getConfig().getInt("update-interval", 1);
   }

   @Override
   public void setUpdateInterval(int updateInterval) {
      this.plugin.getConfig().set("update-interval", updateInterval);
   }

   @Override
   public void reload() {
      this.plugin.reloadConfig();
   }
}
