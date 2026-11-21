package com.sk89q.worldedit.internal.anvil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector2;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.Logger;

public final class ChunkDeleter {
   public static final String DELCHUNKS_FILE_NAME = "delete_chunks.json";
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private static final Comparator<BlockVector2> chunkSorter = Comparator.comparing(pos -> (pos.x() & 31) + (pos.z() & 31) * 32);
   private static final Gson chunkDeleterGson = new GsonBuilder()
      .registerTypeAdapter(BlockVector2.class, new ChunkDeleter.BlockVector2Adapter().nullSafe())
      .setPrettyPrinting()
      .create();
   private final ChunkDeletionInfo chunkDeletionInfo;
   private final Set<Path> backedUpRegions = new HashSet<>();
   private boolean shouldPreload;
   private int debugRate = 100;
   private int totalChunksDeleted = 0;
   private int deletionsRequested = 0;

   public static ChunkDeletionInfo readInfo(Path chunkFile) throws IOException, JsonSyntaxException {
      String json = Files.readString(chunkFile);
      return (ChunkDeletionInfo)chunkDeleterGson.fromJson(json, ChunkDeletionInfo.class);
   }

   public static void writeInfo(ChunkDeletionInfo info, Path chunkFile) throws IOException, JsonIOException {
      String json = chunkDeleterGson.toJson(info, (new TypeToken<ChunkDeletionInfo>() {}).getType());

      try (BufferedWriter writer = Files.newBufferedWriter(chunkFile)) {
         writer.write(json);
      }
   }

   public static void runFromFile(Path chunkFile, boolean deleteOnSuccess) {
      ChunkDeleter chunkDeleter;
      try {
         chunkDeleter = createFromFile(chunkFile);
      } catch (IOException | JsonSyntaxException var8) {
         LOGGER.error("Could not parse chunk deletion file. Invalid file?", var8);
         return;
      }

      LOGGER.info("Found chunk deletions. Proceeding with deletion...");
      long start = System.currentTimeMillis();
      if (chunkDeleter.runDeleter()) {
         LOGGER.info(
            "Successfully deleted {} matching chunks (out of {}, taking {} ms).",
            chunkDeleter.getDeletedChunkCount(),
            chunkDeleter.getDeletionsRequested(),
            System.currentTimeMillis() - start
         );
         if (deleteOnSuccess) {
            boolean deletedFile = false;

            try {
               deletedFile = Files.deleteIfExists(chunkFile);
            } catch (IOException var7) {
            }

            if (!deletedFile) {
               LOGGER.warn(
                  "Chunk deletion file could not be cleaned up. This may have unintended consequences on next startup, or if /delchunks is used again."
               );
            }
         }
      } else {
         LOGGER.error("Error occurred while deleting chunks. If world errors occur, stop the server and restore the *.bak backup files.");
      }
   }

   private ChunkDeleter(ChunkDeletionInfo chunkDeletionInfo) {
      this.chunkDeletionInfo = chunkDeletionInfo;
   }

   private static ChunkDeleter createFromFile(Path chunkFile) throws IOException {
      ChunkDeletionInfo info = readInfo(chunkFile);
      if (info == null) {
         throw new IOException("Read null json. Empty file?");
      } else {
         return new ChunkDeleter(info);
      }
   }

   private boolean runDeleter() {
      return this.chunkDeletionInfo.batches.stream().allMatch(this::runBatch);
   }

   private boolean runBatch(ChunkDeletionInfo.ChunkBatch chunkBatch) {
      int chunkCount = chunkBatch.getChunkCount();
      LOGGER.info("Processing deletion batch with {} chunks.", chunkCount);
      Map<Path, Stream<BlockVector2>> regionToChunkList = this.groupChunks(chunkBatch);
      BiPredicate<RegionAccess, BlockVector2> predicate = this.createPredicates(chunkBatch.deletionPredicates);
      this.shouldPreload = chunkBatch.chunks == null;
      this.deletionsRequested += chunkCount;
      this.debugRate = chunkCount / 10;
      return regionToChunkList.entrySet().stream().allMatch(entry -> {
         Path regionPath = entry.getKey();
         if (!Files.exists(regionPath)) {
            return true;
         } else {
            if (chunkBatch.backup && !this.backedUpRegions.contains(regionPath)) {
               try {
                  this.backupRegion(regionPath);
               } catch (IOException var6) {
                  LOGGER.warn("Error backing up region file: " + regionPath + ". Aborting the process.", var6);
                  return false;
               }
            }

            return this.deleteChunks(regionPath, entry.getValue(), predicate);
         }
      });
   }

