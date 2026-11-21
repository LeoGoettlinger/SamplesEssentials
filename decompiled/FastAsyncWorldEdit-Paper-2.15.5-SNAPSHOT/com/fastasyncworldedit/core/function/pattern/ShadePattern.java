package com.fastasyncworldedit.core.function.pattern;

import com.fastasyncworldedit.core.util.TextureHolder;
import com.fastasyncworldedit.core.util.TextureUtil;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.pattern.AbstractPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

public class ShadePattern extends AbstractPattern {
   private final transient TextureUtil util;
   private final transient Extent extent;
   private final boolean darken;

   public ShadePattern(Extent extent, TextureHolder holder, boolean darken) {
      Preconditions.checkNotNull(extent);
      this.extent = extent;
      this.util = holder.getTextureUtil();
      this.darken = darken;
   }

   private ShadePattern(Extent extent, boolean darken, TextureUtil util) {
      Preconditions.checkNotNull(extent);
      this.extent = extent;
      this.util = util;
      this.darken = darken;
   }

   @Override
   public BaseBlock applyBlock(BlockVector3 position) {
      BlockType block = this.extent.getBlock(position).getBlockType();
      BlockType type;
      if (block == BlockTypes.GRASS_BLOCK) {
         int color = this.util.getColor(this.extent.getBiome(position));
         type = this.darken ? this.util.getDarkerBlock(color) : this.util.getLighterBlock(color);
      } else {
         type = this.darken ? this.util.getDarkerBlock(block) : this.util.getLighterBlock(block);
      }

      return type.getDefaultState().toBaseBlock();
   }

   @Override
   public boolean apply(Extent extent, BlockVector3 get, BlockVector3 set) throws WorldEditException {
      BlockType type = get.getBlock(extent).getBlockType();
      BlockType newType;
      if (type == BlockTypes.GRASS_BLOCK) {
         int color = this.util.getColor(extent.getBiome(get));
         newType = this.darken ? this.util.getDarkerBlock(color) : this.util.getLighterBlock(color);
      } else {
         newType = this.darken ? this.util.getDarkerBlock(type) : this.util.getLighterBlock(type);
      }

      return type != newType ? set.setBlock(extent, newType.getDefaultState()) : false;
   }

   @Override
   public Pattern fork() {
      return new ShadePattern(this.extent, this.darken, this.util.fork());
   }
}
