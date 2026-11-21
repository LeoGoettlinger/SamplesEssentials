package com.magmaguy.elitemobs.shaded.reflections.vfs;

import com.magmaguy.elitemobs.shaded.reflections.ReflectionsException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

public class SystemDir implements Vfs.Dir {
   private final File file;

   public SystemDir(File file) {
      if (file == null || file.isDirectory() && file.canRead()) {
         this.file = file;
      } else {
         throw new RuntimeException("cannot use dir " + file);
      }
   }

   @Override
   public String getPath() {
      return this.file != null ? this.file.getPath().replace("\\", "/") : "/NO-SUCH-DIRECTORY/";
   }

   @Override
   public Iterable<Vfs.File> getFiles() {
      return (Iterable<Vfs.File>)(this.file != null && this.file.exists() ? () -> {
         try {
            return Files.walk(this.file.toPath()).filter(x$0 -> Files.isRegularFile(x$0)).map(path -> new SystemFile(this, path.toFile())).iterator();
         } catch (IOException var2) {
            throw new ReflectionsException("could not get files for " + this.file, var2);
         }
      } : Collections.emptyList());
   }
}
