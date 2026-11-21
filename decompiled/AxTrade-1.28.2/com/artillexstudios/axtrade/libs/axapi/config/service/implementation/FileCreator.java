package com.artillexstudios.axtrade.libs.axapi.config.service.implementation;

import com.artillexstudios.axtrade.libs.axapi.config.service.Writer;
import com.artillexstudios.axtrade.libs.axapi.utils.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileCreator {
   private final Writer writer;

   public FileCreator(Writer writer) {
      this.writer = writer;
   }

   public boolean create(Path path, InputStream defaults) {
      if (Files.exists(path)) {
         if (Files.isDirectory(path)) {
            LogUtils.error("Failed to load file at {}! File is a directory!", path);

            try {
               Files.delete(path);
            } catch (IOException var5) {
               LogUtils.error("An unexpected error occurred while deleting directory which should have been a file!", var5);
               return false;
            }
         }
      } else {
         try {
            if (defaults != null) {
               this.writer.write(path, new String(defaults.readAllBytes(), StandardCharsets.UTF_8));
            } else {
               this.writer.write(path, null);
            }
         } catch (IOException var4) {
            LogUtils.error("Failed to read bytes from defaults stream!");
            return false;
         }
      }

      return true;
   }
}
