package com.sk89q.worldedit.world.storage;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.chunk.Chunk;
import java.io.Closeable;
import java.io.IOException;
import javax.annotation.Nullable;

public abstract class ChunkStore implements Closeable {
   public static final int CHUNK_SHIFTS = 4;
   public static final int CHUNK_SHIFTS_Y = 8;

   public static BlockVector3 toChunk3d(BlockVector3 position) {
      return position.shr(4, 8, 4);
   }

   public static BlockVector2 toChunk(BlockVector3 position) {
      return BlockVector2.at(position.x() >> 4, position.z() >> 4);
   }

   public abstract CompoundTag getChunkTag(BlockVector2 position, World world) throws DataException, IOException;

   @Nullable
   public CompoundTag getEntitiesTag(BlockVector2 position, World world) {
      return null;
   }

   public Chunk getChunk(BlockVector2 position, World world) throws DataException, IOException {
      CompoundTag rootTag = this.getChunkTag(position, world);
      int dataVersion = rootTag.getInt("DataVersion");
      if (dataVersion == 0) {
         dataVersion = -1;
      }

      return dataVersion >= 2724 ? ChunkStoreHelper.getChunk(rootTag, () -> this.getEntitiesTag(position, world)) : ChunkStoreHelper.getChunk(rootTag);
   }

   @Override
   public void close() throws IOException {
   }

   public abstract boolean isValid();
}
