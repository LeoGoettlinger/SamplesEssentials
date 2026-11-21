package com.sk89q.worldedit.bukkit;

import com.fastasyncworldedit.core.extent.inventory.SlottableBlockBag;
import com.fastasyncworldedit.core.util.TaskManager;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.extent.inventory.BlockBagException;
import com.sk89q.worldedit.extent.inventory.OutOfBlocksException;
import com.sk89q.worldedit.extent.inventory.OutOfSpaceException;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BukkitPlayerBlockBag extends BlockBag implements SlottableBlockBag {
   private final Player player;
   private ItemStack[] items;

   public BukkitPlayerBlockBag(Player player) {
      this.player = player;
   }

   private void loadInventory() {
      if (this.items == null) {
         this.items = this.player.getInventory().getContents();
      }
   }

   public Player getPlayer() {
      return this.player;
   }

   @Override
   public void fetchBlock(BlockState blockState) throws BlockBagException {
      if (blockState.getBlockType().getMaterial().isAir()) {
         throw new IllegalArgumentException("Can't fetch air block");
      } else {
         this.loadInventory();
         boolean found = false;

         for (int slot = 0; slot < this.items.length; slot++) {
            ItemStack bukkitItem = this.items[slot];
            if (bukkitItem != null && BukkitAdapter.equals(blockState.getBlockType(), bukkitItem.getType())) {
               int currentAmount = bukkitItem.getAmount();
               if (currentAmount < 0) {
                  return;
               }

               if (currentAmount > 1) {
                  bukkitItem.setAmount(currentAmount - 1);
                  found = true;
               } else {
                  this.items[slot] = null;
                  found = true;
               }
               break;
            }
         }

         if (!found) {
            throw new OutOfBlocksException();
         }
      }
   }

   @Override
   public void storeBlock(BlockState blockState, int amount) throws BlockBagException {
      if (blockState.getBlockType().getMaterial().isAir()) {
         throw new IllegalArgumentException("Can't store air block");
      } else if (!blockState.getBlockType().hasItemType()) {
         throw new IllegalArgumentException("This block cannot be stored");
      } else {
         this.loadInventory();
         int freeSlot = -1;

         for (int slot = 0; slot < this.items.length; slot++) {
            ItemStack bukkitItem = this.items[slot];
            if (bukkitItem == null) {
               if (freeSlot == -1) {
                  freeSlot = slot;
               }
            } else if (BukkitAdapter.equals(blockState.getBlockType(), bukkitItem.getType())) {
               int currentAmount = bukkitItem.getAmount();
               if (currentAmount < 0) {
                  return;
               }

               if (currentAmount < 64) {
                  int spaceLeft = 64 - currentAmount;
                  if (spaceLeft >= amount) {
                     bukkitItem.setAmount(currentAmount + amount);
                     return;
                  }

                  bukkitItem.setAmount(64);
                  amount -= spaceLeft;
               }
            }
         }

         if (freeSlot > -1) {
            this.items[freeSlot] = BukkitAdapter.adapt(new BaseItemStack(blockState.getBlockType().getItemType(), amount));
         } else {
            throw new OutOfSpaceException(blockState.getBlockType());
         }
      }
   }

   @Override
   public void flushChanges() {
      if (this.items != null) {
         TaskManager.taskManager().sync(() -> {
            this.player.getInventory().setContents(this.items);
            return null;
         });
         this.items = null;
      }
   }

   @Override
   public void addSourcePosition(Location pos) {
   }

   @Override
   public void addSingleSourcePosition(Location pos) {
   }

   @Override
   public BaseItem getItem(int slot) {
      this.loadInventory();
      return this.items[slot] == null ? null : BukkitAdapter.adapt(this.items[slot]);
   }

   @Override
   public void setItem(int slot, BaseItem block) {
      this.loadInventory();
      BaseItemStack stack = block instanceof BaseItemStack ? (BaseItemStack)block : new BaseItemStack(block.getType(), block.getNbtData(), 1);
      this.items[slot] = BukkitAdapter.adapt(stack);
   }
}
