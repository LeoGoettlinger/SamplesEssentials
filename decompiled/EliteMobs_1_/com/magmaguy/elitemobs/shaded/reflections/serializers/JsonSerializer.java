package com.magmaguy.elitemobs.shaded.reflections.serializers;

import com.google.gson.GsonBuilder;
import com.magmaguy.elitemobs.shaded.reflections.Reflections;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class JsonSerializer implements Serializer {
   @Override
   public Reflections read(InputStream inputStream) {
      return (Reflections)new GsonBuilder().setPrettyPrinting().create().fromJson(new InputStreamReader(inputStream), Reflections.class);
   }

   @Override
   public File save(Reflections reflections, String filename) {
      try {
         File file = Serializer.prepareFile(filename);
         String json = new GsonBuilder().setPrettyPrinting().create().toJson(reflections);
         Files.write(file.toPath(), json.getBytes(Charset.defaultCharset()));
         return file;
      } catch (IOException var5) {
         throw new RuntimeException(var5);
      }
   }
}
