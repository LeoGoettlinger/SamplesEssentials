package com.fastasyncworldedit.core.function.pattern;

import com.fastasyncworldedit.core.util.TextureHolder;
import com.fastasyncworldedit.core.util.TextureUtil;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.pattern.AbstractPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

public class DesaturatePattern extends AbstractPattern {
   private final transient TextureUtil util;
   private final transient Extent extent;
   private final double value;

   public DesaturatePattern(Extent extent, TextureHolder holder, double value) {
      this.extent = extent;
      this.util = holder.getTextureUtil();
      this.value = Math.clamp(value, 0.0, 1.0);
   }

   private DesaturatePattern(Extent extent, double value, TextureUtil util) {
      this.extent = extent;
      this.util = util;
      this.value = Math.clamp(value, 0.0, 1.0);
   }

   @Override
   public BaseBlock applyBlock(BlockVector3 position) {
      BlockType type = this.extent.getBlock(position).getBlockType();
      int color;
      if (type == BlockTypes.GRASS_BLOCK) {
         color = this.util.getColor(this.extent.getBiome(position));
      } else {
         color = this.util.getColor(type);
      }

      return this.util.getNearestBlock(color).getDefaultState().toBaseBlock();
   }

   private int getColor(int color) {
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color >> 0 & 0xFF;
      int alpha = color >> 24 & 0xFF;
      double l = 0.3F * r + 0.6F * g + 0.1F * b;
      int red = (int)(r + this.value * (l - r));
      int green = (int)(g + this.value * (l - g));
      int blue = (int)(b + this.value * (l - b));
      return (alpha << 24) + (red << 16) + (green << 8) + (blue << 0);
   }

   @Override
   public boolean apply(Extent extent, BlockVector3 get, BlockVector3 set) throws WorldEditException {
      BlockType type = get.getBlock(extent).getBlockType();
      int color;
      if (type == BlockTypes.GRASS_BLOCK) {
         color = this.util.getColor(extent.getBiome(get));
      } else {
         color = this.util.getColor(type);
      }

      int newColor = this.getColor(color);
      if (newColor == color) {
         return false;
      } else {
         BlockType newType = this.util.getNextNearestBlock(newColor);
         return type.equals(newType) ? false : set.setBlock(extent, newType.getDefaultState());
      }
   }

   @Override
   public Pattern fork() {
      return new DesaturatePattern(this.extent, this.value, this.util);
   }
}
