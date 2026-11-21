package com.fastasyncworldedit.core.history.changeset;

import com.fastasyncworldedit.core.FaweCache;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.extent.inventory.BlockBagException;
import com.sk89q.worldedit.extent.inventory.UnplaceableBlockException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

public class BlockBagChangeSet extends AbstractDelegateChangeSet {
   private final boolean mine;
   private final int[] missingBlocks = new int[BlockTypes.size()];
   private BlockBag blockBag;

   public BlockBagChangeSet(AbstractChangeSet parent, BlockBag blockBag, boolean mine) {
      super(parent);
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
   public void add(BlockVector3 loc, BaseBlock from, BaseBlock to) {
      int x = loc.x();
      int y = loc.y();
      int z = loc.z();
      this.add(x, y, z, from, to);
   }

   @Override
   public void add(int x, int y, int z, BaseBlock from, BaseBlock to) {
      this.check(from.getBlockType(), to.getBlockType());
      super.add(x, y, z, from, to);
   }

   public void check(BlockType typeFrom, BlockType typeTo) {
      if (!typeTo.getMaterial().isAir()) {
         try {
            this.blockBag.fetchPlacedBlock(typeTo.getDefaultState());
         } catch (UnplaceableBlockException var5) {
            throw FaweCache.BLOCK_BAG;
         } catch (BlockBagException var6) {
            this.missingBlocks[typeTo.getInternalId()]++;
            throw FaweCache.BLOCK_BAG;
         }
      }

      if (this.mine && !typeFrom.getMaterial().isAir()) {
         try {
            this.blockBag.storeDroppedBlock(typeFrom.getDefaultState());
         } catch (BlockBagException var4) {
         }
      }
   }

   @Override
   public void add(int x, int y, int z, int combinedFrom, int combinedTo) {
      BlockType typeFrom = BlockTypes.getFromStateId(combinedFrom);
      BlockType typeTo = BlockTypes.getFromStateId(combinedTo);
      this.check(typeFrom, typeTo);
      super.add(x, y, z, combinedFrom, combinedTo);
   }
}
