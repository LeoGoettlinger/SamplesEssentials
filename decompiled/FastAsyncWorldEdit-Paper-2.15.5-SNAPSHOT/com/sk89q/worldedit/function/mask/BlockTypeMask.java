package com.sk89q.worldedit.function.mask;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockTypeMask extends AbstractExtentMask {
   private final boolean[] types;
   private boolean hasAir;

   public BlockTypeMask(Extent extent, @Nonnull Collection<BlockType> blocks) {
      this(extent, blocks.toArray(new BlockType[0]));
   }

   public BlockTypeMask(Extent extent, @Nonnull BlockType... block) {
      super(extent);
      this.types = new boolean[BlockTypes.size()];

      for (BlockType type : block) {
         this.add(type);
      }
   }

   private BlockTypeMask(Extent extent, boolean[] types, boolean hasAir) {
      super(extent);
      this.types = types;
      this.hasAir = hasAir;
   }

   public void add(@Nonnull Collection<BlockType> blocks) {
      Preconditions.checkNotNull(blocks);

      for (BlockType type : blocks) {
         this.add(type);
      }
   }

   public void add(@Nonnull BlockType... block) {
      for (BlockType type : block) {
         if (!this.hasAir && (type == BlockTypes.AIR || type == BlockTypes.CAVE_AIR || type == BlockTypes.VOID_AIR)) {
            this.hasAir = true;
         }

         this.types[type.getInternalId()] = true;
      }
   }

   public Collection<BlockType> getBlocks() {
      Set<BlockType> blocks = new HashSet<>();

      for (int i = 0; i < this.types.length; i++) {
         if (this.types[i]) {
            blocks.add(BlockTypes.get(i));
         }
      }

      return blocks;
   }

   @Override
   public boolean test(BlockVector3 vector) {
      return this.test(vector.getBlock(this.getExtent()).getBlockType());
   }

   @Override
   public boolean test(Extent extent, BlockVector3 vector) {
      return this.test(extent.getBlock(vector).getBlockType());
   }

   @Override
   public boolean replacesAir() {
      return this.hasAir;
   }

   public boolean test(BlockType block) {
      return this.types[block.getInternalId()];
   }

   public <B extends BlockStateHolder<B>> boolean test(B blockStateHolder) {
      return this.types[blockStateHolder.getBlockType().getInternalId()];
   }

   @Nullable
   @Override
   public Mask2D toMask2D() {
      return null;
   }

   public BlockTypeMask copy() {
      return new BlockTypeMask(this.getExtent(), (boolean[])this.types.clone(), this.hasAir);
   }
}
