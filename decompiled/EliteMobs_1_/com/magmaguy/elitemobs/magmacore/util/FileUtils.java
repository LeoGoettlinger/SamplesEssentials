package com.magmaguy.elitemobs.magmacore.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FileUtils {
   private FileUtils() {
   }

   public static void deleteDirectory(Path directory) throws IOException {
      if (directory != null && Files.exists(directory)) {
         Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
               Files.delete(file);
               return FileVisitResult.CONTINUE;
            }

            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
               if (exc != null) {
                  throw exc;
               } else {
                  Files.delete(dir);
                  return FileVisitResult.CONTINUE;
               }
            }
         });
      }
   }

   public static void deleteDirectory(File directory) throws IOException {
      if (directory != null) {
         deleteDirectory(directory.toPath());
      }
   }
}
