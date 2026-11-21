package com.lishid.openinv.internal.paper26_2.container.menu;

import com.google.common.base.Suppliers;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.InternalOwned;
import com.lishid.openinv.internal.paper26_2.container.slot.SlotPlaceholder;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RemoteSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public abstract class OpenSyncMenu<T extends Container & ISpecialInventory & InternalOwned<ServerPlayer>> extends OpenChestMenu<T> {
   @Nullable
   protected ContainerSynchronizer synchronizer;
   protected final List<ContainerListener> listeners = new ArrayList<>();
   @Nullable
   private RemoteSlot remoteCarried = RemoteSlot.PLACEHOLDER;
   protected boolean suppressRemoteUpdates;

   protected OpenSyncMenu(MenuType<ChestMenu> type, int containerCounter, T container, ServerPlayer viewer, boolean viewOnly) {
      super(type, containerCounter, container, viewer, viewOnly);
   }

   public void broadcastChanges() {
      for (int index = 0; index < this.slots.size(); index++) {
         Slot slot = (Slot)this.slots.get(index);
         ItemStack itemstack = slot instanceof SlotPlaceholder placeholder ? placeholder.getOrDefault() : slot.getItem();
         Supplier<ItemStack> supplier = Suppliers.memoize(itemstack::copy);
         this.triggerSlotListeners(index, itemstack, supplier);
         this.synchronizeSlotToRemote(index, itemstack, supplier);
      }

      if (!this.suppressRemoteUpdates && !this.getRemoteCarried().matches(this.getCarried())) {
         this.broadcastCarriedItem();
      }

      for (int index = 0; index < this.dataSlots.size(); index++) {
         DataSlot dataSlot = (DataSlot)this.dataSlots.get(index);
         int current = dataSlot.get();
         if (dataSlot.checkAndClearUpdateFlag()) {
            for (ContainerListener containerListener : this.listeners) {
               containerListener.dataChanged(this, index, current);
            }
         }

         if (!this.suppressRemoteUpdates && this.remoteDataSlots.getInt(index) != current) {
            this.remoteDataSlots.set(index, current);
            if (this.synchronizer != null) {
               this.synchronizer.sendDataChange(this, index, current);
            }
         }
      }
   }

   public void sendAllDataToRemote() {
      List<ItemStack> contentsCopy = new ArrayList<>();

      for (int index = 0; index < this.slots.size(); index++) {
         Slot slot = (Slot)this.slots.get(index);
         ItemStack itemStack = slot instanceof SlotPlaceholder placeholder ? placeholder.getOrDefault() : slot.getItem();
         contentsCopy.add(itemStack.copy());
         ((RemoteSlot)this.remoteSlots.get(index)).force(itemStack);
      }

      this.getRemoteCarried().force(this.getCarried());

      for (int index = 0; index < this.dataSlots.size(); index++) {
         this.remoteDataSlots.set(index, ((DataSlot)this.dataSlots.get(index)).get());
      }

      if (this.synchronizer != null) {
         this.synchronizer.sendInitialData(this, contentsCopy, this.getCarried().copy(), this.remoteDataSlots.toIntArray());
         this.synchronizer.sendOffHandSlotChange();
      }
   }

   protected RemoteSlot getRemoteCarried() {
      if (this.remoteCarried != null) {
         return this.remoteCarried;
      } else {
         try {
            Field remoteCarried = AbstractContainerMenu.class.getDeclaredField("remoteCarried");
            remoteCarried.setAccessible(true);
            if (remoteCarried.get(this) instanceof RemoteSlot remoteSlot) {
               this.remoteCarried = remoteSlot;
            } else {
               this.remoteCarried = RemoteSlot.PLACEHOLDER;
            }
         } catch (IllegalAccessException | NoSuchFieldException var4) {
            this.remoteCarried = RemoteSlot.PLACEHOLDER;
         }

         return this.remoteCarried;
      }
   }

   public void addSlotListener(ContainerListener containerListener) {
      if (!this.listeners.contains(containerListener)) {
         this.listeners.add(containerListener);
      }

      super.addSlotListener(containerListener);
   }

   public void setSynchronizer(ContainerSynchronizer containerSynchronizer) {
      this.synchronizer = containerSynchronizer;
      this.remoteCarried = null;
      super.setSynchronizer(containerSynchronizer);
   }

   public void removeSlotListener(ContainerListener containerListener) {
      this.listeners.remove(containerListener);
      super.removeSlotListener(containerListener);
   }

   public void suppressRemoteUpdates() {
      this.suppressRemoteUpdates = true;
      super.suppressRemoteUpdates();
   }

   public void resumeRemoteUpdates() {
      this.suppressRemoteUpdates = false;
      super.resumeRemoteUpdates();
   }
}
