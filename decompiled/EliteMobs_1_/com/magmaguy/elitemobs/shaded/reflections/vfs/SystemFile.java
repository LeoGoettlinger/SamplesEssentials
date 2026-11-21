package com.magmaguy.elitemobs.shaded.reflections.vfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class SystemFile implements Vfs.File {
   private final SystemDir root;
   private final File file;

   public SystemFile(SystemDir root, File file) {
      this.root = root;
      this.file = file;
   }

   @Override
   public String getName() {
      return this.file.getName();
   }

   @Override
   public String getRelativePath() {
      String filepath = this.file.getPath().replace("\\", "/");
      return filepath.startsWith(this.root.getPath()) ? filepath.substring(this.root.getPath().length() + 1) : null;
   }

   @Override
   public InputStream openInputStream() {
      try {
         return new FileInputStream(this.file);
      } catch (FileNotFoundException var2) {
         throw new RuntimeException(var2);
      }
   }

   @Override
   public String toString() {
      return this.file.toString();
   }
}
