package com.sk89q.worldedit.util.io.file;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class SafeFiles {
   private static final FileAttribute<?>[] OWNER_ONLY_FILE_ATTRS;
   private static final FileAttribute<?>[] OWNER_ONLY_DIR_ATTRS;

   public static Stream<Path> noLeakFileList(Path dir) throws IOException {
      Stream var2;
      try (Stream<Path> stream = Files.list(dir)) {
         var2 = stream.collect(Collectors.toList()).stream();
      }

      return var2;
   }

   public static String canonicalFileName(Path path) {
      return dropSlash(path.getFileName().toString());
   }

   private static String dropSlash(String name) {
      return !name.isEmpty() && name.codePointBefore(name.length()) == 47 ? name.substring(0, name.length() - 1) : name;
   }

   public static void tryHardToDeleteDir(Path directory) throws IOException {
      if (!Files.isDirectory(directory)) {
         if (Files.exists(directory)) {
            throw new IOException(directory + " is not a directory");
         }
      } else {
         try (Stream<Path> files = Files.list(directory)) {
            Iterator<Path> iter = files.iterator();

            while (iter.hasNext()) {
               Path next = iter.next();
               if (Files.isDirectory(next)) {
                  tryHardToDeleteDir(next);
               } else {
                  tryHardToDelete(next);
               }
            }
         }

         tryHardToDelete(directory);
      }
   }

   public static void tryHardToDelete(Path path) throws IOException {
      IOException suppressed = tryDelete(path);
      if (suppressed != null) {
         System.gc();

         try {
            Thread.sleep(10L);
         } catch (InterruptedException var4) {
            Thread.currentThread().interrupt();
         }

         IOException suppressed2 = tryDelete(path);
         if (suppressed2 != null) {
            IOException ex = new IOException("Failed to delete " + path, suppressed2);
            ex.addSuppressed(suppressed);
            throw ex;
         }
      }
   }

   @Nullable
   private static IOException tryDelete(Path path) {
      try {
         Files.deleteIfExists(path);
         return Files.exists(path) ? new IOException(path + " still exists after deleting") : null;
      } catch (IOException var2) {
         return var2;
      }
   }

   public static FileAttribute<?>[] getOwnerOnlyFileAttributes(AttributeTarget attributeTarget) {
      switch (attributeTarget) {
         case FILE:
            return OWNER_ONLY_FILE_ATTRS;
         case DIRECTORY:
            return OWNER_ONLY_DIR_ATTRS;
         default:
            throw new IllegalStateException("Unknown attribute target " + attributeTarget);
      }
   }

   private SafeFiles() {
   }

   static {
      if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
         OWNER_ONLY_FILE_ATTRS = new FileAttribute[]{
            PosixFilePermissions.asFileAttribute(ImmutableSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE))
         };
         OWNER_ONLY_DIR_ATTRS = new FileAttribute[]{
            PosixFilePermissions.asFileAttribute(
               ImmutableSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE)
            )
         };
      } else {
         OWNER_ONLY_FILE_ATTRS = new FileAttribute[0];
         OWNER_ONLY_DIR_ATTRS = new FileAttribute[0];
      }
   }
}
