package com.github.retrooper.packetevents.protocol.component.builtin.item;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.ItemStackSerialization;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.List;
import java.util.Objects;

public class BundleContents {
   private List<ItemStack> items;

   public BundleContents(List<ItemStack> items) {
      this.items = items;
   }

   public static BundleContents read(PacketWrapper<?> wrapper) {
      List<ItemStack> items = wrapper.readList(ItemStackSerialization::readTemplate);
      return new BundleContents(items);
   }

   public static void write(PacketWrapper<?> wrapper, BundleContents projectiles) {
      wrapper.writeList(projectiles.items, ItemStackSerialization::writeTemplate);
   }

   public void addItem(ItemStack itemStack) {
      this.items.add(itemStack);
   }

   public List<ItemStack> getItems() {
      return this.items;
   }

   public void setItems(List<ItemStack> items) {
      this.items = items;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof BundleContents)) {
         return false;
      } else {
         BundleContents that = (BundleContents)obj;
         return this.items.equals(that.items);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.items);
   }
}
