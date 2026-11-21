package com.magmaguy.elitemobs.magmacore.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFile {
   private static final ZipFile.ExtractionLimits DEFAULT_EXTRACTION_LIMITS = new ZipFile.ExtractionLimits(100000, 2147483648L, 2147483648L, 2147483648L, 1000.0);

   private ZipFile() {
   }

   public static boolean zip(File directory, String targetZipPath) {
      if (!directory.exists()) {
         Logger.warn("Failed to zip directory " + directory.getPath() + " because it does not exist!");
         return false;
      } else {
         try {
            ZipFile.ZipUtility.zip(directory, targetZipPath);
            return true;
         } catch (IOException var3) {
            var3.printStackTrace();
            return false;
         }
      }
   }

   public static File unzip(File zippedFile, File destinationUnzippedFile) throws IOException {
      return unzip(zippedFile, destinationUnzippedFile, DEFAULT_EXTRACTION_LIMITS);
   }

   static File unzip(File zippedFile, File destinationUnzippedFile, ZipFile.ExtractionLimits limits) throws IOException {
      if (zippedFile != null && zippedFile.isFile()) {
         if (zippedFile.length() > limits.maxArchiveBytes()) {
            throw new IOException("ZIP archive exceeds the compressed size limit.");
         } else {
            Path destination = destinationUnzippedFile.toPath().toAbsolutePath().normalize();
            Files.createDirectories(destination);

            try (Stream<Path> existing = Files.list(destination)) {
               if (existing.findAny().isPresent()) {
                  throw new IOException("ZIP destination must be empty: " + destination);
               }
            }

            byte[] buffer = new byte[8192];

            try {
               try (java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(zippedFile)) {
                  validateArchive(zipFile, destinationUnzippedFile, limits);
                  long totalWritten = 0L;
                  Enumeration<? extends ZipEntry> entries = zipFile.entries();
                  String currentEntry = null;

                  try {
                     while (entries.hasMoreElements()) {
                        ZipEntry zipEntry = entries.nextElement();
                        currentEntry = zipEntry.getName();
                        File newFile = newFile(destinationUnzippedFile, zipEntry);
                        String entryName = zipEntry.getName();
                        boolean isDirectory = zipEntry.isDirectory() || entryName.endsWith("\\") || entryName.endsWith("/");
                        if (isDirectory) {
                           if (!newFile.isDirectory() && !newFile.mkdirs()) {
                              throw new IOException("Failed to create directory " + newFile);
                           }
                        } else {
                           File parent = newFile.getParentFile();
                           if (!parent.isDirectory() && !parent.mkdirs()) {
                              throw new IOException("Failed to create directory " + parent);
                           }

                           try (
                              InputStream in = zipFile.getInputStream(zipEntry);
                              FileOutputStream fileOutputStream = new FileOutputStream(newFile);
                           ) {
                              long entryWritten = 0L;

                              int len;
                              while ((len = in.read(buffer)) > 0) {
                                 if (entryWritten + len > limits.maxEntryExpandedBytes()) {
                                    throw new IOException("ZIP entry exceeds expanded size limit: " + zipEntry.getName());
                                 }

                                 if (totalWritten + len > limits.maxTotalExpandedBytes()) {
                                    throw new IOException("ZIP archive exceeds total expanded size limit.");
                                 }

                                 fileOutputStream.write(buffer, 0, len);
                                 entryWritten += len;
                                 totalWritten += len;
                              }
                           }
                        }

                        long entryTime = zipEntry.getTime();
                        if (entryTime >= 0L) {
                           newFile.setLastModified(entryTime);
                        }
                     }
                  } catch (IOException var28) {
                     throw new IOException("Failed to extract ZIP entry '" + currentEntry + "': " + var28.getMessage(), var28);
                  }
               }

               return destinationUnzippedFile;
            } catch (IOException var30) {
               try {
                  deleteExtractedContents(destination);
               } catch (IOException var20) {
                  var30.addSuppressed(var20);
               }

               throw var30;
            }
         }
      } else {
         throw new IOException("ZIP archive does not exist: " + zippedFile);
      }
   }

   private static void validateArchive(java.util.zip.ZipFile zipFile, File destination, ZipFile.ExtractionLimits limits) throws IOException {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      Set<String> names = new HashSet<>();
      long declaredExpandedBytes = 0L;
      int entryCount = 0;

      while (entries.hasMoreElements()) {
         ZipEntry entry = entries.nextElement();
         if (++entryCount > limits.maxEntries()) {
            throw new IOException("ZIP archive exceeds entry-count limit.");
         }

         newFile(destination, entry);
         String normalized = normalizeEntryName(entry.getName());
         if (!normalized.isEmpty()) {
            String key = normalized.toLowerCase(Locale.ROOT);
            if (!names.add(key)) {
               throw new IOException("ZIP archive contains a duplicate path: " + entry.getName());
            }

            long size = entry.getSize();
            if (size >= 0L) {
               if (size > limits.maxEntryExpandedBytes()) {
                  throw new IOException("ZIP entry exceeds expanded size limit: " + entry.getName());
               }

               if (declaredExpandedBytes > limits.maxTotalExpandedBytes() - size) {
                  throw new IOException("ZIP archive exceeds total expanded size limit.");
               }

               declaredExpandedBytes += size;
               long compressed = entry.getCompressedSize();
               if (size > 0L && compressed >= 0L) {
                  double ratio = (double)size / Math.max(1L, compressed);
                  if (ratio > limits.maxCompressionRatio()) {
                     throw new IOException("ZIP entry exceeds compression-ratio limit: " + entry.getName());
                  }
               }
            }
         }
      }
   }

   private static String normalizeEntryName(String entryName) {
      String normalized = entryName.replace('\\', '/');

      while (normalized.endsWith("/")) {
         normalized = normalized.substring(0, normalized.length() - 1);
      }

      return normalized;
   }

   private static void deleteExtractedContents(Path destination) throws IOException {
      if (Files.isDirectory(destination)) {
         try (Stream<Path> children = Files.list(destination)) {
            for (Path child : children.toList()) {
               try (Stream<Path> paths = Files.walk(child)) {
                  for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                     Files.deleteIfExists(path);
                  }
               }
            }
         }
      }
   }

   private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
      String entryName = zipEntry.getName().replace('\\', '/');
      if (entryName.endsWith("/")) {
         entryName = entryName.substring(0, entryName.length() - 1);
      }

      File destFile = new File(destinationDir, entryName);
      String destDirPath = destinationDir.getCanonicalPath();
      String destFilePath = destFile.getCanonicalPath();
      if (destFilePath.equals(destDirPath)) {
         return destFile;
      } else if (!destFilePath.startsWith(destDirPath + File.separatorChar)) {
         throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
      } else {
         return destFile;
      }
   }

   record ExtractionLimits(int maxEntries, long maxArchiveBytes, long maxEntryExpandedBytes, long maxTotalExpandedBytes, double maxCompressionRatio) {
      ExtractionLimits(int maxEntries, long maxArchiveBytes, long maxEntryExpandedBytes, long maxTotalExpandedBytes, double maxCompressionRatio) {
         if (maxEntries > 0 && maxArchiveBytes > 0L && maxEntryExpandedBytes > 0L && maxTotalExpandedBytes > 0L && !(maxCompressionRatio <= 0.0)) {
            this.maxEntries = maxEntries;
            this.maxArchiveBytes = maxArchiveBytes;
            this.maxEntryExpandedBytes = maxEntryExpandedBytes;
            this.maxTotalExpandedBytes = maxTotalExpandedBytes;
            this.maxCompressionRatio = maxCompressionRatio;
         } else {
            throw new IllegalArgumentException("All ZIP extraction limits must be positive.");
         }
      }
   }

   public static class ZipUtility {
      private static final int BUFFER_SIZE = 4096;

      public static void zip(File file, String destZipFile) throws FileNotFoundException, IOException {
         try (
            FileOutputStream fileOutputStream = new FileOutputStream(destZipFile);
            ZipOutputStream zos = new ZipOutputStream(fileOutputStream);
         ) {
            if (file.isDirectory()) {
               File[] children = file.listFiles();
               if (children == null) {
                  throw new IOException("Failed to list directory " + file);
               }

               for (File child : children) {
                  if (child.isDirectory()) {
                     zipDirectory(child, child.getName(), zos);
                  } else {
                     zipFile(child, zos);
                  }
               }
            } else {
               zipFile(file, zos);
            }
         }
      }

      private static void zipDirectory(File folder, String parentFolder, ZipOutputStream zos) throws FileNotFoundException, IOException {
         File[] children = folder.listFiles();
         if (children == null) {
            throw new IOException("Failed to list directory " + folder);
         } else {
            for (File file : children) {
               if (file.isDirectory()) {
                  zipDirectory(file, parentFolder + "/" + file.getName(), zos);
               } else {
                  ZipEntry zipEntry = new ZipEntry(parentFolder + "/" + file.getName());
                  zippedySplit(zos, file, zipEntry);
               }
            }
         }
      }

      private static void zipFile(File file, ZipOutputStream zos) throws FileNotFoundException, IOException {
         if (!file.getName().endsWith(".zip")) {
            ZipEntry zipEntry = new ZipEntry(file.getName());
            zippedySplit(zos, file, zipEntry);
         }
      }

      private static void zippedySplit(ZipOutputStream zos, File file, ZipEntry zipEntry) throws IOException {
         zipEntry.setTime(0L);
         zos.putNextEntry(zipEntry);

         try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            byte[] bytesIn = new byte[4096];

            int read;
            while ((read = bis.read(bytesIn)) != -1) {
               zos.write(bytesIn, 0, read);
            }
         } finally {
            zos.closeEntry();
         }
      }
   }
}
