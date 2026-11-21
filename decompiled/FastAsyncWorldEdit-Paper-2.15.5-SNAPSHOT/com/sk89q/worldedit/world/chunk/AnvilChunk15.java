package com.sk89q.worldedit.world.chunk;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinIntArrayTag;
import org.enginehub.linbus.tree.LinTagType;

public class AnvilChunk15 extends AnvilChunk13 {
   @Deprecated
   public AnvilChunk15(CompoundTag tag) throws DataException {
      super(tag);
   }

   public AnvilChunk15(LinCompoundTag tag) throws DataException {
      super(tag);
   }

   @Override
   public BiomeType getBiome(final BlockVector3 position) throws DataException {
      if (this.biomes == null) {
         this.populateBiomes();
      }

      int x = (position.x() & 15) >> 2;
      int y = position.y() >> 2;
      int z = (position.z() & 15) >> 2;
      return this.biomes[y << 4 | z << 2 | x];
   }

   private void populateBiomes() throws DataException {
      this.biomes = new BiomeType[1024];
      LinIntArrayTag biomeTag = this.rootTag.findTag("Biomes", LinTagType.intArrayTag());
      if (biomeTag != null) {
         int[] stored = biomeTag.value();

         for (int i = 0; i < 1024; i++) {
            this.biomes[i] = BiomeTypes.getLegacy(stored[i]);
         }
      }
   }
}
