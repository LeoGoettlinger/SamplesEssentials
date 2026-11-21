package com.magmaguy.elitemobs.magmacore.nightbreak;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class NightbreakFileUtils {
   private NightbreakFileUtils() {
   }

   public static List<File> collectRootEntries(File rootFolder, String folderName, List<String> filePrefixes) {
      if (!isUsableDirectory(rootFolder)) {
         return List.of();
      } else {
         Set<File> matches = new LinkedHashSet<>();
         if (folderName != null && !folderName.isEmpty()) {
            File packageFolder = new File(rootFolder, folderName);
            if (packageFolder.exists()) {
               matches.add(packageFolder);
            }
         }

         File[] children = rootFolder.listFiles();
         if (children == null) {
            return new ArrayList<>(matches);
         } else {
            for (File child : children) {
               if (matchesPrefix(child, filePrefixes)) {
                  matches.add(child);
               }
            }

            return new ArrayList<>(matches);
         }
      }
   }

   public static List<File> collectRecursiveFiles(File rootFolder, List<String> filePrefixes) {
      if (!isUsableDirectory(rootFolder)) {
         return List.of();
      } else {
         Set<File> matches = new LinkedHashSet<>();
         collectRecursiveFiles(rootFolder, filePrefixes, matches);
         return new ArrayList<>(matches);
      }
   }

   public static void moveEntriesFlat(List<File> sources, File targetRoot) {
      if (sources != null && !sources.isEmpty()) {
         if (!targetRoot.exists()) {
            targetRoot.mkdirs();
         }

         for (File source : sources) {
            File target = new File(targetRoot, source.getName());

            try {
               Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException var6) {
               throw new RuntimeException("Failed to move content from " + source + " to " + target, var6);
            }
         }
      }
   }

   public static void moveEntriesPreservingRelativePaths(List<File> sources, File sourceRoot, File targetRoot) {
      if (sources != null && !sources.isEmpty()) {
         for (File source : sources) {
            Path relativePath = sourceRoot.toPath().relativize(source.toPath());
            File target = targetRoot.toPath().resolve(relativePath).toFile();

            try {
               Files.createDirectories(target.toPath().getParent());
               Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
               pruneEmptyDirectories(source.getParentFile(), sourceRoot);
            } catch (IOException var8) {
               throw new RuntimeException("Failed to move content from " + source + " to " + target, var8);
            }
         }
      }
   }

   private static void collectRecursiveFiles(File currentFolder, List<String> filePrefixes, Set<File> matches) {
      File[] children = currentFolder.listFiles();
      if (children != null) {
         for (File child : children) {
            if (child.isDirectory()) {
               collectRecursiveFiles(child, filePrefixes, matches);
            } else if (matchesPrefix(child, filePrefixes)) {
               matches.add(child);
            }
         }
      }
   }

   private static boolean matchesPrefix(File file, List<String> filePrefixes) {
      if (filePrefixes != null && !filePrefixes.isEmpty()) {
         String name = file.getName().toLowerCase(Locale.ROOT);

         for (String prefix : filePrefixes) {
            if (prefix != null && !prefix.isEmpty() && name.startsWith(prefix.toLowerCase(Locale.ROOT))) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static boolean isUsableDirectory(File folder) {
      return folder != null && folder.exists() && folder.isDirectory();
   }

   private static void pruneEmptyDirectories(File current, File root) {
      while (current != null && !current.equals(root)) {
         File[] children = current.listFiles();
         if (children == null || children.length != 0) {
            return;
         }

         current.delete();
         current = current.getParentFile();
      }
   }
}
