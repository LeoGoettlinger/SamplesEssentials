package su.nightexpress.nightcore.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class FileUtil {
   public static void copy(@NonNull InputStream inputStream, @NonNull File file) {
      try {
         FileOutputStream outputStream = new FileOutputStream(file);
         byte[] array = new byte[1024];

         int read;
         while ((read = inputStream.read(array)) > 0) {
            outputStream.write(array, 0, read);
         }

         outputStream.close();
         inputStream.close();
      } catch (IOException var5) {
         var5.printStackTrace();
      }
   }

   public static boolean createFileIfNotExists(@NonNull Path path) {
      if (Files.exists(path)) {
         return false;
      } else {
         try {
            Files.createDirectories(path.getParent());
            Files.createFile(path);
            return true;
         } catch (IOException var2) {
            var2.printStackTrace();
            return false;
         }
      }
   }

   public static boolean create(@NonNull File file) {
      if (file.exists()) {
         return false;
      } else {
         File parent = file.getParentFile();
         if (parent == null) {
            return false;
         } else {
            parent.mkdirs();

            try {
               return file.createNewFile();
            } catch (IOException var3) {
               var3.printStackTrace();
               return false;
            }
         }
      }
   }

   @NonNull
   public static String getNameWithoutExtension(@NonNull Path path) {
      String fileName = path.getFileName().toString();
      int index = fileName.lastIndexOf(46);
      return index == -1 ? fileName : fileName.substring(0, index);
   }

   @NonNull
   public static List<File> getConfigFiles(@NonNull String path) {
      return getConfigFiles(path, false);
   }

   @NonNull
   public static List<File> getConfigFiles(@NonNull String path, boolean deep) {
      return getFiles(path, ".yml", deep);
   }

   @NonNull
   public static List<File> getFiles(@NonNull String path) {
      return getFiles(path, false);
   }

   @NonNull
   public static List<File> getFiles(@NonNull String path, boolean deep) {
      return getFiles(path, null, deep);
   }

   @NonNull
   public static List<File> getFiles(@NonNull String path, @Nullable String extension, boolean deep) {
      List<File> files = new ArrayList<>();
      File folder = new File(path);
      File[] listOfFiles = folder.listFiles();
      if (listOfFiles == null) {
         return files;
      } else {
         for (File file : listOfFiles) {
            if (file.isFile()) {
               if (extension == null || file.getName().endsWith(extension)) {
                  files.add(file);
               }
            } else if (file.isDirectory() && deep) {
               files.addAll(getFiles(file.getPath(), true));
            }
         }

         return files;
      }
   }

   @NonNull
   public static List<Path> findYamlFiles(@NonNull String directoryPath) {
      return findYamlFiles(Path.of(directoryPath));
   }

   @NonNull
   public static List<Path> findFiles(@NonNull String directoryPath) {
      return findFiles(Path.of(directoryPath));
   }

   @NonNull
   public static List<Path> findFiles(@NonNull String directoryPath, @NonNull Predicate<Path> predicate) {
      return findFiles(Path.of(directoryPath), predicate);
   }

   @NonNull
   public static List<Path> findYamlFiles(@NonNull Path directoryPath) {
      return findFiles(directoryPath, path -> Files.isRegularFile(path) && path.toString().endsWith(".yml"));
   }

   @NonNull
   public static List<Path> findFiles(@NonNull Path directoryPath) {
      return findFiles(directoryPath, x$0 -> Files.isRegularFile(x$0));
   }

   @NonNull
   public static List<Path> findFiles(@NonNull Path path, @NonNull Predicate<Path> predicate) {
      if (!Files.exists(path)) {
         return Collections.emptyList();
      } else {
         try {
            List var3;
            try (Stream<Path> stream = Files.list(path)) {
               var3 = stream.filter(predicate).toList();
            }

            return var3;
         } catch (IOException var7) {
            var7.printStackTrace();
            return Collections.emptyList();
         }
      }
   }

   @NonNull
   public static List<File> getFolders(@NonNull String path) {
      File folder = new File(path);
      File[] listOfFiles = folder.listFiles();
      return listOfFiles == null ? Collections.emptyList() : Stream.of(listOfFiles).filter(File::isDirectory).toList();
   }

   public static boolean deleteRecursive(@NonNull String path) {
      return deleteRecursive(new File(path));
   }

   public static boolean deleteRecursive(@NonNull File dir) {
      if (!dir.exists()) {
         return false;
      } else {
         File[] inside = dir.listFiles();
         if (inside != null) {
            for (File file : inside) {
               deleteRecursive(file);
            }
         }

         return dir.delete();
      }
   }

   public static void extractResources(@NonNull File pluginFile, @NonNull String fromPath, @NonNull File destination) {
      if (destination.exists() || destination.mkdirs()) {
         try {
            JarFile jar = new JarFile(pluginFile);
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
               JarEntry entry = entries.nextElement();
               String path = entry.getName();
               if (!entry.isDirectory() && path.startsWith(fromPath)) {
                  File file = new File(destination, path.replaceFirst(fromPath, ""));
                  if (!file.exists()) {
                     create(file);
                     InputStream inputStream = jar.getInputStream(entry);
                     FileOutputStream outputStream = new FileOutputStream(file);

                     while (inputStream.available() > 0) {
                        outputStream.write(inputStream.read());
                     }

                     outputStream.close();
                     inputStream.close();
                  }
               }
            }

            jar.close();
         } catch (IOException var10) {
            var10.printStackTrace();
         }
      }
   }
}
