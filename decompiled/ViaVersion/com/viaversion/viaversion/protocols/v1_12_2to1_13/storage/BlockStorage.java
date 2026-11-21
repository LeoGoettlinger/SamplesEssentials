package com.viaversion.viaversion.protocols.v1_12_2to1_13.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.ChunkPosition;
import com.viaversion.viaversion.libs.fastutil.ints.IntOpenHashSet;
import com.viaversion.viaversion.libs.fastutil.ints.IntSet;
import com.viaversion.viaversion.libs.fastutil.longs.Long2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.Map;

public class BlockStorage implements StorableObject {
   private static final IntSet WHITELIST = new IntOpenHashSet(46);
   private final Long2ObjectMap<Map<BlockPosition, BlockStorage.ReplacementData>> blocks = new Long2ObjectOpenHashMap<>();

   public void store(BlockPosition position, int block) {
      this.store(position, block, -1);
   }

   public void store(BlockPosition position, int block, int replacementId) {
      if (WHITELIST.contains(block)) {
         long chunkKey = ChunkPosition.chunkKeyForBlock(position.x(), position.z());
         this.blocks.computeIfAbsent(chunkKey, key -> new HashMap<>()).put(position, new BlockStorage.ReplacementData(block, replacementId));
      }
   }

   public boolean isWelcome(int block) {
      return WHITELIST.contains(block);
   }

   public boolean contains(BlockPosition position) {
      Map<BlockPosition, BlockStorage.ReplacementData> chunkBlocks = this.blocks.get(ChunkPosition.chunkKeyForBlock(position.x(), position.z()));
      return chunkBlocks != null && chunkBlocks.containsKey(position);
   }

   public BlockStorage.ReplacementData get(BlockPosition position) {
      Map<BlockPosition, BlockStorage.ReplacementData> chunkBlocks = this.blocks.get(ChunkPosition.chunkKeyForBlock(position.x(), position.z()));
      return chunkBlocks != null ? chunkBlocks.get(position) : null;
   }

   public BlockStorage.ReplacementData remove(BlockPosition position) {
      long chunkKey = ChunkPosition.chunkKeyForBlock(position.x(), position.z());
      Map<BlockPosition, BlockStorage.ReplacementData> chunkBlocks = this.blocks.get(chunkKey);
      if (chunkBlocks == null) {
         return null;
      } else {
         BlockStorage.ReplacementData removed = chunkBlocks.remove(position);
         if (chunkBlocks.isEmpty()) {
            this.blocks.remove(chunkKey);
         }

         return removed;
      }
   }

   public void removeChunk(int chunkX, int chunkZ) {
      this.blocks.remove(ChunkPosition.chunkKey(chunkX, chunkZ));
   }

   public void clear() {
      this.blocks.clear();
   }

   static {
      WHITELIST.add(5266);

      for (int i = 0; i < 16; i++) {
         WHITELIST.add(972 + i);
      }

      for (int i = 0; i < 20; i++) {
         WHITELIST.add(6854 + i);
      }

      for (int i = 0; i < 4; i++) {
         WHITELIST.add(7110 + i);
      }

      for (int i = 0; i < 5; i++) {
         WHITELIST.add(5447 + i);
      }
   }

   public static final class ReplacementData {
      private final int original;
      private int replacement;

      public ReplacementData(int original, int replacement) {
         this.original = original;
         this.replacement = replacement;
      }

      public int getOriginal() {
         return this.original;
      }

      public int getReplacement() {
         return this.replacement;
      }

      public void setReplacement(int replacement) {
         this.replacement = replacement;
      }
   }
}
