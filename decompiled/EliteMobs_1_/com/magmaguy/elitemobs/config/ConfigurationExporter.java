package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.magmacore.util.FileUtils;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

public class ConfigurationExporter {
   private static final String RSP_RESOURCE_PATH = "em_rsp_defaults";
   private static final String CHECKSUM_FILE = ".rsp_checksum";

   private ConfigurationExporter() {
   }

   public static void initializeConfigs() {
      Path pluginFolder = Paths.get(MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath());
      Path exportsPath = pluginFolder.resolve("resource_pack");
      if (!Files.isDirectory(exportsPath)) {
         try {
            Files.createDirectories(exportsPath);
         } catch (Exception var4) {
            Logger.warn("Failed to create exports directory! Tell the dev!");
            var4.printStackTrace();
            return;
         }
      }

      try {
         exportResourcePack(exportsPath);
      } catch (Exception var3) {
         Logger.warn("Failed to export resource pack! Tell the dev!");
         var3.printStackTrace();
      }
   }

   private static void exportResourcePack(Path exportsPath) throws IOException {
      Path targetPath = exportsPath.resolve("em_rsp_defaults");
      Path checksumFile = exportsPath.resolve(".rsp_checksum");
      String jarChecksum = calculateJarResourceChecksum();
      if (jarChecksum == null) {
         Logger.warn("Could not calculate resource pack checksum from jar!");
      } else {
         boolean needsCopy = true;
         if (Files.exists(checksumFile) && Files.isDirectory(targetPath)) {
            try {
               String existingChecksum = Files.readString(checksumFile).trim();
               if (existingChecksum.equals(jarChecksum)) {
                  needsCopy = false;
               }
            } catch (IOException var11) {
            }
         }

         if (needsCopy) {
            Path stagingPath = exportsPath.resolve("em_rsp_defaults.tmp");
            Path backupPath = exportsPath.resolve("em_rsp_defaults.backup");
            if (!Files.exists(targetPath) && Files.isDirectory(backupPath)) {
               Files.move(backupPath, targetPath);
            } else {
               FileUtils.deleteDirectory(backupPath);
            }

            FileUtils.deleteDirectory(stagingPath);
            Files.createDirectories(stagingPath);
            copyResourceFolder("em_rsp_defaults", stagingPath);
            boolean movedExistingPack = false;
            if (Files.exists(targetPath)) {
               Files.move(targetPath, backupPath);
               movedExistingPack = true;
            }

            try {
               Files.move(stagingPath, targetPath);
            } catch (IOException var12) {
               if (movedExistingPack && !Files.exists(targetPath) && Files.exists(backupPath)) {
                  Files.move(backupPath, targetPath);
               }

               throw var12;
            }

            FileUtils.deleteDirectory(backupPath);
            Path checksumStaging = exportsPath.resolve(".rsp_checksum.tmp");
            Files.writeString(checksumStaging, jarChecksum);

            try {
               Files.move(checksumStaging, checksumFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException var10) {
               Files.move(checksumStaging, checksumFile, StandardCopyOption.REPLACE_EXISTING);
            }

            Logger.info("Resource pack exported to resource_pack folder.");
         }
      }
   }

   private static String calculateJarResourceChecksum() {
      try {
         MessageDigest digest = MessageDigest.getInstance("SHA-1");
         URL resourceUrl = MetadataHandler.PLUGIN.getClass().getClassLoader().getResource("em_rsp_defaults");
         if (resourceUrl == null) {
            return null;
         } else if (resourceUrl.getProtocol().equals("jar")) {
            JarURLConnection jarConnection = (JarURLConnection)resourceUrl.openConnection();

            try (JarFile jarFile = jarConnection.getJarFile()) {
               for (JarEntry entry : Collections.list(jarFile.entries())
                  .stream()
                  .filter(entryx -> entryx.getName().startsWith("em_rsp_defaults") && !entryx.isDirectory())
                  .sorted(Comparator.comparing(ZipEntry::getName))
                  .toList()) {
                  digest.update((entry.getName() + "\u0000").getBytes(StandardCharsets.UTF_8));

                  try (InputStream inputStream = jarFile.getInputStream(entry)) {
                     updateDigest(digest, inputStream);
                  }
               }
            }

            return toHex(digest.digest());
         } else {
            Path resourcePath = Paths.get(resourceUrl.toURI());
            return calculateDirectoryChecksum(resourcePath);
         }
      } catch (Exception var14) {
         Logger.warn("Error calculating jar resource checksum: " + var14.getMessage());
         return null;
      }
   }

   private static void copyResourceFolder(String resourcePath, Path targetPath) throws IOException {
      URL resourceUrl = MetadataHandler.PLUGIN.getClass().getClassLoader().getResource(resourcePath);
      if (resourceUrl == null) {
         throw new IOException("Resource folder not found: " + resourcePath);
      } else {
         if (resourceUrl.getProtocol().equals("jar")) {
            JarURLConnection jarConnection = (JarURLConnection)resourceUrl.openConnection();

            try (JarFile jarFile = jarConnection.getJarFile()) {
               Enumeration<JarEntry> entries = jarFile.entries();

               while (entries.hasMoreElements()) {
                  JarEntry entry = entries.nextElement();
                  String entryName = entry.getName();
                  if (entryName.startsWith(resourcePath + "/")) {
                     String relativePath = entryName.substring(resourcePath.length() + 1);
                     if (!relativePath.isEmpty()) {
                        Path targetFile = targetPath.resolve(relativePath);
                        if (entry.isDirectory()) {
                           Files.createDirectories(targetFile);
                        } else {
                           Files.createDirectories(targetFile.getParent());

                           try (InputStream is = jarFile.getInputStream(entry)) {
                              Files.copy(is, targetFile, StandardCopyOption.REPLACE_EXISTING);
                           }
                        }
                     }
                  }
               }
            }
         } else {
            try {
               Path sourcePath = Paths.get(resourceUrl.toURI());
               copyDirectory(sourcePath, targetPath);
            } catch (Exception var15) {
               throw new IOException("Failed to copy resource folder from IDE", var15);
            }
         }
      }
   }

   private static void copyDirectory(Path source, Path target) throws IOException {
      List<Path> sourcePaths;
      try (Stream<Path> paths = Files.walk(source)) {
         sourcePaths = paths.toList();
      }

      for (Path sourcePath : sourcePaths) {
         Path targetFile = target.resolve(source.relativize(sourcePath));
         if (Files.isDirectory(sourcePath)) {
            Files.createDirectories(targetFile);
         } else {
            Files.createDirectories(targetFile.getParent());
            Files.copy(sourcePath, targetFile, StandardCopyOption.REPLACE_EXISTING);
         }
      }
   }

   public static byte[] sha1CodeByteArray(File file) throws IOException, NoSuchAlgorithmException {
      MessageDigest digest = MessageDigest.getInstance("SHA-1");

      try (
         InputStream fileInputStream = Files.newInputStream(file.toPath());
         DigestInputStream digestInputStream = new DigestInputStream(fileInputStream, digest);
      ) {
         byte[] bytes = new byte[8192];

         while (digestInputStream.read(bytes) != -1) {
         }
      }

      return digest.digest();
   }

   static String calculateDirectoryChecksum(Path resourcePath) throws IOException, NoSuchAlgorithmException {
      MessageDigest digest = MessageDigest.getInstance("SHA-1");

      List<Path> files;
      try (Stream<Path> paths = Files.walk(resourcePath)) {
         files = paths.filter(x$0 -> Files.isRegularFile(x$0)).sorted(Comparator.comparing(pathx -> normalizedRelativePath(resourcePath, pathx))).toList();
      }

      for (Path path : files) {
         digest.update((normalizedRelativePath(resourcePath, path) + "\u0000").getBytes(StandardCharsets.UTF_8));

         try (InputStream inputStream = Files.newInputStream(path)) {
            updateDigest(digest, inputStream);
         }
      }

      return toHex(digest.digest());
   }

   private static String normalizedRelativePath(Path root, Path path) {
      return root.relativize(path).toString().replace(File.separatorChar, '/');
   }

   private static void updateDigest(MessageDigest digest, InputStream inputStream) throws IOException {
      byte[] buffer = new byte[8192];

      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
         digest.update(buffer, 0, bytesRead);
      }
   }

   private static String toHex(byte[] hashBytes) {
      StringBuilder result = new StringBuilder(hashBytes.length * 2);

      for (byte hashByte : hashBytes) {
         result.append(String.format("%02x", hashByte));
      }

      return result.toString();
   }
}
