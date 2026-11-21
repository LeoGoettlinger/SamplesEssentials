package com.magmaguy.elitemobs.instanced.dungeons;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Supplier;

final class PendingWorldCopy {
   private File output;
   private boolean cancelled;

   boolean copy(Supplier<File> copier) {
      synchronized (this) {
         if (this.cancelled) {
            return false;
         }
      }

      File copied = copier.get();
      if (copied == null) {
         return false;
      } else {
         synchronized (this) {
            if (!this.cancelled) {
               this.output = copied;
               return true;
            }
         }

         discard(copied);
         return false;
      }
   }

   synchronized boolean transferToInitializer() {
      if (!this.cancelled && this.output != null) {
         this.output = null;
         return true;
      } else {
         return false;
      }
   }

   void cancel() {
      File abandoned;
      synchronized (this) {
         this.cancelled = true;
         abandoned = this.output;
         this.output = null;
      }

      if (abandoned != null) {
         discard(abandoned);
      }
   }

   private static void discard(File copied) {
      Path root = copied.toPath();

      try {
         Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
               Files.delete(file);
               return FileVisitResult.CONTINUE;
            }

            public FileVisitResult postVisitDirectory(Path directory, IOException failure) throws IOException {
               if (failure != null) {
                  throw failure;
               } else {
                  Files.delete(directory);
                  return FileVisitResult.CONTINUE;
               }
            }
         });
      } catch (IOException var3) {
         Logger.warn("Could not remove cancelled dungeon copy " + root + ": " + var3.getMessage());
      }
   }
}
