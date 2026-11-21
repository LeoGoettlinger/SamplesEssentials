package de.oliver.jdb;

import com.google.gson.annotations.SerializedName;
import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public record JIndex(String name, @SerializedName("base_path") String basePath, @SerializedName("index_map") Map<String, String> indexMap) {
   public static JIndex load(String name, String basePath) {
      File indexFile = new File(basePath, name + ".json");
      if (!indexFile.exists()) {
         return new JIndex(name, basePath, new ConcurrentHashMap<>());
      } else {
         try {
            JIndex var4;
            try (BufferedReader reader = Files.newBufferedReader(indexFile.toPath())) {
               var4 = JDB.GSON.fromJson(reader, JIndex.class);
            }

            return var4;
         } catch (Exception var8) {
            var8.printStackTrace();
            return new JIndex(name, basePath, new ConcurrentHashMap<>());
         }
      }
   }

   public void save() {
      File indexFile = new File(this.basePath, this.name + ".json");
      String json = JDB.GSON.toJson(this);

      try {
         Files.write(indexFile.toPath(), json.getBytes());
      } catch (Exception var4) {
         var4.printStackTrace();
      }
   }
}
