package com.fastasyncworldedit.core.extent;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;

public class BlockTranslateExtent extends AbstractDelegateExtent {
   private final int dx;
   private final int dy;
   private final int dz;

   public BlockTranslateExtent(Extent extent, int dx, int dy, int dz) {
      super(extent);
      this.dx = dx;
      this.dy = dy;
      this.dz = dz;
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) throws WorldEditException {
      return this.getExtent().setBlock(location.x() + this.dx, location.y() + this.dy, location.z() + this.dz, block);
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(int x, int y, int z, T block) throws WorldEditException {
      return this.getExtent().setBlock(x + this.dx, y + this.dy, z + this.dz, block);
   }

   @Override
   public boolean setBiome(BlockVector3 position, BiomeType biome) {
      return super.setBiome(position.add(this.dx, this.dy, this.dz), biome);
   }

   @Override
   public boolean setBiome(int x, int y, int z, BiomeType biome) {
      return super.setBiome(x + this.dx, y + this.dy, z + this.dz, biome);
   }

   @Override
   public BiomeType getBiome(BlockVector3 position) {
      return super.getBiome(position.add(this.dx, this.dy, this.dz));
   }

   @Override
   public BlockState getBlock(BlockVector3 location) {
      return this.getBlock(location.x(), location.y(), location.z());
   }

   @Override
   public BlockState getBlock(int x, int y, int z) {
      return super.getBlock(x + this.dx, y + this.dy, z + this.dz);
   }

   @Override
   public BaseBlock getFullBlock(BlockVector3 pos) {
      return super.getFullBlock(pos.add(this.dx, this.dy, this.dz));
   }
}
