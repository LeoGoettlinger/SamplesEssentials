package com.viaversion.viaversion.protocols.v1_17_1to1_18.storage;

import com.viaversion.viaversion.api.minecraft.ChunkPosition;
import com.viaversion.viaversion.libs.fastutil.longs.Long2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.longs.Long2ObjectOpenHashMap;
import com.viaversion.viaversion.libs.fastutil.longs.LongOpenHashSet;
import com.viaversion.viaversion.libs.fastutil.longs.LongSet;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ChunkLightStorage {
   private final Long2ObjectMap<ChunkLightStorage.ChunkLight> lightPackets = new Long2ObjectOpenHashMap<>();
   private final LongSet loadedChunks = new LongOpenHashSet();

   public void storeLight(int x, int z, ChunkLightStorage.ChunkLight chunkLight) {
      this.lightPackets.put(ChunkPosition.chunkKey(x, z), chunkLight);
   }

   @Nullable
   public ChunkLightStorage.ChunkLight removeLight(int x, int z) {
      return this.lightPackets.remove(ChunkPosition.chunkKey(x, z));
   }

   @Nullable
   public ChunkLightStorage.ChunkLight getLight(int x, int z) {
      return this.lightPackets.get(ChunkPosition.chunkKey(x, z));
   }

   public boolean addLoadedChunk(int x, int z) {
      return this.loadedChunks.add(ChunkPosition.chunkKey(x, z));
   }

   public boolean isLoaded(int x, int z) {
      return this.loadedChunks.contains(ChunkPosition.chunkKey(x, z));
   }

   public void clear(int x, int z) {
      long index = ChunkPosition.chunkKey(x, z);
      this.lightPackets.remove(index);
      this.loadedChunks.remove(index);
   }

   public void clear() {
      this.loadedChunks.clear();
      this.lightPackets.clear();
   }

   public record ChunkLight(
      boolean trustEdges,
      long[] skyLightMask,
      long[] blockLightMask,
      long[] emptySkyLightMask,
      long[] emptyBlockLightMask,
      byte[][] skyLight,
      byte[][] blockLight
   ) {
   }
}
