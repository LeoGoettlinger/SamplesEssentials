package com.magmaguy.elitemobs.magmacore.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.bukkit.World.Environment;

public final class WorldBlueprint {
   private static final Set<Path> IDENTITY_FILES = Set.of(
      Path.of("uid.dat"), Path.of("session.lock"), Path.of("data", "paper", "metadata.dat"), Path.of("data", "paper", "metadata.dat_old")
   );
   private final Path root;
   private final Path terrain;
   private final WorldBlueprint.Layout layout;

   private WorldBlueprint(Path root, Path terrain, WorldBlueprint.Layout layout) {
      this.root = root;
      this.terrain = terrain;
      this.layout = layout;
   }

   public static WorldBlueprint inspect(Path root, Environment environment) throws IOException {
      if (!Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS)) {
         throw new IOException("Blueprint directory is missing: " + root);
      } else if (Files.exists(root.resolve("dimensions"))) {
         throw new IOException("Export a single dimension, not the server's dimensions container: " + root);
      } else {
         Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {
               if (!Files.isSymbolicLink(path) && !attrs.isOther()) {
                  return FileVisitResult.CONTINUE;
               } else {
                  throw new IOException("Linked blueprint directory: " + path);
               }
            }

            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
               if (attrs.isRegularFile() && !Files.isSymbolicLink(path)) {
                  return FileVisitResult.CONTINUE;
               } else {
                  throw new IOException("Non-regular blueprint file: " + path);
               }
            }
         });
         List<Path> candidates = new ArrayList<>();

         for (Path candidate : List.of(root, root.resolve("DIM-1"), root.resolve("DIM1"))) {
            if (hasRegions(candidate)) {
               candidates.add(candidate);
            }
         }

         if (candidates.size() != 1) {
            throw new IOException("Expected one terrain dimension with region files, found " + candidates.size() + ": " + root);
         } else {
            Path terrain = candidates.getFirst();
            boolean metadata = Files.isRegularFile(root.resolve("level.dat")) || Files.isRegularFile(root.resolve("level.dat_old"));
            boolean modern = !metadata
               && (Files.isDirectory(root.resolve("data").resolve("paper")) || Files.isDirectory(root.resolve("data").resolve("minecraft")));
            if ((!metadata || !Files.isRegularFile(root.resolve("data").resolve("paper").resolve("metadata.dat"))) && (!modern || terrain.equals(root))) {
               if (!terrain.equals(root) && !terrain.equals(root.resolve(legacyDimension(environment)))) {
                  throw new IOException("Blueprint dimension does not match configured environment " + environment + ": " + root);
               } else {
                  return new WorldBlueprint(
                     root,
                     terrain,
                     modern ? WorldBlueprint.Layout.MODERN_DIMENSION : (metadata ? WorldBlueprint.Layout.LEGACY_WORLD : WorldBlueprint.Layout.LEGACY_TERRAIN)
                  );
               }
            } else {
               throw new IOException("Mixed legacy and modern blueprint layout: " + root);
            }
         }
      }
   }

   private static boolean hasRegions(Path directory) throws IOException {
      Path region = directory.resolve("region");
      if (!Files.isDirectory(region)) {
         return false;
      } else {
         boolean var3;
         try (Stream<Path> files = Files.list(region)) {
            var3 = files.anyMatch(file -> Files.isRegularFile(file) && file.getFileName().toString().matches("r\\.-?\\d+\\.-?\\d+\\.mca"));
         }

         return var3;
      }
   }

   private static String legacyDimension(Environment environment) throws IOException {
      return switch (environment) {
         case NORMAL -> "";
         case NETHER -> "DIM-1";
         case THE_END -> "DIM1";
         default -> throw new IOException("Unsupported blueprint environment: " + environment);
      };
   }

   public void requireCompatibleServer(boolean modernServer) throws IOException {
      if (this.layout == WorldBlueprint.Layout.MODERN_DIMENSION && !modernServer) {
         throw new IOException("Modern dimension exports require a server using modern dimension storage: " + this.root);
      }
   }

   public boolean usesModernDestination(boolean modernServer) throws IOException {
      this.requireCompatibleServer(modernServer);
      return modernServer && this.layout != WorldBlueprint.Layout.LEGACY_WORLD;
   }

   public void copyTo(Path destination, Environment environment, boolean modernServer) throws IOException {
      this.requireCompatibleServer(modernServer);
      final Path source = this.layout == WorldBlueprint.Layout.LEGACY_TERRAIN ? this.terrain : this.root;
      String child = this.layout == WorldBlueprint.Layout.LEGACY_TERRAIN && !modernServer ? legacyDimension(environment) : "";
      Files.createDirectories(destination.getParent());
      Files.createDirectory(destination);

      try {
         final Path output = destination.resolve(child);
         Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attrs) throws IOException {
               if (!Files.isSymbolicLink(directory) && !attrs.isOther()) {
                  Files.createDirectories(output.resolve(source.relativize(directory)));
                  return FileVisitResult.CONTINUE;
               } else {
                  throw new IOException("Linked blueprint directory: " + directory);
               }
            }

            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
               if (attrs.isRegularFile() && !Files.isSymbolicLink(file)) {
                  Path relative = source.relativize(file);
                  if (WorldBlueprint.IDENTITY_FILES.contains(relative)) {
                     return FileVisitResult.CONTINUE;
                  } else {
                     Files.copy(file, output.resolve(relative), StandardCopyOption.COPY_ATTRIBUTES);
                     return FileVisitResult.CONTINUE;
                  }
               } else {
                  throw new IOException("Non-regular blueprint file: " + file);
               }
            }
         });
      } catch (RuntimeException | IOException var9) {
         try {
            Files.walkFileTree(destination, new SimpleFileVisitor<Path>() {
               public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                  Files.delete(file);
                  return FileVisitResult.CONTINUE;
               }

               public FileVisitResult postVisitDirectory(Path directory, IOException problem) throws IOException {
                  if (problem != null) {
                     throw problem;
                  } else {
                     Files.delete(directory);
                     return FileVisitResult.CONTINUE;
                  }
               }
            });
         } catch (IOException var8) {
            var9.addSuppressed(var8);
         }

         throw var9;
      }
   }

   private static enum Layout {
      LEGACY_WORLD,
      LEGACY_TERRAIN,
      MODERN_DIMENSION;
   }
}
