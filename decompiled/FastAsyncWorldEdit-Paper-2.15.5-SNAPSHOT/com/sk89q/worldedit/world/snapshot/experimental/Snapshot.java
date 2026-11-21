package com.sk89q.worldedit.world.snapshot.experimental;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.chunk.Chunk;
import com.sk89q.worldedit.world.storage.ChunkStoreHelper;
import java.io.Closeable;
import java.io.IOException;

public interface Snapshot extends Closeable {
   SnapshotInfo getInfo();

   CompoundTag getChunkTag(BlockVector3 position) throws DataException, IOException;

   default Chunk getChunk(BlockVector3 position) throws DataException, IOException {
      return ChunkStoreHelper.getChunk(this.getChunkTag(position));
   }

   @Override
   void close() throws IOException;
}
