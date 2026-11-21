package com.sk89q.worldedit.world.snapshot.experimental.fs;

import com.google.common.base.Preconditions;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.io.Closer;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.snapshot.experimental.Snapshot;
import com.sk89q.worldedit.world.snapshot.experimental.SnapshotInfo;
import com.sk89q.worldedit.world.storage.ChunkStoreHelper;
import com.sk89q.worldedit.world.storage.LegacyChunkStore;
import com.sk89q.worldedit.world.storage.McRegionChunkStore;
import com.sk89q.worldedit.world.storage.McRegionReader;
import com.sk89q.worldedit.world.storage.MissingChunkException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import javax.annotation.Nullable;

public class FolderSnapshot implements Snapshot {
   private static final Object NOT_FOUND_TOKEN = new Object();
   private final SnapshotInfo info;
   private final Path folder;
   private final AtomicReference<Object> regionFolder = new AtomicReference<>();
   @Nullable
   private final Closer closeCallback;

   private static Object getRegionFolder(Path folder) throws IOException {
      Path regionDir = folder.resolve("region");
      if (Files.exists(regionDir)) {
         Preconditions.checkState(Files.isDirectory(regionDir), "Region folder is actually a file");
         return regionDir;
      } else {
         try (Stream<Path> paths = Files.list(folder)) {
            Optional<Path> path = paths.filter(x$0 -> Files.isDirectory(x$0))
               .filter(p -> p.getFileName().toString().startsWith("DIM"))
               .map(p -> p.resolve("region"))
               .filter(x$0 -> Files.isDirectory(x$0))
               .findFirst();
            if (path.isPresent()) {
               return path.get();
            }
         }

         try (Stream<Path> pathsx = Files.list(folder)) {
            if (pathsx.filter(x$0 -> Files.isRegularFile(x$0)).anyMatch(p -> {
               String fileName = p.getFileName().toString();
               return fileName.startsWith("r") && (fileName.endsWith(".mca") || fileName.endsWith(".mcr"));
            })) {
               return folder;
            }
         }

         return NOT_FOUND_TOKEN;
      }
   }

   public FolderSnapshot(SnapshotInfo info, Path folder, @Nullable Closer closeCallback) {
      this.info = info;
      this.folder = folder.toAbsolutePath();
      this.closeCallback = closeCallback;
   }

   public Path getFolder() {
      return this.folder;
   }

   @Override
   public SnapshotInfo getInfo() {
      return this.info;
   }

   private Optional<Path> getRegionFolder() throws IOException {
      Object regFolder = this.regionFolder.get();
      if (regFolder == null) {
         Object update = getRegionFolder(this.folder);
         if (!this.regionFolder.compareAndSet(null, update)) {
            regFolder = this.regionFolder.get();
         } else {
            regFolder = update;
         }
      }

      return regFolder == NOT_FOUND_TOKEN ? Optional.empty() : Optional.of((Path)regFolder);
   }

   @Override
   public CompoundTag getChunkTag(BlockVector3 position) throws DataException, IOException {
      BlockVector2 pos = position.toBlockVector2();
      Optional<Path> regFolder = this.getRegionFolder();
      if (!regFolder.isPresent()) {
         Path chunkFile = this.getFolder().resolve(LegacyChunkStore.getFilename(pos, "/"));
         if (!Files.exists(chunkFile)) {
            throw new MissingChunkException();
         } else {
            return ChunkStoreHelper.readCompoundTag(() -> new GZIPInputStream(Files.newInputStream(chunkFile)));
         }
      } else {
         Path regionFile = regFolder.get().resolve(McRegionChunkStore.getFilename(pos));
         if (!Files.exists(regionFile)) {
            regionFile = regionFile.resolveSibling(regionFile.getFileName().toString().replace(".mca", ".mcr"));
            if (!Files.exists(regionFile)) {
               throw new MissingChunkException();
            }
         }

         CompoundTag var7;
         try (InputStream stream = Files.newInputStream(regionFile)) {
            McRegionReader regionReader = new McRegionReader(stream);
            var7 = ChunkStoreHelper.readCompoundTag(() -> regionReader.getChunkInputStream(pos));
         }

         return var7;
      }
   }

   @Override
   public void close() throws IOException {
      if (this.closeCallback != null) {
         this.closeCallback.close();
      }
   }
}
