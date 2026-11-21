package com.github.retrooper.packetevents.protocol.component.builtin.item;

import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntitySet;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;

public class ItemRepairable {
   private MappedEntitySet<ItemType> items;

   public ItemRepairable(MappedEntitySet<ItemType> items) {
      this.items = items;
   }

   public static ItemRepairable read(PacketWrapper<?> wrapper) {
      MappedEntitySet<ItemType> items = MappedEntitySet.read(wrapper, ItemTypes.getRegistry());
      return new ItemRepairable(items);
   }

   public static void write(PacketWrapper<?> wrapper, ItemRepairable repairable) {
      MappedEntitySet.write(wrapper, repairable.items);
   }

   public MappedEntitySet<ItemType> getItems() {
      return this.items;
   }

   public void setItems(MappedEntitySet<ItemType> items) {
      this.items = items;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof ItemRepairable)) {
         return false;
      } else {
         ItemRepairable that = (ItemRepairable)obj;
         return this.items.equals(that.items);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.items);
   }

   @Override
   public String toString() {
      return "ItemRepairable{items=" + this.items + '}';
   }
}
