package com.fastasyncworldedit.core.history.change;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.extent.inventory.BlockBagException;
import com.sk89q.worldedit.history.UndoContext;
import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.world.block.BlockState;

public class MutableFullBlockChange implements Change {
   public int z;
   public int y;
   public int x;
   public int from;
   public int to;
   public BlockBag blockBag;
   public boolean allowFetch;
   public boolean allowStore;

   public MutableFullBlockChange(BlockBag blockBag, int mode, boolean redo) {
      this.blockBag = blockBag;
      this.allowFetch = redo || mode == 1;
      this.allowStore = !redo || mode == 1;
   }

   @Override
   public void undo(UndoContext context) throws WorldEditException {
      this.create(context);
   }

   @Override
   public void redo(UndoContext context) throws WorldEditException {
      this.create(context);
   }

   public void create(UndoContext context) {
      BlockState fromState = BlockState.getFromOrdinal(this.from);
      if (this.blockBag != null) {
         BlockState toState = BlockState.getFromOrdinal(this.to);
         if (fromState != toState) {
            if (this.allowFetch && this.from != 0) {
               try {
                  this.blockBag.fetchPlacedBlock(fromState);
               } catch (BlockBagException var6) {
                  return;
               }
            }

            if (this.allowStore && this.to != 0) {
               try {
                  this.blockBag.storeDroppedBlock(toState);
               } catch (BlockBagException var5) {
               }
            }
         }
      }

      context.getExtent().setBlock(this.x, this.y, this.z, fromState);
   }
}
