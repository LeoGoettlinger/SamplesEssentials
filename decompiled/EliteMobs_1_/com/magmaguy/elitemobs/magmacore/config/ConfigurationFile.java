package com.magmaguy.elitemobs.magmacore.config;

import java.io.File;
import java.util.Objects;
import lombok.Generated;
import org.bukkit.configuration.file.FileConfiguration;

public abstract class ConfigurationFile {
   protected final File file;
   protected FileConfiguration fileConfiguration;

   protected ConfigurationFile(String filename) {
      this(ConfigurationEngine.fileCreator(filename));
   }

   protected ConfigurationFile(File file) {
      this.file = ConfigurationEngine.fileCreator(Objects.requireNonNull(file, "file"));
      this.fileConfiguration = Objects.requireNonNull(ConfigurationEngine.fileConfigurationCreator(file), "Configuration could not be loaded: " + file);
      this.initializeValues();
      this.saveDefaults();
   }

   public abstract void initializeValues();

   public void saveDefaults() {
      ConfigurationEngine.fileSaverOnlyDefaults(this.fileConfiguration, this.file);
   }

   @Generated
   public FileConfiguration getFileConfiguration() {
      return this.fileConfiguration;
   }
}
