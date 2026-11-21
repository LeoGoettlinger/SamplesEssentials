package com.github.jikoo.openinv.internal.spigot26_2.container.menu;

import com.github.jikoo.openinv.internal.spigot26_2.container.slot.SlotPlaceholder;
import com.google.common.base.Suppliers;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.InternalOwned;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.HashedStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RemoteSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class OpenSyncMenu<T extends Container & ISpecialInventory & InternalOwned<ServerPlayer>> extends OpenChestMenu<T> {
   @Nullable
   protected ContainerSynchronizer synchronizer;
   protected final List<DataSlot> dataSlots = new ArrayList<>();
   protected final IntList remoteDataSlots = new IntArrayList();
   protected final List<ContainerListener> containerListeners = new ArrayList<>();
   private RemoteSlot remoteCarried = RemoteSlot.PLACEHOLDER;
   protected boolean suppressRemoteUpdates;

   protected OpenSyncMenu(@NotNull MenuType<ChestMenu> type, int containerCounter, @NotNull T container, @NotNull ServerPlayer viewer, boolean viewOnly) {
      super(type, containerCounter, container, viewer, viewOnly);
   }

   @NotNull
   protected Slot addSlot(@NotNull Slot slot) {
      slot.index = this.slots.size();
      this.slots.add(slot);
      this.lastSlots.add(ItemStack.EMPTY);
      this.remoteSlots.add(this.synchronizer != null ? this.synchronizer.createSlot() : RemoteSlot.PLACEHOLDER);
      return slot;
   }

   @NotNull
   protected DataSlot addDataSlot(@NotNull DataSlot dataSlot) {
      this.dataSlots.add(dataSlot);
      this.remoteDataSlots.add(0);
      return dataSlot;
   }

   protected void addDataSlots(ContainerData containerData) {
      for (int i = 0; i < containerData.getCount(); i++) {
         this.addDataSlot(DataSlot.forContainer(containerData, i));
      }
   }

   public void addSlotListener(@NotNull ContainerListener containerListener) {
      if (!this.containerListeners.contains(containerListener)) {
         this.containerListeners.add(containerListener);
         this.broadcastChanges();
      }
   }

   public void setSynchronizer(@NotNull ContainerSynchronizer containerSynchronizer) {
      this.synchronizer = containerSynchronizer;
      this.remoteCarried = this.synchronizer.createSlot();
      this.remoteSlots.replaceAll(slot -> this.synchronizer.createSlot());
      this.sendAllDataToRemote();
   }

   public void sendAllDataToRemote() {
      List<ItemStack> contentsCopy = new ArrayList<>();

      for (int index = 0; index < this.slots.size(); index++) {
         Slot slot = (Slot)this.slots.get(index);
         ItemStack itemStack = slot instanceof SlotPlaceholder placeholder ? placeholder.getOrDefault() : slot.getItem();
         contentsCopy.add(itemStack.copy());
         ((RemoteSlot)this.remoteSlots.get(index)).force(itemStack);
      }

      this.remoteCarried.force(this.getCarried());

      for (int index = 0; index < this.dataSlots.size(); index++) {
         this.remoteDataSlots.set(index, this.dataSlots.get(index).get());
      }

      if (this.synchronizer != null) {
         this.synchronizer.sendInitialData(this, contentsCopy, this.getCarried().copy(), this.remoteDataSlots.toIntArray());
      }
   }

   public void broadcastCarriedItem() {
      ItemStack carried = this.getCarried();
      this.remoteCarried.force(carried);
      if (this.synchronizer != null) {
         this.synchronizer.sendCarriedChange(this, carried.copy());
      }
   }

   public void removeSlotListener(@NotNull ContainerListener containerListener) {
      this.containerListeners.remove(containerListener);
   }

   public void broadcastChanges() {
      for (int index = 0; index < this.slots.size(); index++) {
         Slot slot = (Slot)this.slots.get(index);
         ItemStack itemstack = slot instanceof SlotPlaceholder placeholder ? placeholder.getOrDefault() : slot.getItem();
         Supplier<ItemStack> supplier = Suppliers.memoize(itemstack::copy);
         this.triggerSlotListeners(index, itemstack, supplier);
         this.synchronizeSlotToRemote(index, itemstack, supplier);
      }

      this.synchronizeCarriedToRemote();

      for (int index = 0; index < this.dataSlots.size(); index++) {
         DataSlot dataSlot = this.dataSlots.get(index);
         int j = dataSlot.get();
         if (dataSlot.checkAndClearUpdateFlag()) {
            this.updateDataSlotListeners(index, j);
         }

         this.synchronizeDataSlotToRemote(index, j);
      }
   }

   public void broadcastFullState() {
      for (int index = 0; index < this.slots.size(); index++) {
         ItemStack itemstack = ((Slot)this.slots.get(index)).getItem();
         this.triggerSlotListeners(index, itemstack, itemstack::copy);
      }

      for (int index = 0; index < this.dataSlots.size(); index++) {
         DataSlot containerproperty = this.dataSlots.get(index);
         if (containerproperty.checkAndClearUpdateFlag()) {
            this.updateDataSlotListeners(index, containerproperty.get());
         }
      }

      this.sendAllDataToRemote();
   }

   private void updateDataSlotListeners(int i, int j) {
      for (ContainerListener containerListener : this.containerListeners) {
         containerListener.dataChanged(this, i, j);
      }
   }

   private void triggerSlotListeners(int index, @NotNull ItemStack itemStack, @NotNull Supplier<ItemStack> supplier) {
      ItemStack itemStack1 = (ItemStack)this.lastSlots.get(index);
      if (!ItemStack.matches(itemStack1, itemStack)) {
         ItemStack itemStack2 = supplier.get();
         this.lastSlots.set(index, itemStack2);

         for (ContainerListener containerListener : this.containerListeners) {
            containerListener.slotChanged(this, index, itemStack2);
         }
      }
   }

   private void synchronizeSlotToRemote(int i, @NotNull ItemStack itemStack, @NotNull Supplier<ItemStack> supplier) {
      if (!this.suppressRemoteUpdates) {
         RemoteSlot slot = (RemoteSlot)this.remoteSlots.get(i);
         if (!slot.matches(itemStack)) {
            slot.force(itemStack);
            if (this.synchronizer != null) {
               this.synchronizer.sendSlotChange(this, i, supplier.get());
            }
         }
      }
   }

   private void synchronizeDataSlotToRemote(int index, int value) {
      if (!this.suppressRemoteUpdates) {
         int existing = this.remoteDataSlots.getInt(index);
         if (existing != value) {
            this.remoteDataSlots.set(index, value);
            if (this.synchronizer != null) {
               this.synchronizer.sendDataChange(this, index, value);
            }
         }
      }
   }

   private void synchronizeCarriedToRemote() {
      if (!this.suppressRemoteUpdates) {
         ItemStack carried = this.getCarried();
         if (!this.remoteCarried.matches(carried)) {
            this.remoteCarried.force(carried);
            if (this.synchronizer != null) {
               this.synchronizer.sendCarriedChange(this, carried.copy());
            }
         }
      }
   }

   public void setRemoteCarried(@NotNull HashedStack stack) {
      this.remoteCarried.receive(stack);
   }

   public void suppressRemoteUpdates() {
      this.suppressRemoteUpdates = true;
   }

   public void resumeRemoteUpdates() {
      this.suppressRemoteUpdates = false;
   }
}
