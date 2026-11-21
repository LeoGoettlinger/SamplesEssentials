package com.magmaguy.elitemobs.magmacore.dlc;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.nio.file.Path;

final class ConfigurationImportProfiles {
   private ConfigurationImportProfiles() {
   }

   static Path resolve(ConfigurationImporter importer, String folder, ConfigurationImporter.PluginPlatform platform) {
      if (folder == null) {
         Logger.warn("Directory null for zipped file was not recognized! Was the zipped file packaged correctly?");
         return null;
      } else if (ConfigurationImportRegistry.isSkippedFolder(folder)) {
         return null;
      } else {
         Path resolvedPath = ConfigurationImportRegistry.resolve(importer, folder, platform);
         if (resolvedPath != null) {
            return resolvedPath;
         } else if (ConfigurationImportRegistry.hasResolver(folder, platform)) {
            return null;
         } else if ("schematics".equalsIgnoreCase(folder) && platform == ConfigurationImporter.PluginPlatform.ELITEMOBS) {
            Logger.warn(
               "You just tried to import legacy content! Schematic dungeons no longer exist as of EliteMobs 9.0, use BetterStructures shrines instead!"
            );
            return null;
         } else {
            Logger.warn("Directory " + folder + " for zipped file was not recognized! Was the zipped file packaged correctly?");
            return null;
         }
      }
   }
}
