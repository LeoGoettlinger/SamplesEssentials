package com.fastasyncworldedit.core.extent.transform;

import com.fastasyncworldedit.core.extent.ResettableExtent;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import java.util.SplittableRandom;

public class RandomOffsetTransform extends ResettableExtent {
   private final int dx;
   private final int dy;
   private final int dz;
   private transient SplittableRandom random;

   public RandomOffsetTransform(Extent parent, int dx, int dy, int dz) {
      super(parent);
      this.dx = dx + 1;
      this.dy = dy + 1;
      this.dz = dz + 1;
      this.random = new SplittableRandom();
   }

   @Override
   public boolean setBiome(BlockVector3 position, BiomeType biome) {
      int x = position.x() + this.random.nextInt(1 + (this.dx << 1)) - this.dx;
      int y = position.y() + this.random.nextInt(1 + (this.dy << 1)) - this.dy;
      int z = position.z() + this.random.nextInt(1 + (this.dz << 1)) - this.dz;
      return !this.getExtent().contains(x, y, z) ? false : this.getExtent().setBiome(x, y, z, biome);
   }

   @Override
   public boolean setBiome(int x, int y, int z, BiomeType biome) {
      x = x + this.random.nextInt(1 + (this.dx << 1)) - this.dx;
      y = y + this.random.nextInt(1 + (this.dy << 1)) - this.dy;
      z = z + this.random.nextInt(1 + (this.dz << 1)) - this.dz;
      return !this.getExtent().contains(x, y, z) ? false : this.getExtent().setBiome(x, y, z, biome);
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 pos, T block) throws WorldEditException {
      int x = pos.x() + this.random.nextInt(1 + (this.dx << 1)) - this.dx;
      int y = pos.y() + this.random.nextInt(1 + (this.dy << 1)) - this.dy;
      int z = pos.z() + this.random.nextInt(1 + (this.dz << 1)) - this.dz;
      return !this.getExtent().contains(x, y, z) ? false : this.getExtent().setBlock(x, y, z, block);
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(int x, int y, int z, T block) throws WorldEditException {
      x = x + this.random.nextInt(1 + (this.dx << 1)) - this.dx;
      y = y + this.random.nextInt(1 + (this.dy << 1)) - this.dy;
      z = z + this.random.nextInt(1 + (this.dz << 1)) - this.dz;
      return !this.getExtent().contains(x, y, z) ? false : this.getExtent().setBlock(x, y, z, block);
   }

   @Override
   public ResettableExtent setExtent(Extent extent) {
      this.random = new SplittableRandom();
      return super.setExtent(extent);
   }
}
