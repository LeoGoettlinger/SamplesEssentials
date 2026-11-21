package com.magmaguy.elitemobs.shaded.reflections.serializers;

import com.magmaguy.elitemobs.shaded.reflections.Reflections;
import java.io.File;
import java.io.InputStream;

public interface Serializer {
   Reflections read(InputStream var1);

   File save(Reflections var1, String var2);

   static File prepareFile(String filename) {
      File file = new File(filename);
      File parent = file.getAbsoluteFile().getParentFile();
      if (!parent.exists()) {
         parent.mkdirs();
      }

      return file;
   }
}
