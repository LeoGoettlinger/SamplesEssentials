package com.sk89q.worldedit.extent.inventory;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.block.BlockState;

public abstract class BlockBag {
   public void storeDroppedBlock(BlockState blockState) throws BlockBagException {
      if (blockState != null) {
         if (!blockState.getBlockType().getMaterial().isAir()) {
            this.storeBlock(blockState);
         }
      }
   }

   public void fetchPlacedBlock(BlockState blockState) throws BlockBagException {
      try {
         if (blockState.getBlockType().getMaterial().isReplacedDuringPlacement()) {
            return;
         }

         this.fetchBlock(blockState);
      } catch (OutOfBlocksException var4) {
         if (blockState.getBlockType().getMaterial().isAir()) {
            throw var4;
         }

         this.fetchBlock(blockState);
      }
   }

   public abstract void fetchBlock(BlockState blockState) throws BlockBagException;

   public void storeBlock(BlockState blockState) throws BlockBagException {
      this.storeBlock(blockState, 1);
   }

   public abstract void storeBlock(BlockState blockState, int amount) throws BlockBagException;

   public boolean peekBlock(BlockState blockState) {
      try {
         this.fetchBlock(blockState);
         this.storeBlock(blockState);
         return true;
      } catch (BlockBagException var3) {
         return false;
      }
   }

   public abstract void flushChanges();

   public abstract void addSourcePosition(Location pos);

   public abstract void addSingleSourcePosition(Location pos);
}
