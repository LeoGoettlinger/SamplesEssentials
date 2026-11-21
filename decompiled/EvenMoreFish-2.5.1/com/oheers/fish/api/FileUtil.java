package com.oheers.fish.api;

import com.oheers.fish.api.plugin.EMFPlugin;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class FileUtil {
   private static final Map<File, URLClassLoader> fileClassLoaders = new HashMap<>();

   private FileUtil() {
      throw new UnsupportedOperationException();
   }

   @NonNull
   public static <T> List<Class<? extends T>> findClasses(@NonNull File file, @NonNull Class<T> clazz) {
      if (!file.exists()) {
         return Collections.emptyList();
      } else {
         List<Class<? extends T>> classes = new ArrayList<>();

         for (String match : matchingNames(file)) {
            try {
               URL jar = file.toURI().toURL();
               URLClassLoader loader = fileClassLoaders.computeIfAbsent(file, f -> new URLClassLoader(new URL[]{jar}, clazz.getClassLoader()));
               Class<? extends T> addonClass = loadClass(loader, match, clazz);
               if (addonClass != null) {
                  classes.add(addonClass);
               }
            } catch (VerifyError var9) {
               EMFPlugin.getInstance().getLogger().severe(() -> String.format("Failed to load addon class %s", file.getName()));
               EMFPlugin.getInstance().getLogger().severe(() -> String.format("Cause: %s %s", var9.getClass().getSimpleName(), var9.getMessage()));
            } catch (ClassNotFoundException | IOException var10) {
               throw new RuntimeException(var10);
            }
         }

         return classes;
      }
   }

   @NonNull
   private static List<String> matchingNames(File file) {
      List<String> matches = new ArrayList<>();

      try {
         URL jar = file.toURI().toURL();

         JarEntry entry;
         try (JarInputStream stream = new JarInputStream(jar.openStream())) {
            while ((entry = stream.getNextJarEntry()) != null) {
               String name = entry.getName();
               if (name.endsWith(".class")) {
                  matches.add(name.substring(0, name.lastIndexOf(46)).replace('/', '.'));
               }
            }
         }

         return matches;
      } catch (Exception var8) {
         return Collections.emptyList();
      }
   }

   @Nullable
   private static <T> Class<? extends T> loadClass(@NonNull URLClassLoader loader, String match, @NonNull Class<T> clazz) throws ClassNotFoundException {
      try {
         Class<?> loaded = loader.loadClass(match);
         if (clazz.isAssignableFrom(loaded)) {
            return loaded.asSubclass(clazz);
         }
      } catch (NoClassDefFoundError var4) {
      } catch (UnsupportedClassVersionError var5) {
         EMFPlugin.getInstance().debug(var5.getMessage());
      }

      return null;
   }

   public static Optional<File> loadFileOrResource(
      @NonNull File directory, @NonNull String fileName, @NonNull String resourceName, @NonNull Plugin plugin, boolean overwrite
   ) {
      Objects.requireNonNull(directory, "directory cannot be null");
      Objects.requireNonNull(fileName, "fileName cannot be null");
      Objects.requireNonNull(resourceName, "resourceName cannot be null");
      Objects.requireNonNull(plugin, "plugin cannot be null");
      if (!directory.exists() && !directory.mkdirs()) {
         plugin.getLogger().severe(() -> "Could not create directory: " + directory);
         return Optional.empty();
      } else {
         File configFile = new File(directory, fileName);
         if (configFile.exists() && !overwrite) {
            return Optional.of(configFile);
         } else {
            try {
               if (!configFile.createNewFile()) {
                  plugin.getLogger().severe(() -> "Could not create file: " + configFile);
                  return Optional.empty();
               }
            } catch (IOException var12) {
               plugin.getLogger().log(Level.SEVERE, "Failed to create file: %s".formatted(configFile.getName()), (Throwable)var12);
               return Optional.empty();
            }

            try {
               Optional var7;
               try (InputStream stream = plugin.getResource(resourceName)) {
                  if (stream == null) {
                     plugin.getLogger().severe(() -> "Resource not found: " + resourceName);
                     return Optional.empty();
                  }

                  Files.copy(stream, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                  var7 = Optional.of(configFile);
               }

               return var7;
            } catch (IOException var11) {
               plugin.getLogger().log(Level.SEVERE, "Failed to copy resource %s to %s".formatted(resourceName, configFile), (Throwable)var11);
               return Optional.empty();
            }
         }
      }
   }

   public static Optional<File> loadFileOrResource(@NonNull File directory, @NonNull String fileName, @NonNull String resourceName, @NonNull Plugin plugin) {
      return loadFileOrResource(directory, fileName, resourceName, plugin, false);
   }

   public static List<File> getFilesInDirectoryWithExtension(
      @NonNull File directory, @Nullable String extension, boolean ignoreUnderscoreFiles, boolean recursive
   ) {
      List<File> finalList = new ArrayList<>();
      if (directory.exists() && directory.isDirectory()) {
         try {
            FilenameFilter filter = extension == null ? null : (dir, name) -> name.endsWith(extension);
            File[] fileArray = directory.listFiles(filter);
            if (fileArray == null) {
               return finalList;
            }

            for (File file : fileArray) {
               if (!ignoreUnderscoreFiles || !file.getName().startsWith("_")) {
                  if (file.isDirectory() && recursive) {
                     finalList.addAll(getFilesInDirectory(file, ignoreUnderscoreFiles, true));
                  } else if (file.isFile()) {
                     finalList.add(file);
                  }
               }
            }
         } catch (SecurityException var11) {
            EMFPlugin.getInstance()
               .getLogger()
               .log(Level.WARNING, "Failed to retrieve files in %s: Access Denied.".formatted(directory.getAbsolutePath()), (Throwable)var11);
         }

         return finalList;
      } else {
         return finalList;
      }
   }

   public static List<File> getFilesInDirectory(@NonNull File directory, boolean ignoreUnderscoreFiles, boolean recursive) {
      return getFilesInDirectoryWithExtension(directory, null, ignoreUnderscoreFiles, recursive);
   }

   public static boolean doesDirectoryContainFile(@NonNull File directory, @NonNull String fileName, boolean recursive) {
      for (File file : getFilesInDirectory(directory, false, recursive)) {
         if (file.getName().equals(fileName)) {
            return true;
         }
      }

      return false;
   }

   public static Set<String> getAddonFilenames(@NonNull Class<?> clazz, String jarPath) throws IOException {
      URL jarLocation = clazz.getProtectionDomain().getCodeSource().getLocation();
      Optional<File> jarFile = fromURL(jarLocation);
      if (jarFile.isEmpty()) {
         EMFPlugin.getInstance().debug("Empty Jar file");
         return Collections.emptySet();
      } else {
         String normalizedJarPath = jarPath.replace('\\', '/').replaceAll("^/+|/+$", "");

         Set var6;
         try (JarFile jar = new JarFile(jarFile.get())) {
            var6 = jar.stream()
               .map(ZipEntry::getName)
               .filter(name -> name.startsWith(normalizedJarPath + "/"))
               .filter(name -> name.endsWith(".addon"))
               .map(name -> name.substring(name.lastIndexOf(47) + 1))
               .peek(result -> EMFPlugin.getInstance().debug("Found matching addon: " + result))
               .collect(Collectors.toSet());
         }

         return var6;
      }
   }

   private static Optional<File> fromURL(@NonNull URL jarLocation) {
      try {
         return Optional.of(new File(jarLocation.toURI()));
      } catch (URISyntaxException var2) {
         EMFPlugin.getInstance().getLogger().log(Level.WARNING, "Failed to convert JAR URL to file path", (Throwable)var2);
         return Optional.empty();
      }
   }

   public static void loadFilesFromJarDirectory(
      @NonNull String jarDirectory, @NonNull File targetDirectory, @NonNull Predicate<String> filter, boolean overwrite
   ) {
      List<String> fileList = getFilesFromJarDirectory(jarDirectory);
      if (fileList.isEmpty()) {
         EMFPlugin.getInstance().getLogger().warning(() -> "No files in jar directory, either this impl is wrong, or the path is.");
      } else {
         fileList.stream().filter(filter).forEach(file -> {
            String fileName = file.substring(file.lastIndexOf(47) + 1);
            loadFileOrResource(targetDirectory, fileName, file, EMFPlugin.getInstance(), overwrite);
         });
      }
   }

   public static List<String> getFilesFromJarDirectory(@NonNull String jarDirectory) {
      try {
         String dirPath = jarDirectory.endsWith("/") ? jarDirectory.substring(0, jarDirectory.length() - 1) : jarDirectory;
         URL dirURL = EMFPlugin.getInstance().getClass().getClassLoader().getResource(dirPath);
         if (dirURL == null) {
            EMFPlugin.getInstance().getLogger().warning(() -> "Directory not found: " + dirPath);
            return Collections.emptyList();
         } else if ("jar".equals(dirURL.getProtocol())) {
            return getFilesFromJar(dirURL, dirPath);
         } else {
            return "file".equals(dirURL.getProtocol()) ? getFilesFromFileSystem(dirURL, dirPath) : Collections.emptyList();
         }
      } catch (Exception var3) {
         EMFPlugin.getInstance().getLogger().warning("Error reading jar directory '" + jarDirectory + "': " + var3.getMessage());
         return Collections.emptyList();
      }
   }

   private static List<String> getFilesFromJar(URL dirURL, String dirPath) throws IOException {
      List<String> files = new ArrayList<>();
      String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!"));

      try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8))) {
         Enumeration<JarEntry> entries = jar.entries();

         while (entries.hasMoreElements()) {
            String entry = entries.nextElement().getName();
            if (entry.startsWith(dirPath) && !entry.equals(dirPath + "/")) {
               files.add(entry);
            }
         }
      }

      return files;
   }

   private static List<String> getFilesFromFileSystem(URL dirURL, String dirPath) {
      File[] files = new File(dirURL.getPath()).listFiles();
      return files != null && files.length != 0 ? Arrays.stream(files).map(File::getName).map(name -> dirPath + "/" + name).toList() : List.of();
   }

   public static void regenExampleFiles(String jarDirectory, File targetDirectory) {
      loadFilesFromJarDirectory(jarDirectory, targetDirectory, file -> file.startsWith("_example") && file.endsWith(".yml"), true);
   }

   public static void loadDefaultFiles(String jarDirectory, File targetDirectory) {
      EMFPlugin.getInstance().getLogger().info(() -> "Loading default %s configs from jar".formatted(jarDirectory));
      loadFilesFromJarDirectory(jarDirectory, targetDirectory, file -> !file.startsWith("_") && file.endsWith(".yml"), false);
   }

   public static void deleteDirectory(@NonNull File directory) {
      if (directory.exists()) {
         try (Stream<Path> paths = Files.walk(directory.toPath())) {
            paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
         } catch (IOException var6) {
            Logging.error("Failed to delete directory " + directory.getAbsolutePath(), var6);
         }
      }
   }
}
