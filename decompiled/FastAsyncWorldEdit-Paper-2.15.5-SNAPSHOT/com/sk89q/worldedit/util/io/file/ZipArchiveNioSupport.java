package com.sk89q.worldedit.util.io.file;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Optional;

public final class ZipArchiveNioSupport implements ArchiveNioSupport {
   private static final ZipArchiveNioSupport INSTANCE = new ZipArchiveNioSupport();

   public static ZipArchiveNioSupport getInstance() {
      return INSTANCE;
   }

   private ZipArchiveNioSupport() {
   }

   @Override
   public Optional<ArchiveDir> tryOpenAsDir(Path archive) throws IOException {
      if (!archive.getFileName().toString().endsWith(".zip")) {
         return Optional.empty();
      } else {
         final FileSystem zipFs = FileSystems.newFileSystem(archive, this.getClass().getClassLoader());
         final Path root = ArchiveNioSupports.skipRootSameName(zipFs.getPath("/"), archive.getFileName().toString().replaceFirst("\\.zip$", ""));
         return Optional.of(new ArchiveDir() {
            @Override
            public Path getPath() {
               return root;
            }

            @Override
            public void close() throws IOException {
               zipFs.close();
            }
         });
      }
   }
}
