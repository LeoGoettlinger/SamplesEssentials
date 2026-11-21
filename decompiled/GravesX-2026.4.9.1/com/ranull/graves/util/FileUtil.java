package com.ranull.graves.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtil {
   private FileUtil() {
   }

   public static void moveFile(File file, String name) {
      try {
         Path source = file.toPath();
         Path target = source.resolveSibling(name);
         Files.move(source, target);
      } catch (IOException var4) {
         var4.printStackTrace();
      }
   }

   @Deprecated
   public static void copyFile(File file, String name) {
      try {
         Path source = file.toPath();
         Path target = source.resolveSibling(name);
         Files.copy(source, target);
      } catch (IOException var4) {
         var4.printStackTrace();
      }
   }
}
