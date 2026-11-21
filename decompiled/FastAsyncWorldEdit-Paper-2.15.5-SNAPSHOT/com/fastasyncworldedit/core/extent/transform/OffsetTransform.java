package com.fastasyncworldedit.core.extent.transform;

import com.fastasyncworldedit.core.extent.ResettableExtent;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockStateHolder;

public class OffsetTransform extends ResettableExtent {
   private final int dx;
   private final int dy;
   private final int dz;

   public OffsetTransform(Extent parent, int dx, int dy, int dz) {
      super(parent);
      this.dx = dx;
      this.dy = dy;
      this.dz = dz;
   }

   @Override
   public boolean setBiome(BlockVector3 location, BiomeType biome) {
      int x = location.x() + this.dx;
      int y = location.x() + this.dy;
      int z = location.x() + this.dz;
      return !this.getExtent().contains(x, y, z) ? false : this.getExtent().setBiome(x, y, z, biome);
   }

   @Override
   public boolean setBiome(int x, int y, int z, BiomeType biome) {
      x += this.dx;
      y += this.dy;
      z += this.dz;
      return !this.getExtent().contains(x, y, z) ? false : this.getExtent().setBiome(x, y, z, biome);
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) throws WorldEditException {
      int x = location.x() + this.dx;
      int y = location.x() + this.dy;
      int z = location.x() + this.dz;
      return !this.getExtent().contains(x, y, z) ? false : this.getExtent().setBlock(x, y, z, block);
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(int x, int y, int z, T block) throws WorldEditException {
      x += this.dx;
      y += this.dy;
      z += this.dz;
      return !this.getExtent().contains(x, y, z) ? false : this.getExtent().setBlock(x, y, z, block);
   }
}
