package com.magmaguy.elitemobs.shaded.reflections.vfs;

import com.magmaguy.elitemobs.shaded.reflections.Reflections;
import java.io.IOException;
import java.util.jar.JarFile;

public class ZipDir implements Vfs.Dir {
   final java.util.zip.ZipFile jarFile;

   public ZipDir(JarFile jarFile) {
      this.jarFile = jarFile;
   }

   @Override
   public String getPath() {
      return this.jarFile != null ? this.jarFile.getName().replace("\\", "/") : "/NO-SUCH-DIRECTORY/";
   }

   @Override
   public Iterable<Vfs.File> getFiles() {
      return () -> this.jarFile.stream().filter(entry -> !entry.isDirectory()).map(entry -> new ZipFile(this, entry)).iterator();
   }

   @Override
   public void close() {
      try {
         this.jarFile.close();
      } catch (IOException var2) {
         if (Reflections.log != null) {
            Reflections.log.warn("Could not close JarFile", (Throwable)var2);
         }
      }
   }

   @Override
   public String toString() {
      return this.jarFile.getName();
   }
}
