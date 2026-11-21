package com.github.jikoo.openinv.internal.spigot26_2.container;

import com.github.jikoo.openinv.internal.spigot26_2.container.menu.OpenChestMenu;
import com.github.jikoo.openinv.internal.spigot26_2.container.menu.OpenEnderChestMenu;
import com.github.jikoo.openinv.internal.spigot26_2.player.PlayerManager;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.InternalOwned;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenEnderChest implements Container, StackedContentsCompatible, InternalOwned<ServerPlayer>, ISpecialEnderChest {
   private CraftInventory inventory;
   @NotNull
   private ServerPlayer owner;
   private NonNullList<ItemStack> items;
   private int maxStack = 64;
   private final List<HumanEntity> transaction = new ArrayList<>();

   public OpenEnderChest(@NotNull Player player) {
      this.owner = PlayerManager.getHandle(player);
      this.items = this.owner.getEnderChestInventory().items;
   }

   @NotNull
   public ServerPlayer getOwnerHandle() {
      return this.owner;
   }

   @NotNull
   @Override
   public Inventory getBukkitInventory() {
      if (this.inventory == null) {
         this.inventory = new CraftInventory(this) {
            @NotNull
            public InventoryType getType() {
               return InventoryType.ENDER_CHEST;
            }
         };
      }

      return this.inventory;
   }

   @Override
   public void setPlayerOnline(@NotNull Player player) {
      this.owner = PlayerManager.getHandle(player);
      NonNullList<ItemStack> activeItems = this.owner.getEnderChestInventory().items;
      int max = Math.min(this.items.size(), activeItems.size());

      for (int index = 0; index < max; index++) {
         activeItems.set(index, (ItemStack)this.items.get(index));
      }

      this.items = activeItems;
   }

   @NotNull
   public Player getPlayer() {
      return this.owner.getBukkitEntity();
   }

   public int getContainerSize() {
      return this.items.size();
   }

   public boolean isEmpty() {
      return this.items.stream().allMatch(ItemStack::isEmpty);
   }

   @NotNull
   public ItemStack getItem(int index) {
      return index >= 0 && index < this.items.size() ? (ItemStack)this.items.get(index) : ItemStack.EMPTY;
   }

   @NotNull
   public ItemStack removeItem(int index, int amount) {
      ItemStack itemstack = ContainerHelper.removeItem(this.items, index, amount);
      if (!itemstack.isEmpty()) {
         this.setChanged();
      }

      return itemstack;
   }

   @NotNull
   public ItemStack removeItemNoUpdate(int index) {
      return index >= 0 && index < this.items.size() ? (ItemStack)this.items.set(index, ItemStack.EMPTY) : ItemStack.EMPTY;
   }

   public void setItem(int index, @NotNull ItemStack itemStack) {
      if (index >= 0 && index < this.items.size()) {
         this.items.set(index, itemStack);
      }
   }

   public int getMaxStackSize() {
      return this.maxStack;
   }

   public void setChanged() {
      this.owner.getEnderChestInventory().setChanged();
   }

   public boolean stillValid(@NotNull net.minecraft.world.entity.player.Player player) {
      return true;
   }

   @NotNull
   public List<ItemStack> getContents() {
      return this.items;
   }

   public void onOpen(@NotNull CraftHumanEntity craftHumanEntity) {
      this.transaction.add(craftHumanEntity);
   }

   public void onClose(@NotNull CraftHumanEntity craftHumanEntity) {
      this.transaction.remove(craftHumanEntity);
   }

   @NotNull
   public List<HumanEntity> getViewers() {
      return this.transaction;
   }

   @NotNull
   public Player getOwner() {
      return this.getPlayer();
   }

   public void setMaxStackSize(int size) {
      this.maxStack = size;
   }

   @Nullable
   public Location getLocation() {
      return null;
   }

   public void clearContent() {
      this.items.clear();
      this.setChanged();
   }

   public void fillStackedContents(@NotNull StackedItemContents stackedContents) {
      for (ItemStack itemstack : this.items) {
         stackedContents.accountStack(itemstack);
      }
   }

   public Component getTitle(@Nullable OpenChestMenu<?> menu) {
      MutableComponent component;
      if (menu != null && menu.isViewOnly()) {
         component = Component.translatableWithFallback("openinv.container.enderchest.viewonly", "[RO] ");
      } else {
         component = Component.translatableWithFallback("openinv.container.enderchest.editable", "");
      }

      return component.append(Component.translatableWithFallback("openinv.container.enderchest.prefix", "", new Object[]{this.owner.getName()}))
         .append(Component.translatable("container.enderchest"))
         .append(Component.translatableWithFallback("openinv.container.enderchest.suffix", " - %s", new Object[]{this.owner.getName()}));
   }

   @Nullable
   public OpenChestMenu<?> createMenu(net.minecraft.world.entity.player.Player player, int i, boolean viewOnly) {
      return player instanceof ServerPlayer serverPlayer ? new OpenEnderChestMenu(this, serverPlayer, i, viewOnly) : null;
   }
}
