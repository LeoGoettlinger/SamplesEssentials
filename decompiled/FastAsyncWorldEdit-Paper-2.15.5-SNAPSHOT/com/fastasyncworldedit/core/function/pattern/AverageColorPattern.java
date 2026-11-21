package com.fastasyncworldedit.core.function.pattern;

import com.fastasyncworldedit.core.util.TextureHolder;
import com.fastasyncworldedit.core.util.TextureUtil;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.awt.Color;

public class AverageColorPattern extends AbstractExtentPattern {
   private final transient TextureUtil util;
   private final int color;

   public AverageColorPattern(Extent extent, TextureHolder holder, int r, int g, int b, int a) {
      super(extent);
      this.util = holder.getTextureUtil();
      this.color = new Color(Math.clamp((long)r, 0, 255), Math.clamp((long)g, 0, 255), Math.clamp((long)b, 0, 255), Math.clamp((long)a, 0, 255)).getRGB();
   }

   private AverageColorPattern(Extent extent, TextureUtil util, int color) {
      super(extent);
      this.util = util;
      this.color = color;
   }

   @Override
   public BaseBlock applyBlock(BlockVector3 position) {
      BaseBlock block = this.getExtent().getFullBlock(position);
      BlockType type = block.getBlockType();
      int currentColor;
      if (type == BlockTypes.GRASS_BLOCK) {
         currentColor = this.util.getColor(this.getExtent().getBiome(position));
      } else {
         currentColor = this.util.getColor(type);
      }

      int newColor = TextureUtil.averageColor(currentColor, this.color);
      return this.util.getNearestBlock(newColor).getDefaultState().toBaseBlock();
   }

   @Override
   public boolean apply(Extent extent, BlockVector3 get, BlockVector3 set) throws WorldEditException {
      BlockType blockType = get.getBlock(extent).getBlockType();
      int currentColor;
      if (blockType == BlockTypes.GRASS_BLOCK) {
         currentColor = this.util.getColor(extent.getBiome(get));
      } else {
         currentColor = this.util.getColor(blockType);
      }

      if (currentColor == 0) {
         return false;
      } else {
         int newColor = TextureUtil.averageColor(currentColor, this.color);
         BlockType newBlock = this.util.getNearestBlock(newColor);
         return newBlock == blockType ? false : set.setBlock(extent, newBlock.getDefaultState());
      }
   }

   @Override
   public Pattern fork() {
      return new AverageColorPattern(this.getExtent(), this.util, this.color);
   }
}
