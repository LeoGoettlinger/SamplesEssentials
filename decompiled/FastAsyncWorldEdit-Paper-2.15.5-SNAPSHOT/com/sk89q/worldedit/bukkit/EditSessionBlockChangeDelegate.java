package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import org.bukkit.BlockChangeDelegate;
import org.bukkit.block.data.BlockData;

public class EditSessionBlockChangeDelegate implements BlockChangeDelegate {
   private final EditSession editSession;

   public EditSessionBlockChangeDelegate(EditSession editSession) {
      this.editSession = editSession;
   }

   public boolean setBlockData(int x, int y, int z, BlockData blockData) {
      try {
         this.editSession.setBlock(x, y, z, BukkitAdapter.adapt(blockData));
         return true;
      } catch (MaxChangedBlocksException var6) {
         return false;
      }
   }

   public BlockData getBlockData(int x, int y, int z) {
      return BukkitAdapter.adapt(this.editSession.getBlock(x, y, z));
   }

   public int getHeight() {
      return this.editSession.getWorld().getMaxY() + 1;
   }

   public boolean isEmpty(int x, int y, int z) {
      return this.editSession.getBlock(x, y, z).getBlockType().getMaterial().isAir();
   }
}
