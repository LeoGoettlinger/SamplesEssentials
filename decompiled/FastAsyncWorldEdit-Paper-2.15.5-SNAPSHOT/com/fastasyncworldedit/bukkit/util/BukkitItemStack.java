package com.fastasyncworldedit.bukkit.util;

import com.fastasyncworldedit.bukkit.FaweBukkit;
import com.fastasyncworldedit.core.Fawe;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.item.ItemType;
import javax.annotation.Nullable;
import org.bukkit.inventory.ItemStack;

public class BukkitItemStack extends BaseItemStack {
   private ItemStack stack;
   private Object nativeItem;
   private boolean loadedNBT;

   public BukkitItemStack(ItemStack stack) {
      super(BukkitAdapter.asItemType(stack.getType()));
      this.stack = stack;
   }

   public BukkitItemStack(ItemType type, ItemStack stack) {
      super(type);
      this.stack = stack;
   }

   @Override
   public int getAmount() {
      return this.stack.getAmount();
   }

   @Nullable
   @Override
   public Object getNativeItem() {
      ItemUtil util = Fawe.<FaweBukkit>platform().getItemUtil();
      return util != null && this.nativeItem == null ? (this.nativeItem = util.getNMSItem(this.stack)) : this.nativeItem;
   }

   public ItemStack getBukkitItemStack() {
      return this.stack;
   }

   @Override
   public boolean hasNbtData() {
      return !this.loadedNBT ? this.stack.hasItemMeta() : super.hasNbtData();
   }

   @Nullable
   @Override
   public CompoundTag getNbtData() {
      if (!this.loadedNBT) {
         this.loadedNBT = true;
         ItemUtil util = Fawe.<FaweBukkit>platform().getItemUtil();
         if (util != null) {
            super.setNbtData(util.getNBT(this.stack));
         }
      }

      return super.getNbtData();
   }

   @Override
   public void setNbtData(@Nullable CompoundTag nbtData) {
      ItemUtil util = Fawe.<FaweBukkit>platform().getItemUtil();
      if (util != null) {
         this.stack = util.setNBT(this.stack, nbtData);
         this.nativeItem = null;
      }

      super.setNbtData(nbtData);
   }
}
