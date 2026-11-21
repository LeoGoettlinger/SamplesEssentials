package com.artillexstudios.axafkzone.libs.axapi.utils.file;

import com.artillexstudios.axafkzone.libs.axapi.AxPlugin;
import java.io.File;

public final class PaperFileUtils implements FileUtils {
   private final AxPlugin plugin;

   public PaperFileUtils(AxPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public File getFolder() {
      return this.plugin.getDataFolder();
   }
}
