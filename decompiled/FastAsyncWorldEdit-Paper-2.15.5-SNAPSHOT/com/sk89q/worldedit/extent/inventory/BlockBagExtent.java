package com.sk89q.worldedit.extent.inventory;

import com.fastasyncworldedit.core.FaweCache;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

public class BlockBagExtent extends AbstractDelegateExtent {
   private final boolean mine;
   private final int[] missingBlocks = new int[BlockTypes.size()];
   private BlockBag blockBag;

   public BlockBagExtent(Extent extent, @Nullable BlockBag blockBag) {
      this(extent, blockBag, false);
   }

   public BlockBagExtent(Extent extent, @Nullable BlockBag blockBag, boolean mine) {
      super(extent);
      this.blockBag = blockBag;
      this.mine = mine;
   }

   @Nullable
   public BlockBag getBlockBag() {
      return this.blockBag;
   }

   public void setBlockBag(@Nullable BlockBag blockBag) {
      this.blockBag = blockBag;
   }

   public Map<BlockType, Integer> popMissing() {
      HashMap<BlockType, Integer> map = new HashMap<>();

      for (int i = 0; i < this.missingBlocks.length; i++) {
         int count = this.missingBlocks[i];
         if (count > 0) {
            map.put(BlockTypes.get(i), count);
         }
      }

      Arrays.fill(this.missingBlocks, 0);
      return map;
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block) throws WorldEditException {
      return this.setBlock(position.x(), position.y(), position.z(), block);
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(int x, int y, int z, B block) throws WorldEditException {
      BlockState existing = this.getBlock(x, y, z);
      if (!block.getBlockType().equals(existing.getBlockType())) {
         if (!block.getBlockType().getMaterial().isAir()) {
            try {
               this.blockBag.fetchPlacedBlock(block.toImmutableState());
            } catch (UnplaceableBlockException var8) {
               throw FaweCache.BLOCK_BAG;
            } catch (BlockBagException var9) {
               this.missingBlocks[block.getBlockType().getInternalId()]++;
               throw FaweCache.BLOCK_BAG;
            }
         }

         if (this.mine && !existing.getBlockType().getMaterial().isAir()) {
            try {
               this.blockBag.storeDroppedBlock(existing);
            } catch (BlockBagException var7) {
            }
         }
      }

      return super.setBlock(x, y, z, block);
   }
}
