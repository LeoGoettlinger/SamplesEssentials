package com.viaversion.viaversion.protocols.v1_21to1_21_2.storage;

import com.viaversion.viaversion.api.minecraft.ChunkPosition;
import com.viaversion.viaversion.libs.fastutil.longs.LongOpenHashSet;
import com.viaversion.viaversion.libs.fastutil.longs.LongSet;

public class ChunkLoadTracker {
   private final LongSet loadedChunks = new LongOpenHashSet();

   public void addChunk(int x, int z) {
      this.loadedChunks.add(ChunkPosition.chunkKey(x, z));
   }

   public void removeChunk(int x, int z) {
      this.loadedChunks.remove(ChunkPosition.chunkKey(x, z));
   }

   public boolean isChunkLoaded(int x, int z) {
      return this.loadedChunks.contains(ChunkPosition.chunkKey(x, z));
   }

   public void clear() {
      this.loadedChunks.clear();
   }
}
