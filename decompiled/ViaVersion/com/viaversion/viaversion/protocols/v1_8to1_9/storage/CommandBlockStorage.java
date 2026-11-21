package com.viaversion.viaversion.protocols.v1_8to1_9.storage;

import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.ChunkPosition;
import com.viaversion.viaversion.libs.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CommandBlockStorage implements StorableObject {
   private final Map<Long, Map<BlockPosition, CompoundTag>> storedCommandBlocks = new Long2ObjectOpenHashMap<>();
   private boolean permissions;

   public void unloadChunk(int x, int z) {
      this.storedCommandBlocks.remove(ChunkPosition.chunkKey(x, z));
   }

   public void addOrUpdateBlock(BlockPosition position, CompoundTag tag) {
      long chunkKey = ChunkPosition.chunkKeyForBlock(position.x(), position.z());
      Map<BlockPosition, CompoundTag> blocks = this.storedCommandBlocks.computeIfAbsent(chunkKey, k -> new HashMap<>());
      blocks.put(position, tag);
   }

   public Optional<CompoundTag> getCommandBlock(BlockPosition position) {
      Map<BlockPosition, CompoundTag> blocks = this.storedCommandBlocks.get(ChunkPosition.chunkKeyForBlock(position.x(), position.z()));
      if (blocks == null) {
         return Optional.empty();
      } else {
         CompoundTag tag = blocks.get(position);
         if (tag == null) {
            return Optional.empty();
         } else {
            tag = tag.copy();
            tag.put("powered", new ByteTag((byte)0));
            tag.put("auto", new ByteTag((byte)0));
            tag.put("conditionMet", new ByteTag((byte)0));
            return Optional.of(tag);
         }
      }
   }

   public void unloadChunks() {
      this.storedCommandBlocks.clear();
   }

   public boolean isPermissions() {
      return this.permissions;
   }

   public void setPermissions(boolean permissions) {
      this.permissions = permissions;
   }
}
