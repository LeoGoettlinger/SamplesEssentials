package com.sk89q.worldedit.function.mask;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockCategory;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import javax.annotation.Nullable;

public class BlockCategoryMask extends AbstractExtentMask {
   private final BlockCategory category;

   public BlockCategoryMask(Extent extent, BlockCategory category) {
      super(extent);
      Preconditions.checkNotNull(category);
      this.category = category;
      this.category.getAll();
   }

   @Override
   public boolean test(BlockVector3 vector) {
      return this.category.contains(vector.getBlock(this.getExtent()));
   }

   @Override
   public boolean test(Extent extent, BlockVector3 vector) {
      return this.category.contains(vector.getBlock(extent));
   }

   public <B extends BlockStateHolder<B>> boolean test(B blockStateHolder) {
      return this.category.contains(blockStateHolder);
   }

   @Nullable
   @Override
   public Mask2D toMask2D() {
      return null;
   }

   @Override
   public Mask copy() {
      return new BlockCategoryMask(this.getExtent(), this.category);
   }

   @Override
   public boolean replacesAir() {
      return this.category.contains(BlockTypes.AIR);
   }
}
