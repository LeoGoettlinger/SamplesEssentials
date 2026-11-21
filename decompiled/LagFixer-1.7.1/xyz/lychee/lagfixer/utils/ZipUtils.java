package xyz.lychee.lagfixer.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public final class ZipUtils {
   public static boolean zipFiles(Collection<String> srcFiles, String zipFilePath) throws IOException {
      return zipFiles(srcFiles, zipFilePath, null);
   }

   public static boolean zipFiles(Collection<String> srcFilePaths, String zipFilePath, String comment) throws IOException {
      if (srcFilePaths != null && zipFilePath != null) {
         boolean var9;
         try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(Paths.get(zipFilePath)))) {
            for (String srcFile : srcFilePaths) {
               if (!zipFile(new File(srcFile), "", zos, comment)) {
                  return false;
               }
            }

            var9 = true;
         }

         return var9;
      } else {
         return false;
      }
   }

   public static boolean zipFiles(Collection<File> srcFiles, File zipFile) throws IOException {
      return zipFiles(srcFiles, zipFile, null);
   }

   public static boolean zipFiles(Collection<File> srcFiles, File zipFile, String comment) throws IOException {
      if (srcFiles != null && zipFile != null) {
         boolean var9;
         try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile.toPath()))) {
            for (File srcFile : srcFiles) {
               if (!zipFile(srcFile, "", zos, comment)) {
                  return false;
               }
            }

            var9 = true;
         }

         return var9;
      } else {
         return false;
      }
   }

   public static boolean zipFile(String srcFilePath, String zipFilePath) throws IOException {
      return zipFile(new File(srcFilePath), new File(zipFilePath), null);
   }

   public static boolean zipFile(String srcFilePath, String zipFilePath, String comment) throws IOException {
      return zipFile(new File(srcFilePath), new File(zipFilePath), comment);
   }

   public static boolean zipFile(File srcFile, File zipFile) throws IOException {
      return zipFile(srcFile, zipFile, null);
   }

   public static boolean zipFile(File srcFile, File zipFile, String comment) throws IOException {
      if (srcFile != null && zipFile != null) {
         boolean var4;
         try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile.toPath()))) {
            var4 = zipFile(srcFile, "", zos, comment);
         }

         return var4;
      } else {
         return false;
      }
   }

   private static boolean zipFile(File srcFile, String rootPath, ZipOutputStream zos, String comment) throws IOException {
      rootPath = rootPath + (rootPath.contentEquals(" ") ? "" : File.separator) + srcFile.getName();
      if (srcFile.isDirectory()) {
         File[] fileList = srcFile.listFiles();
         if (fileList != null && fileList.length > 0) {
            for (File file : fileList) {
               if (!zipFile(file, rootPath, zos, comment)) {
                  return false;
               }
            }
         } else {
            ZipEntry entry = new ZipEntry(rootPath + "/");
            entry.setComment(comment);
            zos.putNextEntry(entry);
            zos.closeEntry();
         }
      } else {
         try (BufferedInputStream is = new BufferedInputStream(Files.newInputStream(srcFile.toPath()))) {
            ZipEntry entry = new ZipEntry(rootPath);
            entry.setComment(comment);
            zos.putNextEntry(entry);
            byte[] buffer = new byte[8192];

            int len;
            while ((len = is.read(buffer, 0, 8192)) != -1) {
               zos.write(buffer, 0, len);
            }

            zos.closeEntry();
         }
      }

      return true;
   }

   private static boolean unzipChildFile(File destDir, List<File> files, ZipFile zip, ZipEntry entry, String name) throws IOException {
      File file = new File(destDir, name);
      files.add(file);
      if (entry.isDirectory()) {
         return file.mkdirs();
      } else if (!file.createNewFile()) {
         return false;
      } else {
         try (
            BufferedInputStream in = new BufferedInputStream(zip.getInputStream(entry));
            BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(file.toPath()));
         ) {
            byte[] buffer = new byte[8192];

            int len;
            while ((len = in.read(buffer)) != -1) {
               out.write(buffer, 0, len);
            }
         }

         return true;
      }
   }

   public static List<String> getFilesPath(String zipFilePath) throws IOException {
      return getFilesPath(new File(zipFilePath));
   }

   public static List<String> getFilesPath(File zipFile) throws IOException {
      if (zipFile == null) {
         return null;
      } else {
         ArrayList<String> paths = new ArrayList<>();
         ZipFile zip = new ZipFile(zipFile);
         Enumeration<? extends ZipEntry> entries = zip.entries();

         while (entries.hasMoreElements()) {
            String entryName = entries.nextElement().getName().replace("\\", "/");
            if (entryName.contains("../")) {
               paths.add(entryName);
            } else {
               paths.add(entryName);
            }
         }

         zip.close();
         return paths;
      }
   }

   public static List<String> getComments(String zipFilePath) throws IOException {
      return getComments(new File(zipFilePath));
   }

   public static List<String> getComments(File zipFile) throws IOException {
      if (zipFile == null) {
         return null;
      } else {
         ArrayList<String> comments = new ArrayList<>();
         ZipFile zip = new ZipFile(zipFile);
         Enumeration<? extends ZipEntry> entries = zip.entries();

         while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            comments.add(entry.getComment());
         }

         zip.close();
         return comments;
      }
   }
}
