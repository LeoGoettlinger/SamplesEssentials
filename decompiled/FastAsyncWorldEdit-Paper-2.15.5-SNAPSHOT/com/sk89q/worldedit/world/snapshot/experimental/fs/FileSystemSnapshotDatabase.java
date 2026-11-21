package com.sk89q.worldedit.world.snapshot.experimental.fs;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.net.UrlEscapers;
import com.sk89q.worldedit.util.function.IOFunction;
import com.sk89q.worldedit.util.function.IORunnable;
import com.sk89q.worldedit.util.io.Closer;
import com.sk89q.worldedit.util.io.file.ArchiveDir;
import com.sk89q.worldedit.util.io.file.ArchiveNioSupport;
import com.sk89q.worldedit.util.io.file.MorePaths;
import com.sk89q.worldedit.util.io.file.SafeFiles;
import com.sk89q.worldedit.util.time.FileNameDateTimeParser;
import com.sk89q.worldedit.util.time.ModificationDateTimeParser;
import com.sk89q.worldedit.util.time.SnapshotDateTimeParser;
import com.sk89q.worldedit.world.snapshot.experimental.Snapshot;
import com.sk89q.worldedit.world.snapshot.experimental.SnapshotDatabase;
import com.sk89q.worldedit.world.snapshot.experimental.SnapshotInfo;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class FileSystemSnapshotDatabase implements SnapshotDatabase {
   private static final String SCHEME = "snapfs";
   private static final List<SnapshotDateTimeParser> DATE_TIME_PARSERS = new Builder()
      .add(FileNameDateTimeParser.getInstance())
      .addAll(ServiceLoader.load(SnapshotDateTimeParser.class))
      .add(ModificationDateTimeParser.getInstance())
      .build();
   private final Path root;
   private final ArchiveNioSupport archiveNioSupport;

   public static ZonedDateTime tryParseDate(Path path) {
      return tryParseDateInternal(path).orElseThrow(() -> new IllegalStateException("Could not detect date of " + path));
   }

   private static Optional<ZonedDateTime> tryParseDateInternal(Path path) {
      return DATE_TIME_PARSERS.stream().map(parser -> parser.detectDateTime(path)).filter(Objects::nonNull).findFirst();
   }

   public static URI createUri(String name) {
      return URI.create("snapfs:" + UrlEscapers.urlFragmentEscaper().escape(name));
   }

   public static FileSystemSnapshotDatabase maybeCreate(Path root, ArchiveNioSupport archiveNioSupport) throws IOException {
      Files.createDirectories(root);
      return new FileSystemSnapshotDatabase(root, archiveNioSupport);
   }

   public FileSystemSnapshotDatabase(Path root, ArchiveNioSupport archiveNioSupport) {
      Preconditions.checkArgument(Files.isDirectory(root), "Database root is not a directory");

      try {
         this.root = root.toRealPath();
      } catch (IOException var4) {
         throw new RuntimeException("Failed to resolve snapshot database path", var4);
      }

      this.archiveNioSupport = archiveNioSupport;
   }

   private SnapshotInfo createSnapshotInfo(Path idPath, Path ioPath) {
      ZonedDateTime date = tryParseDateInternal(idPath).orElseGet(() -> tryParseDate(ioPath));
      return SnapshotInfo.create(createUri(idPath.toString()), date);
   }

   private Snapshot createSnapshot(Path idPath, Path ioPath, @Nullable Closer closeCallback) {
      return new FolderSnapshot(this.createSnapshotInfo(idPath, ioPath), ioPath, closeCallback);
   }

   public Path getRoot() {
      return this.root;
   }

   @Override
   public String getScheme() {
      return "snapfs";
   }

   @Override
   public Optional<Snapshot> getSnapshot(URI name) throws IOException {
      return !name.getScheme().equals("snapfs") ? Optional.empty() : this.getSnapshot(name.getSchemeSpecificPart());
   }

   private Optional<Snapshot> getSnapshot(String id) throws IOException {
      Path rawResolved = this.root.resolve(id);
      Path ioPath = rawResolved.normalize();
      if (!ioPath.startsWith(this.root)) {
         return Optional.empty();
      } else {
         Path idPath = this.root.relativize(ioPath);
         Optional<Snapshot> result = this.tryRegularFileSnapshot(idPath);
         if (result.isPresent()) {
            return result;
         } else {
            return !Files.isDirectory(ioPath) ? Optional.empty() : Optional.of(this.createSnapshot(idPath, ioPath, null));
         }
      }
   }

   private Optional<Snapshot> tryRegularFileSnapshot(Path idPath) throws IOException {
      Closer closer = Closer.create();
      Path root = this.root;
      Path relative = idPath;
      Iterator<Path> iterator = null;

      try {
         while (true) {
            if (iterator == null) {
               iterator = MorePaths.iterPaths(relative).iterator();
            }

            if (!iterator.hasNext()) {
               closer.close();
               return Optional.empty();
            }

            Path relativeNext = iterator.next();
            Path next = root.resolve(relativeNext);
            if (Files.isRegularFile(next)) {
               Optional<ArchiveDir> newRootOpt = this.archiveNioSupport.tryOpenAsDir(next);
               if (newRootOpt.isPresent()) {
                  ArchiveDir archiveDir = newRootOpt.get();
                  root = archiveDir.getPath();
                  closer.register(archiveDir);
                  relative = root.resolve(relativeNext.relativize(relative).toString());
                  iterator = null;
                  if (Files.exists(relative)) {
                     return Optional.of(this.createSnapshot(idPath, relative, closer));
                  }
               }
            }
         }
      } catch (Throwable var10) {
         throw closer.rethrowAndClose(var10);
      }
   }

   @Override
   public Stream<Snapshot> getSnapshots(String worldName) throws IOException {
      return SafeFiles.noLeakFileList(this.root)
         .flatMap(
            IOFunction.unchecked(
               entry -> {
                  String worldEntry = this.getWorldEntry(worldName, entry);
                  if (worldEntry != null) {
                     return Stream.of(worldEntry);
                  } else {
                     String fileName = SafeFiles.canonicalFileName(entry);
                     return fileName.equals(worldName) && Files.isDirectory(entry) && !Files.exists(entry.resolve("level.dat"))
                        ? this.listTimestampedEntries(worldName, entry).map(id -> worldName + "/" + id)
                        : this.getTimestampedEntries(worldName, entry);
                  }
               }
            )
         )
         .map(IOFunction.unchecked(id -> this.getSnapshot(id).orElseThrow(() -> new AssertionError("Could not find discovered snapshot: " + id))));
   }

   private Stream<String> listTimestampedEntries(String worldName, Path directory) throws IOException {
      return SafeFiles.noLeakFileList(directory).flatMap(IOFunction.unchecked(entry -> this.getTimestampedEntries(worldName, entry)));
   }

   private Stream<String> getTimestampedEntries(String worldName, Path entry) throws IOException {
      ZonedDateTime dateTime = FileNameDateTimeParser.getInstance().detectDateTime(entry);
      if (dateTime == null) {
         return Stream.of();
      } else {
         String fileName = SafeFiles.canonicalFileName(entry);
         if (Files.isDirectory(entry)) {
            return this.listWorldEntries(worldName, entry).map(id -> fileName + "/" + id);
         } else if (!Files.isRegularFile(entry)) {
            return Stream.of();
         } else {
            Optional<ArchiveDir> asArchive = this.archiveNioSupport.tryOpenAsDir(entry);
            if (asArchive.isPresent()) {
               ArchiveDir dir = asArchive.get();
               return this.listWorldEntries(worldName, dir.getPath()).map(id -> fileName + "/" + id).onClose(IORunnable.unchecked(dir::close));
            } else {
               return Stream.of();
            }
         }
      }
   }

   private Stream<String> listWorldEntries(String worldName, Path directory) throws IOException {
      return SafeFiles.noLeakFileList(directory).map(IOFunction.unchecked(entry -> this.getWorldEntry(worldName, entry))).filter(Objects::nonNull);
   }

   private String getWorldEntry(String worldName, Path entry) throws IOException {
      String fileName = SafeFiles.canonicalFileName(entry);
      if (fileName.equals(worldName) && Files.exists(entry.resolve("level.dat"))) {
         return worldName;
      } else {
         if (fileName.startsWith(worldName + ".") && Files.isRegularFile(entry)) {
            Optional<ArchiveDir> asArchive = this.archiveNioSupport.tryOpenAsDir(entry);
            if (asArchive.isPresent()) {
               asArchive.get().close();
               return fileName;
            }
         }

         return null;
      }
   }
}
