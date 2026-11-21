package com.github.jikoo.openinv.internal.spigot26_2.container;

import com.github.jikoo.openinv.internal.spigot26_2.container.bukkit.OpenPlayerInventory;
import com.github.jikoo.openinv.internal.spigot26_2.container.menu.OpenChestMenu;
import com.github.jikoo.openinv.internal.spigot26_2.container.menu.OpenInventoryMenu;
import com.github.jikoo.openinv.internal.spigot26_2.container.slot.Content;
import com.github.jikoo.openinv.internal.spigot26_2.container.slot.ContentCrafting;
import com.github.jikoo.openinv.internal.spigot26_2.container.slot.ContentCraftingResult;
import com.github.jikoo.openinv.internal.spigot26_2.container.slot.ContentCursor;
import com.github.jikoo.openinv.internal.spigot26_2.container.slot.ContentDrop;
import com.github.jikoo.openinv.internal.spigot26_2.container.slot.ContentEquipment;
import com.github.jikoo.openinv.internal.spigot26_2.container.slot.ContentList;
import com.github.jikoo.openinv.internal.spigot26_2.container.slot.ContentOffHand;
import com.github.jikoo.openinv.internal.spigot26_2.container.slot.ContentViewOnly;
import com.github.jikoo.openinv.internal.spigot26_2.container.slot.SlotViewOnly;
import com.github.jikoo.openinv.internal.spigot26_2.container.slot.placeholder.Placeholders;
import com.github.jikoo.openinv.internal.spigot26_2.player.PlayerManager;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.internal.InternalOwned;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.FontDescription.Resource;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenInventory implements Container, InternalOwned<ServerPlayer>, ISpecialPlayerInventory {
   protected final List<Content> slots;
   private final int size;
   protected ServerPlayer owner;
   private int maxStackSize = 99;
   protected CraftInventory bukkitEntity;
   public List<HumanEntity> transaction = new ArrayList<>();

   public OpenInventory(@NotNull Player bukkitPlayer) {
      this.owner = PlayerManager.getHandle(bukkitPlayer);
      int rawSize = this.owner.getInventory().getContainerSize() + this.owner.inventoryMenu.getCraftSlots().getContainerSize() + 1;
      this.size = (int)Math.ceil(rawSize / 9.0) * 9;
      this.slots = NonNullList.withSize(this.size, new ContentViewOnly(this.owner));
      this.setupSlots();
   }

   protected void setupSlots() {
      int nextIndex = this.addMainInventory();
      Inventory ownerInv = this.owner.getInventory();
      if (ownerInv.getNonEquipmentItems().size() != 36
         || this.owner.inventoryMenu.getCraftSlots().getContainerSize() != 4
         || Inventory.EQUIPMENT_SLOT_MAPPING.size() != 5 && Inventory.EQUIPMENT_SLOT_MAPPING.size() != 7) {
         nextIndex = this.addArmor(nextIndex);
         nextIndex = this.addOffHand(nextIndex);
         nextIndex = this.addCrafting(nextIndex, false);
         this.slots.set(nextIndex, new ContentCursor(this.owner));
         this.slots.set(this.slots.size() - 1, new ContentDrop(this.owner));
      } else {
         this.addArmor(36);
         this.addOffHand(46);
         this.slots.set(53, new ContentDrop(this.owner));
         this.slots.set(44, new ContentCursor(this.owner));
         this.addCrafting(41, true);
      }
   }

   private int addMainInventory() {
      int listSize = this.owner.getInventory().getNonEquipmentItems().size();
      int hotbarDiff = listSize - 9;

      for (int localIndex = 0; localIndex < listSize; localIndex++) {
         SlotType type;
         int invIndex;
         if (localIndex < hotbarDiff) {
            invIndex = localIndex + 9;
            type = SlotType.CONTAINER;
         } else {
            type = SlotType.QUICKBAR;
            invIndex = localIndex - hotbarDiff;
         }

         this.slots.set(localIndex, new ContentList(this.owner, invIndex, type) {
            @Override
            public void setHolder(@NotNull ServerPlayer holder) {
               this.items = holder.getInventory().getNonEquipmentItems();
            }
         });
      }

      return listSize;
   }

   private int addArmor(int startIndex) {
      EquipmentSlot[] sorted = Inventory.EQUIPMENT_SLOT_MAPPING
         .int2ObjectEntrySet()
         .stream()
         .sorted(Comparator.comparingInt(Entry::getIntKey))
         .map(java.util.Map.Entry::getValue)
         .toArray(EquipmentSlot[]::new);
      int localIndex = 0;

      for (int i = sorted.length - 1; i >= 0; i--) {
         if (sorted[i].getType() == Type.HUMANOID_ARMOR) {
            this.slots.set(startIndex + localIndex, new ContentEquipment(this.owner, sorted[i]));
            localIndex++;
         }
      }

      return startIndex + localIndex;
   }

   private int addOffHand(int startIndex) {
      if (!Inventory.EQUIPMENT_SLOT_MAPPING.containsValue(EquipmentSlot.OFFHAND)) {
         return startIndex;
      } else {
         this.slots.set(startIndex, new ContentOffHand(this.owner));
         return startIndex + 1;
      }
   }

   private int addCrafting(int startIndex, boolean pretty) {
      int listSize = this.owner.inventoryMenu.getCraftSlots().getContents().size();
      pretty &= listSize == 4;

      for (int localIndex = 0; localIndex < listSize; localIndex++) {
         int modIndex = startIndex + (localIndex >= 2 && pretty ? localIndex + 7 : localIndex);
         this.slots.set(modIndex, new ContentCrafting(this.owner, localIndex));
      }

      if (pretty) {
         this.slots.set(startIndex + 2, new ContentViewOnly(this.owner) {
            @Override
            public Slot asSlot(Container container, int slot, int x, int y) {
               return new SlotViewOnly(container, slot, x, y) {
                  @Override
                  public ItemStack getOrDefault() {
                     return Placeholders.craftingOutput;
                  }
               };
            }
         });
         this.slots.set(startIndex + 11, this.getCraftingResult(this.owner));
      }

      return startIndex + listSize;
   }

   protected Content getCraftingResult(@NotNull ServerPlayer serverPlayer) {
      return new ContentCraftingResult(serverPlayer);
   }

   public Slot getMenuSlot(int index, int x, int y) {
      return this.slots.get(index).asSlot(this, index, x, y);
   }

   public SlotType getSlotType(int index) {
      return this.slots.get(index).getSlotType();
   }

   @NotNull
   public Component getTitle(@Nullable ServerPlayer viewer, @Nullable OpenChestMenu<?> menu) {
      MutableComponent component = Component.empty();
      if (this.owner.equals(viewer)) {
         component.append(
            Component.translatableWithFallback("openinv.container.inventory.self", "")
               .withStyle(style -> style.withFont(new Resource(Identifier.parse("openinv:font/inventory"))).withColor(ChatFormatting.WHITE))
         );
      } else {
         component.append(
            Component.translatableWithFallback("openinv.container.inventory.other", "")
               .withStyle(style -> style.withFont(new Resource(Identifier.parse("openinv:font/inventory"))).withColor(ChatFormatting.WHITE))
         );
      }

      if (menu != null && menu.isViewOnly()) {
         component.append(Component.translatableWithFallback("openinv.container.inventory.viewonly", "[RO] "));
      } else {
         component.append(Component.translatableWithFallback("openinv.container.inventory.editable", ""));
      }

      component.append(Component.translatableWithFallback("openinv.container.inventory.prefix", "", new Object[]{this.owner.getName()}))
         .append(Component.translatable("container.inventory"))
         .append(Component.translatableWithFallback("openinv.container.inventory.suffix", " - %s", new Object[]{this.owner.getName()}));
      return component;
   }

   public ServerPlayer getOwnerHandle() {
      return this.owner;
   }

   @NotNull
   @Override
   public org.bukkit.inventory.Inventory getBukkitInventory() {
      if (this.bukkitEntity == null) {
         this.bukkitEntity = new OpenPlayerInventory(this);
      }

      return this.bukkitEntity;
   }

   @Override
   public void setPlayerOnline(@NotNull Player player) {
      ServerPlayer newOwner = PlayerManager.getHandle(player);
      newOwner.getInventory().replaceWith(this.owner.getInventory());
      this.owner = newOwner;
      this.slots.forEach(slot -> slot.setHolder(newOwner));
   }

   @Override
   public boolean isInUse() {
      return !this.transaction.isEmpty();
   }

   @NotNull
   public Player getPlayer() {
      return this.getOwner();
   }

   public int getContainerSize() {
      return this.size;
   }

   public boolean isEmpty() {
      return this.slots.stream().map(Content::get).allMatch(ItemStack::isEmpty);
   }

   @NotNull
   public ItemStack getItem(int index) {
      return this.slots.get(index).get();
   }

   @NotNull
   public ItemStack removeItem(int index, int amount) {
      return this.slots.get(index).removePartial(amount);
   }

   @NotNull
   public ItemStack removeItemNoUpdate(int index) {
      return this.slots.get(index).remove();
   }

   public void setItem(int index, @NotNull ItemStack itemStack) {
      this.slots.get(index).set(itemStack);
   }

   public int getMaxStackSize() {
      return this.maxStackSize;
   }

   public void setMaxStackSize(int maxStackSize) {
      this.maxStackSize = maxStackSize;
   }

   public void setChanged() {
   }

   public boolean stillValid(@NotNull net.minecraft.world.entity.player.Player player) {
      return true;
   }

   @NotNull
   public List<ItemStack> getContents() {
      NonNullList<ItemStack> contents = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);

      for (int i = 0; i < this.getContainerSize(); i++) {
         contents.set(i, this.getItem(i));
      }

      return contents;
   }

   public void onOpen(@NotNull CraftHumanEntity viewer) {
      this.transaction.add(viewer);
   }

   public void onClose(@NotNull CraftHumanEntity viewer) {
      this.transaction.remove(viewer);
   }

   @NotNull
   public List<HumanEntity> getViewers() {
      return this.transaction;
   }

   @NotNull
   public Player getOwner() {
      return this.owner.getBukkitEntity();
   }

   @NotNull
   public Location getLocation() {
      return this.owner.getBukkitEntity().getLocation();
   }

   public void clearContent() {
      this.owner.getInventory().clearContent();
      this.owner.inventoryMenu.getCraftSlots().clearContent();
      this.owner.inventoryMenu.slotsChanged(this.owner.inventoryMenu.getCraftSlots());
      this.owner.containerMenu.setCarried(ItemStack.EMPTY);
   }

   @Nullable
   public OpenChestMenu<?> createMenu(net.minecraft.world.entity.player.Player player, int i, boolean viewOnly) {
      return player instanceof ServerPlayer serverPlayer ? new OpenInventoryMenu(this, serverPlayer, i, viewOnly) : null;
   }
}
