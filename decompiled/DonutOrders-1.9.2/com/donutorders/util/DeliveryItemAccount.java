package com.donutorders.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class DeliveryItemAccount {
   private final ItemStack[] snapshot;
   private final AtomicReference<DeliveryItemAccount.State> state = new AtomicReference<>(DeliveryItemAccount.State.HELD);

   public DeliveryItemAccount(ItemStack[] snapshot) {
      this.snapshot = snapshot != null ? snapshot : new ItemStack[0];
   }

   public ItemStack[] snapshot() {
      return this.snapshot;
   }

   public DeliveryItemAccount.State state() {
      return this.state.get();
   }

   public boolean trySubmit() {
      return this.state.compareAndSet(DeliveryItemAccount.State.HELD, DeliveryItemAccount.State.SUBMITTED);
   }

   public List<ItemStack> takeForReturn() {
      return !this.state.compareAndSet(DeliveryItemAccount.State.HELD, DeliveryItemAccount.State.RETURNED) ? List.of() : this.copyAndClear();
   }

   private List<ItemStack> copyAndClear() {
      List<ItemStack> out = new ArrayList<>();

      for (int i = 0; i < this.snapshot.length; i++) {
         ItemStack item = this.snapshot[i];
         this.snapshot[i] = null;
         if (item != null && item.getType() != Material.AIR) {
            out.add(item.clone());
         }
      }

      return List.copyOf(out);
   }

   public static enum State {
      HELD,
      SUBMITTED,
      RETURNED;
   }
}
