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
import java.awt.Color;

public class SaturatePattern extends AbstractPattern {
   private final transient TextureUtil util;
   private final transient Extent extent;
   private final int color;

   public SaturatePattern(Extent extent, TextureHolder holder, int r, int g, int b, int a) {
      this.extent = extent;
      this.util = holder.getTextureUtil();
      this.color = new Color(Math.clamp((long)r, 0, 255), Math.clamp((long)g, 0, 255), Math.clamp((long)b, 0, 255), Math.clamp((long)a, 0, 255)).getRGB();
   }

   private SaturatePattern(Extent extent, TextureUtil util, int color) {
      this.extent = extent;
      this.util = util;
      this.color = color;
   }

   @Override
   public BaseBlock applyBlock(BlockVector3 position) {
      BlockType type = this.extent.getBlock(position).getBlockType();
      int currentColor;
      if (type == BlockTypes.GRASS_BLOCK) {
         currentColor = this.util.getColor(this.extent.getBiome(position));
      } else {
         currentColor = this.util.getColor(type);
      }

      int newColor = TextureUtil.multiplyColor(currentColor, this.color);
      return this.util.getNearestBlock(newColor).getDefaultState().toBaseBlock();
   }

   @Override
   public boolean apply(Extent extent, BlockVector3 get, BlockVector3 set) throws WorldEditException {
      BlockType type = get.getBlock(extent).getBlockType();
      int currentColor;
      if (type == BlockTypes.GRASS_BLOCK) {
         currentColor = this.util.getColor(extent.getBiome(get));
      } else {
         currentColor = this.util.getColor(type);
      }

      if (currentColor == 0) {
         return false;
      } else {
         int newColor = TextureUtil.multiplyColor(currentColor, this.color);
         BlockType newBlock = this.util.getNearestBlock(newColor);
         return newBlock != null && !newBlock.equals(type) ? set.setBlock(extent, newBlock.getDefaultState()) : false;
      }
   }

   @Override
   public Pattern fork() {
      return new SaturatePattern(this.extent, this.util.fork(), this.color);
   }
}
