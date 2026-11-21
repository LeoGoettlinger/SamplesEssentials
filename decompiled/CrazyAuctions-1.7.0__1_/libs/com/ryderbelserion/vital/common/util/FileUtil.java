package libs.com.ryderbelserion.vital.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import libs.com.ryderbelserion.vital.common.VitalAPI;
import libs.com.ryderbelserion.vital.common.api.Provider;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FileUtil {
   @NotNull
   private static final VitalAPI api = Provider.getApi();
   @NotNull
   private static final ComponentLogger logger = api.getComponentLogger();
   @NotNull
   private static final File dataFolder = api.getDirectory();

   private FileUtil() {
      throw new AssertionError();
   }

   public static void download(@NotNull String link, @NotNull File directory) {
      CompletableFuture.runAsync(() -> {
         URL url = null;

         try {
            url = URI.create(link).toURL();
         } catch (MalformedURLException var10) {
            var10.printStackTrace();
         }

         if (url != null) {
            try (
               ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
               FileOutputStream outputStream = new FileOutputStream(directory);
            ) {
               outputStream.getChannel().transferFrom(readableByteChannel, 0L, Long.MAX_VALUE);
            } catch (IOException var12) {
               var12.printStackTrace();
            }
         }
      });
   }

   public static void write(@NotNull File input, @NotNull String format) {
      try (
         FileWriter writer = new FileWriter(input, true);
         BufferedWriter bufferedWriter = new BufferedWriter(writer);
      ) {
         bufferedWriter.write(format);
         bufferedWriter.newLine();
         writer.flush();
      } catch (Exception var10) {
         logger.warn("Failed to write to: {}", input.getName(), var10);
      }
   }

   public static void zip(@NotNull File input, boolean purge) {
      zip(List.of(input), null, "", purge);
   }

   public static void zip(@NotNull File input, @NotNull String extension, boolean purge) {
      List<File> files = getFileObjects(dataFolder, input.getName(), extension);
      if (!files.isEmpty()) {
         boolean hasNonEmptyFile = false;

         for (File zip : files) {
            if (zip.exists() && zip.length() > 0L) {
               hasNonEmptyFile = true;
               break;
            }
         }

         if (!hasNonEmptyFile) {
            if (api.isVerbose()) {
               logger.warn("All log files are empty. No zip file will be created.");
            }
         } else {
            int count = getFiles(input, ".gz", true).size();
            zip(files, input, "-" + ++count, purge);
         }
      }
   }

   public static void zip(@NotNull List<File> files, @Nullable File directory, String extra, boolean purge) {
      if (!files.isEmpty()) {
         StringBuilder builder = new StringBuilder();
         builder.append(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
         if (!builder.isEmpty()) {
            builder.append(extra);
         }

         builder.append(".gz");
         if (directory != null) {
            directory.mkdirs();
         }

         File zipFile = new File(directory == null ? dataFolder : directory, builder.toString());

         try (
            FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
            ZipOutputStream zipOut = new ZipOutputStream(fileOutputStream);
         ) {
            for (File file : files) {
               if (file.length() > 0L) {
                  FileInputStream fileInputStream = new FileInputStream(file);

                  try {
                     ZipEntry zipEntry = new ZipEntry(file.getName());
                     zipOut.putNextEntry(zipEntry);
                     byte[] bytes = new byte[1024];

                     int length;
                     while ((length = fileInputStream.read(bytes)) >= 0) {
                        zipOut.write(bytes, 0, length);
                     }
                  } catch (Throwable var17) {
                     try {
                        fileInputStream.close();
                     } catch (Throwable var16) {
                        var17.addSuppressed(var16);
                     }

                     throw var17;
                  }

                  fileInputStream.close();
                  if (purge) {
                     file.delete();
                  }
               } else if (api.isVerbose()) {
                  logger.warn("The file named {}'s size is 0, We are not adding to zip.", file.getName());
               }
            }
         } catch (IOException var20) {
            var20.printStackTrace();
         }
      }
   }

   public static void extract(@NotNull String input, boolean overwrite) {
      api.saveResource(input, overwrite);
   }

   public static void visit(@NotNull Consumer<Path> consumer, @NotNull String input) throws IOException {
      URL resource = FileUtil.class.getClassLoader().getResource("config.yml");
      if (resource == null) {
         throw new IllegalStateException("We are lacking awareness of the files in src/main/resources/" + input);
      } else {
         URI path = URI.create(resource.toString().split("!")[0] + "!/");

         try (FileSystem fileSystem = FileSystems.newFileSystem(path, Map.of("create", "true"))) {
            Path toVisit = fileSystem.getPath(input);
            if (Files.exists(toVisit)) {
               consumer.accept(toVisit);
            }
         }
      }
   }

   public static void extract(@NotNull String input) {
      extract(input, input, false);
   }

   public static void extract(@NotNull String input, @NotNull String output, boolean replaceExisting) {
      try {
         visit(path -> {
            Path directory = api.getDirectory().toPath().resolve(output);

            try {
               if (replaceExisting) {
                  directory.toFile().delete();
               }

               if (!Files.exists(directory)) {
                  directory.toFile().mkdirs();

                  try (Stream<Path> files = Files.walk(path)) {
                     files.filter(x$0 -> Files.isRegularFile(x$0)).forEach(file -> {
                        try {
                           Path langFile = directory.resolve(file.getFileName().toString());
                           if (!Files.exists(langFile)) {
                              try (InputStream stream = Files.newInputStream(file)) {
                                 Files.copy(stream, langFile);
                              }
                           }
                        } catch (IOException var8x) {
                           var8x.printStackTrace();
                        }
                     });
                  }
               }
            } catch (IOException var9) {
               var9.printStackTrace();
            }
         }, input);
      } catch (IOException var4) {
         var4.printStackTrace();
      }
   }

   public static void extracts(@Nullable Class<?> object, @NotNull String input, @Nullable Path output, boolean replaceExisting) {
      if (object != null && output != null && !input.isEmpty()) {
         try (JarFile jarFile = new JarFile(Path.of(object.getProtectionDomain().getCodeSource().getLocation().toURI()).toFile())) {
            String path = input.substring(1);
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
               JarEntry entry = entries.nextElement();
               String name = entry.getName();
               if (name.startsWith(path)) {
                  Path file = output.resolve(name.substring(path.length()));
                  boolean exists = Files.exists(file);
                  if (replaceExisting || !exists) {
                     if (!entry.isDirectory()) {
                        try (
                           InputStream inputStream = new BufferedInputStream(jarFile.getInputStream(entry));
                           OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file.toFile()));
                        ) {
                           byte[] buffer = new byte[4096];

                           int readCount;
                           while ((readCount = inputStream.read(buffer)) > 0) {
                              outputStream.write(buffer, 0, readCount);
                           }

                           outputStream.flush();
                        } catch (IOException var21) {
                           var21.printStackTrace();
                        }
                     } else if (!exists) {
                        try {
                           Files.createDirectories(file);
                        } catch (IOException var18) {
                           var18.printStackTrace();
                        }
                     }
                  }
               }
            }
         } catch (IOException var23) {
            var23.printStackTrace();
         } catch (URISyntaxException var24) {
            throw new RuntimeException(var24);
         }
      }
   }

   public static List<String> getFiles(@NotNull File directory, @NotNull String extension, boolean keepExtension) {
      List<String> files = new ArrayList<>();
      String[] list = directory.list();
      if (list == null) {
         return files;
      } else {
         File[] array = directory.listFiles();
         if (array == null) {
            return files;
         } else {
            for (File file : array) {
               if (file.isDirectory()) {
                  String[] folder = file.list();
                  if (folder != null) {
                     for (String name : folder) {
                        if (name.endsWith(extension)) {
                           files.add(keepExtension ? name : name.replaceAll(extension, ""));
                        }
                     }
                  }
               } else {
                  String namex = file.getName();
                  if (namex.endsWith(extension)) {
                     files.add(keepExtension ? namex : namex.replaceAll(extension, ""));
                  }
               }
            }

            return Collections.unmodifiableList(files);
         }
      }
   }

   public static List<String> getFiles(@NotNull File directory, @NotNull String folder, @NotNull String extension) {
      return getFiles(folder.isEmpty() ? directory : new File(directory, folder), extension, false);
   }

   public static List<File> getFileObjects(@NotNull File directory, @NotNull String folder, @NotNull String extension) {
      List<File> files = new ArrayList<>();
      File root = new File(directory, folder);
      getFiles(folder.isEmpty() ? directory : root, extension, true).forEach(file -> files.add(new File(root, file)));
      return files;
   }
}
