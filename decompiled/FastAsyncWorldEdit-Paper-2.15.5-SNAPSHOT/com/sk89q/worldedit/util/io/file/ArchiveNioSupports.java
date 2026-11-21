package com.sk89q.worldedit.util.io.file;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Stream;

public class ArchiveNioSupports {
   private static final List<ArchiveNioSupport> SUPPORTS;
   private static final ArchiveNioSupport COMBINED = ArchiveNioSupports::tryOpenAsDir;

   public static Optional<ArchiveDir> tryOpenAsDir(Path archive) throws IOException {
      for (ArchiveNioSupport support : SUPPORTS) {
         Optional<ArchiveDir> fs = support.tryOpenAsDir(archive);
         if (fs.isPresent()) {
            return fs;
         }
      }

      return Optional.empty();
   }

   public static ArchiveNioSupport combined() {
      return COMBINED;
   }

   public static Path skipRootSameName(Path root, String name) throws IOException {
      Path innerDir = root.resolve(name);
      if (Files.isDirectory(innerDir)) {
         try (Stream<Path> files = Files.list(root)) {
            return files.allMatch(x$0 -> Files.isDirectory(x$0)) ? innerDir : root;
         }
      } else {
         return root;
      }
   }

   private ArchiveNioSupports() {
   }

   static {
      Builder<ArchiveNioSupport> builder = ImmutableList.builder();
      SUPPORTS = builder.add(ZipArchiveNioSupport.getInstance()).addAll(ServiceLoader.load(ArchiveNioSupport.class)).build();
   }
}
