package de.oliver.jdb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.jetbrains.annotations.NotNull;

public class JDB {
   public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
   private static final String FILE_EXTENSION = ".json";
   @NotNull
   private final String basePath;
   @NotNull
   private final File baseDirectory;
   private final JIndex index;

   public JDB(@NotNull String basePath) {
      this.basePath = basePath;
      this.baseDirectory = new File(basePath);
      this.index = JIndex.load("jdb_index", basePath);
   }

   public <T> T get(@NotNull String path, @NotNull Class<T> clazz) throws IOException {
      File documentFile = new File(this.baseDirectory, this.createFilePath(path));
      if (!documentFile.exists()) {
         if (this.index.indexMap().containsKey(path)) {
            String indexPath = this.index.indexMap().get(path);
            return this.get(indexPath, clazz);
         } else {
            return null;
         }
      } else {
         BufferedReader bufferedReader = Files.newBufferedReader(documentFile.toPath());
         return GSON.fromJson(bufferedReader, clazz);
      }
   }

   public JDocument getDocument(@NotNull String path) throws IOException {
      File documentFile = new File(this.baseDirectory, this.createFilePath(path));
      if (!documentFile.exists()) {
         return null;
      } else {
         BufferedReader bufferedReader = Files.newBufferedReader(documentFile.toPath());
         Map<String, Object> data = GSON.fromJson(bufferedReader, Map.class);
         return new JDocument(data);
      }
   }

   public <T> List<T> getAll(@NotNull String path, @NotNull Class<T> clazz) throws IOException {
      File directory = new File(this.baseDirectory, path);
      if (!directory.exists()) {
         return new ArrayList<>();
      } else {
         File[] files = directory.listFiles();
         if (files == null) {
            return new ArrayList<>();
         } else {
            List<T> documents = new ArrayList<>(files.length);

            for (File file : files) {
               documents.add(this.get(path + "/" + file.getName().replace(".json", ""), clazz));
            }

            return documents;
         }
      }
   }

   public int countDocuments(@NotNull String path) {
      File directory = new File(this.baseDirectory, path);
      if (!directory.exists()) {
         return 0;
      } else {
         File[] files = directory.listFiles();
         return files == null ? 0 : files.length;
      }
   }

   public <T> void set(@NotNull String path, @NotNull T value) throws IOException {
      File documentFile = new File(this.baseDirectory, this.createFilePath(path));
      if (!documentFile.exists()) {
         documentFile.getParentFile().mkdirs();
         documentFile.createNewFile();
      }

      String json = GSON.toJson(value);
      Files.write(documentFile.toPath(), json.getBytes());
   }

   public <T> void set(@NotNull String path, @NotNull T value, String... indexPaths) throws IOException {
      this.set(path, value);

      for (String indexPath : indexPaths) {
         this.indexDocument(indexPath, path);
      }
   }

   public void indexDocument(@NotNull String originalPath, @NotNull String indexPath) {
      this.index.indexMap().put(originalPath, indexPath);
      this.index.save();
   }

   public void delete(@NotNull String path) {
      for (Entry<String, String> entry : new HashSet<>(this.index.indexMap().entrySet())) {
         if (entry.getKey().equals(path) || entry.getValue().equals(path)) {
            this.index.indexMap().remove(entry.getKey());
            this.index.save();
         }
      }

      File file = new File(this.baseDirectory, path);
      if (file.isDirectory()) {
         this.deleteDirectory(file);
      } else {
         File documentFile = new File(this.baseDirectory, this.createFilePath(path));
         if (documentFile.exists()) {
            documentFile.delete();
         }
      }
   }

   private String createFilePath(@NotNull String path) {
      return path + ".json";
   }

   private void deleteDirectory(File directory) {
      File[] files = directory.listFiles();
      if (files != null) {
         for (File f : files) {
            if (f.isDirectory()) {
               this.deleteDirectory(f);
            } else {
               f.delete();
            }
         }
      }

      directory.delete();
   }
}
