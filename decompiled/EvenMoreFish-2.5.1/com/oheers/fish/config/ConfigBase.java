package com.oheers.fish.config;

import com.oheers.fish.api.plugin.EMFPlugin;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.plugin.Plugin;
import org.evenmorefish.fish.libs.boostedyaml.YamlDocument;
import org.evenmorefish.fish.libs.boostedyaml.dvs.versioning.BasicVersioning;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.exceptions.ScannerException;
import org.evenmorefish.fish.libs.boostedyaml.settings.Settings;
import org.evenmorefish.fish.libs.boostedyaml.settings.dumper.DumperSettings;
import org.evenmorefish.fish.libs.boostedyaml.settings.general.GeneralSettings;
import org.evenmorefish.fish.libs.boostedyaml.settings.loader.LoaderSettings;
import org.evenmorefish.fish.libs.boostedyaml.settings.updater.UpdaterSettings;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ConfigBase {
   private final boolean preventIO;
   private final String fileName;
   private final String resourceName;
   private final Plugin plugin;
   private final boolean configUpdater;
   private YamlDocument config = null;
   private File file = null;

   private static YamlDocument empty() {
      try {
         return YamlDocument.create(InputStream.nullInputStream());
      } catch (IOException var1) {
         throw new RuntimeException(var1);
      }
   }

   public ConfigBase(@NonNull File file, @NonNull Plugin plugin, boolean configUpdater) {
      this.preventIO = false;
      this.fileName = file.getName();
      this.resourceName = null;
      this.plugin = plugin;
      this.configUpdater = configUpdater;
      this.reload(file);
      this.update();
   }

   public ConfigBase(@NonNull String fileName, @NonNull String resourceName, @NonNull Plugin plugin, boolean configUpdater) {
      this.preventIO = false;
      this.fileName = fileName;
      this.resourceName = resourceName;
      this.plugin = plugin;
      this.configUpdater = configUpdater;
      this.reload(new File(this.getPlugin().getDataFolder(), this.getFileName()));
      this.update();
   }

   public ConfigBase() {
      this.preventIO = true;
      this.fileName = null;
      this.resourceName = null;
      this.plugin = EMFPlugin.getInstance();
      this.configUpdater = false;
      this.config = empty();
   }

   public void reload(@NonNull File configFile) {
      if (!this.preventIO) {
         Settings[] settings = this.getSettings();

         try {
            InputStream resource = this.getResourceName() == null ? null : this.getPlugin().getResource(this.getResourceName());
            if (resource == null) {
               this.config = YamlDocument.create(configFile, settings);
            } else {
               this.config = YamlDocument.create(configFile, resource, settings);
            }

            this.file = configFile;
         } catch (IOException var4) {
            this.plugin.getLogger().log(Level.SEVERE, var4.getMessage(), (Throwable)var4);
            this.config = empty();
         } catch (ScannerException var5) {
            this.plugin
               .getLogger()
               .log(Level.WARNING, "Failed to reload " + this.getFileName() + " due to invalid YAML syntax. Please check the file for errors.");
            this.config = empty();
         }
      }
   }

   public void reload() {
      if (!this.preventIO && this.file != null) {
         this.reload(this.file);
      }
   }

   @NonNull
   public final YamlDocument getConfig() {
      if (this.config == null) {
         throw new IllegalStateException("Config " + this.getFileName() + " is not loaded. Please check for startup errors.");
      } else {
         return this.config;
      }
   }

   @Nullable
   public final File getFile() {
      return this.file;
   }

   @NonNull
   public final Plugin getPlugin() {
      return this.plugin;
   }

   @Nullable
   public final String getFileName() {
      return this.fileName;
   }

   @Nullable
   public final String getResourceName() {
      return this.resourceName;
   }

   public Settings[] getSettings() {
      List<Settings> settingsList = new ArrayList<>(Arrays.asList(this.getGeneralSettings(), this.getDumperSettings(), this.getLoaderSettings()));
      if (this.configUpdater) {
         settingsList.add(this.getUpdaterSettings());
      }

      return settingsList.toArray(Settings[]::new);
   }

   public GeneralSettings getGeneralSettings() {
      return GeneralSettings.builder().setUseDefaults(false).build();
   }

   public DumperSettings getDumperSettings() {
      return DumperSettings.DEFAULT;
   }

   public LoaderSettings getLoaderSettings() {
      return LoaderSettings.DEFAULT;
   }

   public UpdaterSettings getUpdaterSettings() {
      return UpdaterSettings.builder().setVersioning(new BasicVersioning("version")).setKeepAll(true).setEnableDowngrading(false).build();
   }

   public void save() {
      if (!this.preventIO) {
         try {
            this.getConfig().save();
         } catch (IOException var2) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to update " + this.getFileName(), (Throwable)var2);
         }
      }
   }

   public void update() {
      if (!this.preventIO && this.configUpdater) {
         try {
            this.getConfig().update();
         } catch (UnsupportedOperationException | IOException var2) {
            this.plugin.getLogger().warning("Failed to update " + this.getFileName());
         }
      }
   }
}
