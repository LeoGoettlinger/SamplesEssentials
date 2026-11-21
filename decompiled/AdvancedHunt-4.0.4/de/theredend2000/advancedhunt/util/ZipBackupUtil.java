package de.theredend2000.advancedhunt.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class ZipBackupUtil {
   private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
   private static final long MAX_FILE_SIZE = 104857600L;

   private ZipBackupUtil() {
   }

   public static File createZipBackup(File sourceDir, File backupsDir, String prefix) throws IOException {
      if (sourceDir == null) {
         throw new IllegalArgumentException("sourceDir is null");
      } else if (sourceDir.exists() && sourceDir.isDirectory()) {
         if (backupsDir == null) {
            backupsDir = new File(sourceDir, "backups");
         }

         if (!backupsDir.exists() && !backupsDir.mkdirs()) {
            throw new IOException("Failed to create backups directory: " + backupsDir);
         } else {
            String ts = LocalDateTime.now().format(TS);
            String safePrefix = prefix != null && !prefix.trim().isEmpty() ? prefix.trim() : "backup";
            File outFile = new File(backupsDir, safePrefix + "-" + ts + ".zip");
            Path base = sourceDir.toPath().toAbsolutePath().normalize();
            Path outPath = outFile.toPath().toAbsolutePath().normalize();
            Path backupsPath = backupsDir.toPath().toAbsolutePath().normalize();

            try {
               ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outFile)));

               try {
                  Stream<Path> walk = Files.walk(base);

                  try {
                     walk.filter(p -> !Files.isDirectory(p)).forEach(p -> {
                        Path absPath = p.toAbsolutePath().normalize();
                        if (absPath.startsWith(base)) {
                           if (!absPath.equals(outPath)) {
                              if (!absPath.startsWith(backupsPath)) {
                                 try {
                                    long fileSize = Files.size(absPath);
                                    if (fileSize <= 104857600L) {
                                       String rel = base.relativize(absPath).toString().replace('\\', '/');
                                       zos.putNextEntry(new ZipEntry(rel));
                                       InputStream in = Files.newInputStream(absPath);

                                       try {
                                          byte[] buffer = new byte[8192];

                                          int read;
                                          while ((read = in.read(buffer)) != -1) {
                                             zos.write(buffer, 0, read);
                                          }
                                       } catch (Throwable var13x) {
                                          if (in != null) {
                                             try {
                                                in.close();
                                             } catch (Throwable var12) {
                                                var13x.addSuppressed(var12);
                                             }
                                          }

                                          throw var13x;
                                       }

                                       if (in != null) {
                                          in.close();
                                       }

                                       zos.closeEntry();
                                    }
                                 } catch (IOException var14x) {
                                    throw new UncheckedIOException(var14x);
                                 }
                              }
                           }
                        }
                     });
                  } catch (Throwable var15) {
                     if (walk != null) {
                        try {
                           walk.close();
                        } catch (Throwable var14) {
                           var15.addSuppressed(var14);
                        }
                     }

                     throw var15;
                  }

                  if (walk != null) {
                     walk.close();
                  }
               } catch (Throwable var16) {
                  try {
                     zos.close();
                  } catch (Throwable var13) {
                     var16.addSuppressed(var13);
                  }

                  throw var16;
               }

               zos.close();
               return outFile;
            } catch (UncheckedIOException var17) {
               throw var17.getCause();
            }
         }
      } else {
         throw new IOException("Source directory does not exist or is not a directory: " + sourceDir);
      }
   }
}