   private Map<Path, Stream<BlockVector2>> groupChunks(ChunkDeletionInfo.ChunkBatch chunkBatch) {
      Path worldPath = Paths.get(chunkBatch.worldPath);
      if (chunkBatch.chunks != null) {
         return chunkBatch.chunks
            .stream()
            .collect(Collectors.groupingBy(ChunkDeleter.RegionFilePos::new))
            .entrySet()
            .stream()
            .collect(Collectors.toMap(e -> worldPath.resolve("region").resolve(e.getKey().getFileName()), e -> e.getValue().stream().sorted(chunkSorter)));
      } else {
         BlockVector2 minChunk = chunkBatch.minChunk;
         BlockVector2 maxChunk = chunkBatch.maxChunk;
         ChunkDeleter.RegionFilePos minRegion = new ChunkDeleter.RegionFilePos(minChunk);
         ChunkDeleter.RegionFilePos maxRegion = new ChunkDeleter.RegionFilePos(maxChunk);
         Map<Path, Stream<BlockVector2>> groupedChunks = new HashMap<>();

         for (int regX = minRegion.getX(); regX <= maxRegion.getX(); regX++) {
            for (int regZ = minRegion.getZ(); regZ <= maxRegion.getZ(); regZ++) {
               Path regionPath = worldPath.resolve("region").resolve(new ChunkDeleter.RegionFilePos(regX, regZ).getFileName());
               if (Files.exists(regionPath)) {
                  int startX = regX << 5;
                  int endX = (regX << 5) + 31;
                  int startZ = regZ << 5;
                  int endZ = (regZ << 5) + 31;
                  int minX = Math.max(Math.min(startX, endX), minChunk.x());
                  int minZ = Math.max(Math.min(startZ, endZ), minChunk.z());
                  int maxX = Math.min(Math.max(startX, endX), maxChunk.x());
                  int maxZ = Math.min(Math.max(startZ, endZ), maxChunk.z());
                  Stream<BlockVector2> stream = Stream.iterate(BlockVector2.at(minX, minZ), bv2 -> {
                     int nextX = bv2.x();
                     int nextZ = bv2.z();
                     if (++nextX > maxX) {
                        nextX = minX;
                        if (++nextZ > maxZ) {
                           return null;
                        }
                     }

                     return BlockVector2.at(nextX, nextZ);
                  });
                  groupedChunks.put(regionPath, stream);
               }
            }
         }

         return groupedChunks;
      }
   }

   private BiPredicate<RegionAccess, BlockVector2> createPredicates(List<ChunkDeletionInfo.DeletionPredicate> deletionPredicates) {
      return deletionPredicates == null
         ? (r, p) -> true
         : deletionPredicates.stream().map(this::createPredicate).reduce(BiPredicate::and).orElse((r, p) -> true);
   }

   private BiPredicate<RegionAccess, BlockVector2> createPredicate(ChunkDeletionInfo.DeletionPredicate deletionPredicate) {
      if ("modification".equals(deletionPredicate.property)) {
         int time;
         try {
            time = Integer.parseInt(deletionPredicate.value);
         } catch (NumberFormatException var5) {
            throw new IllegalStateException("Modification time predicate specified invalid time: " + deletionPredicate.value);
         }

         String e = deletionPredicate.comparison;
         switch (e) {
            case "<":
               return (r, p) -> {
                  try {
                     return r.getModificationTime(p) < time;
                  } catch (IOException var4) {
                     return false;
                  }
               };
            case ">":
               return (r, p) -> {
                  try {
                     return r.getModificationTime(p) > time;
                  } catch (IOException var4) {
                     return false;
                  }
               };
            default:
               throw new IllegalStateException("Unexpected comparison value: " + deletionPredicate.comparison);
         }
      } else {
         throw new IllegalStateException("Unexpected property value: " + deletionPredicate.property);
      }
   }

   private void backupRegion(Path regionFile) throws IOException {
      Path backupFile = regionFile.resolveSibling(regionFile.getFileName() + ".bak");
      Files.copy(regionFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
      this.backedUpRegions.add(backupFile);
   }

   private boolean deleteChunks(Path regionFile, Stream<BlockVector2> chunks, BiPredicate<RegionAccess, BlockVector2> deletionPredicate) {
      try {
         boolean var10;
         try (RegionAccess region = new RegionAccess(regionFile, this.shouldPreload)) {
            Iterator<BlockVector2> iterator = chunks.iterator();

            while (iterator.hasNext()) {
               BlockVector2 chunk = iterator.next();
               if (chunk == null) {
                  break;
               }

               if (deletionPredicate.test(region, chunk)) {
                  region.deleteChunk(chunk);
                  this.totalChunksDeleted++;
                  if (this.debugRate != 0 && this.totalChunksDeleted % this.debugRate == 0) {
                     LOGGER.info("Deleted {} chunks so far.", this.totalChunksDeleted);
                  }
               } else {
                  LOGGER.warn("Chunk did not match predicates: {}", chunk);
               }
            }

            var10 = true;
         }

         return var10;
      } catch (IOException var9) {
         LOGGER.warn("Error deleting chunks from region: " + regionFile + ". Aborting the process.", var9);
         return false;
      }
   }

   public int getDeletedChunkCount() {
      return this.totalChunksDeleted;
   }

   public int getDeletionsRequested() {
      return this.deletionsRequested;
   }

   private static class BlockVector2Adapter extends TypeAdapter<BlockVector2> {
      public void write(JsonWriter out, BlockVector2 value) throws IOException {
         out.beginArray();
         out.value(value.x());
         out.value(value.z());
         out.endArray();
      }

      public BlockVector2 read(JsonReader in) throws IOException {
         in.beginArray();
         int x = in.nextInt();
         int z = in.nextInt();
         in.endArray();
         return BlockVector2.at(x, z);
      }
   }

   private static class RegionFilePos {
      private final int x;
      private final int z;

      RegionFilePos(BlockVector2 chunk) {
         this.x = chunk.x() >> 5;
         this.z = chunk.z() >> 5;
      }

      RegionFilePos(int regX, int regZ) {
         this.x = regX;
         this.z = regZ;
      }

      public int getX() {
         return this.x;
      }

      public int getZ() {
         return this.z;
      }

      public String getFileName() {
         return "r." + this.x + "." + this.z + ".mca";
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            ChunkDeleter.RegionFilePos that = (ChunkDeleter.RegionFilePos)o;
            return this.x != that.x ? false : this.z == that.z;
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         int result = this.x;
         return 31 * result + this.z;
      }

      @Override
      public String toString() {
         return "(" + this.x + ", " + this.z + ")";
      }
   }
}
