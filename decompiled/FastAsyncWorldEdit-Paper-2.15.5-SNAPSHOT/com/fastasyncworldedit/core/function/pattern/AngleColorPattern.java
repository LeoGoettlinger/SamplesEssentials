package com.fastasyncworldedit.core.function.pattern;

import com.fastasyncworldedit.core.util.TextureHolder;
import com.fastasyncworldedit.core.util.TextureUtil;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

public class AngleColorPattern extends AnglePattern {
   protected transient TextureUtil util;

   public AngleColorPattern(Extent extent, TextureHolder holder, int distance) {
      super(extent, distance);
      this.util = holder.getTextureUtil();
   }

   private AngleColorPattern(Extent extent, int distance, TextureUtil util) {
      super(extent, distance);
      this.util = util;
   }

   private int getColor(int color, int slope) {
      if (slope == 0) {
         return color;
      } else {
         double newFactor = 1.0 - Math.min(1.0, slope * this.factor);
         int newRed = (int)((color >> 16 & 0xFF) * newFactor);
         int newGreen = (int)((color >> 8 & 0xFF) * newFactor);
         int newBlue = (int)((color >> 0 & 0xFF) * newFactor);
         return ((color >> 24 & 0xFF) << 24) + (newRed << 16) + (newGreen << 8) + (newBlue << 0);
      }
   }

   @Override
   public <T extends BlockStateHolder<T>> int getSlope(T block, BlockVector3 vector, Extent extent) {
      int slope = super.getSlope(block, vector, extent);
      if (slope != -1) {
         int x = vector.x();
         int y = vector.y();
         int z = vector.z();
         int height = extent.getNearestSurfaceTerrainBlock(x, z, y, this.minY, this.maxY);
         if (height > this.minY) {
            BlockState below = extent.getBlock(x, height - 1, z);
            if (!below.getBlockType().getMaterial().isMovementBlocker()) {
               return Integer.MAX_VALUE;
            }
         }
      }

      return slope;
   }

   @Override
   public BaseBlock applyBlock(BlockVector3 position) {
      BaseBlock block = this.extent.getFullBlock(position);
      int slope = this.getSlope(block, position, this.extent);
      if (slope == -1) {
         return block;
      } else {
         BlockType type = block.getBlockType();
         int color;
         if (type == BlockTypes.GRASS_BLOCK) {
            color = this.util.getColor(this.extent.getBiome(position));
         } else {
            color = this.util.getColor(type);
         }

         if (color == 0) {
            return block;
         } else {
            int newColor = this.getColor(color, slope);
            return this.util.getNearestBlock(newColor).getDefaultState().toBaseBlock();
         }
      }
   }

   @Override
   public boolean apply(Extent extent, BlockVector3 get, BlockVector3 set) throws WorldEditException {
      BlockState block = get.getBlock(extent);
      int slope = this.getSlope(block, get, extent);
      if (slope == -1) {
         return false;
      } else {
         BlockType type = block.getBlockType();
         int color;
         if (type == BlockTypes.GRASS_BLOCK) {
            color = this.util.getColor(extent.getBiome(get));
         } else {
            color = this.util.getColor(type);
         }

         if (color == 0) {
            return false;
         } else {
            int newColor = this.getColor(color, slope);
            BlockType newBlock = this.util.getNearestBlock(newColor);
            return newBlock == null ? false : set.setBlock(extent, newBlock.getDefaultState());
         }
      }
   }

   @Override
   public Pattern fork() {
      return new AngleColorPattern(this.extent, this.distance, this.util.fork());
   }
}
