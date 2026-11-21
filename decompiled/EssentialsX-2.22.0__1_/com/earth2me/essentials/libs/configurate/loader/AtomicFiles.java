package com.earth2me.essentials.libs.configurate.loader;

import java.io.BufferedWriter;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

public final class AtomicFiles {
   private static final int MAX_TRIES = 2;

   private AtomicFiles() {
   }

   public static Callable<BufferedWriter> atomicWriterFactory(final Path path, final Charset charset) {
      Objects.requireNonNull(path, "path");
      return () -> atomicBufferedWriter(path, charset);
   }

   public static BufferedWriter atomicBufferedWriter(Path path, final Charset charset) throws IOException {
      path = path.toAbsolutePath();

      try {
         while (Files.isSymbolicLink(path)) {
            path = Files.readSymbolicLink(path);
         }
      } catch (IOException | UnsupportedOperationException var4) {
      }

      Path writePath = temporaryPath(path.getParent(), path.getFileName().toString());
      if (Files.exists(path)) {
         Files.copy(path, writePath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
      }

      createDirectoriesIfNecessary(writePath.getParent());
      BufferedWriter output = Files.newBufferedWriter(writePath, charset);
      return new BufferedWriter(new AtomicFiles.AtomicFileWriter(writePath, path, output));
   }

   private static void createDirectoriesIfNecessary(final Path directory) throws IOException {
      if (!Files.isDirectory(directory)) {
         Files.createDirectories(directory);
      }
   }

   private static Path temporaryPath(final Path parent, final String key) {
      String fileName = "."
         + System.nanoTime()
         + ThreadLocalRandom.current().nextInt()
         + Objects.requireNonNull(key, "key").replaceAll("[\\\\/:]", "-")
         + ".tmp";
      return parent.resolve(fileName);
   }

   private static class AtomicFileWriter extends FilterWriter {
      private final Path targetPath;
      private final Path writePath;

      protected AtomicFileWriter(final Path writePath, final Path targetPath, final Writer wrapping) {
         super(wrapping);
         this.writePath = writePath;
         this.targetPath = targetPath;
      }

      @Override
      public void close() throws IOException {
         super.close();

         try {
            Files.move(this.writePath, this.targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
         } catch (AccessDeniedException var6) {
            int tries = 0;

            while (tries < 2) {
               try {
                  Thread.sleep(5L * (tries + 1));
                  Files.move(this.writePath, this.targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                  return;
               } catch (AccessDeniedException var4) {
                  if (tries == 1) {
                     throw var6;
                  }

                  tries++;
               } catch (InterruptedException var5) {
                  Thread.currentThread().interrupt();
                  throw var6;
               }
            }
         }
      }
   }
}
