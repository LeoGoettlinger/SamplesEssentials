package com.sk89q.worldedit.world.storage;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.World;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nullable;

public abstract class McRegionChunkStore extends ChunkStore {
   public static String getFilename(BlockVector2 position) {
      int x = position.x();
      int z = position.z();
      return "r." + (x >> 5) + "." + (z >> 5) + ".mca";
   }

   protected McRegionReader getReader(BlockVector2 pos, String worldname, @Nullable String folderOverride) throws DataException, IOException {
      String filename = getFilename(pos);
      InputStream stream = this.getInputStream(filename, worldname, folderOverride);
      return new McRegionReader(stream);
   }

   @Override
   public CompoundTag getChunkTag(BlockVector2 position, World world) throws DataException, IOException {
      return ChunkStoreHelper.readCompoundTag(() -> {
         McRegionReader reader = this.getReader(position, world.getName(), null);
         return reader.getChunkInputStream(position);
      });
   }

   @Override
   public CompoundTag getEntitiesTag(BlockVector2 position, World world) {
      try {
         return ChunkStoreHelper.readCompoundTag(() -> {
            McRegionReader reader = this.getReader(position, world.getName(), "entities");
            return reader.getChunkInputStream(position);
         });
      } catch (IOException | DataException var4) {
         return null;
      }
   }

   protected abstract InputStream getInputStream(String name, String worldName, @Nullable String folderOverride) throws IOException, DataException;
}
