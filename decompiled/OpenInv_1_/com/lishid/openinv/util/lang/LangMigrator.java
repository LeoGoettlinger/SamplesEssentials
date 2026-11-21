package com.lishid.openinv.util.lang;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;

public class LangMigrator {
   private int cacheLayer;
   @NotNull
   private final Path oldFolder;
   @NotNull
   private final Path newFolder;
   @NotNull
   private final Logger logger;

   public LangMigrator(@NotNull Path oldFolder, @NotNull Path newFolder, @NotNull Logger logger) {
      this.oldFolder = oldFolder;
      this.newFolder = newFolder;
      this.logger = logger;
   }

   public void migrate() {
      this.cacheLayer = this.initializeCacheLayer();
      if (Files.exists(this.oldFolder.resolve("en_us.yml"))) {
         this.logger.info(() -> String.format("[LanguageManager] Migrating language files to %s", this.newFolder));
         if (!Files.exists(this.newFolder)) {
            try {
               Files.createDirectories(this.newFolder);
            } catch (IOException var5) {
               this.logger.log(Level.WARNING, "Unable to create language subdirectory!", (Throwable)var5);
            }
         }

         try (DirectoryStream<Path> files = Files.newDirectoryStream(this.oldFolder)) {
            files.forEach(path -> {
               if (path != null) {
                  String fileName = path.getFileName().toString();
                  if (!fileName.startsWith("config") && fileName.endsWith(".yml")) {
                     fileName = switch (fileName) {
                        case "en_us.yml" -> "en.yml";
                        case "de_de.yml" -> "de.yml";
                        case "es_es.yml" -> "es.yml";
                        case "pt_br.yml" -> "pt.yml";
                        default -> fileName;
                     };

                     try {
                        Files.copy(path, this.newFolder.resolve(fileName));
                        Files.delete(path);
                     } catch (FileAlreadyExistsException var6) {
                        try {
                           Files.copy(path, this.newFolder.resolve("old_" + fileName));
                           Files.delete(path);
                        } catch (IOException var5x) {
                           throw new UncheckedIOException(var5x);
                        }
                     } catch (IOException var7x) {
                        throw new UncheckedIOException(var7x);
                     }
                  }
               }
            });
         } catch (UncheckedIOException var7) {
            this.logger.log(Level.WARNING, "Unable to migrate languages to subdirectory!", (Throwable)var7.getCause());
         } catch (IOException var8) {
            this.logger.log(Level.WARNING, "Unable to migrate languages to subdirectory!", (Throwable)var8);
         }
      }
   }
}
