package com.github.retrooper.packetevents.protocol.component.builtin.item;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;

public class ItemEnchantable {
   private int value;

   public ItemEnchantable(int value) {
      this.value = value;
   }

   public static ItemEnchantable read(PacketWrapper<?> wrapper) {
      int value = wrapper.readVarInt();
      return new ItemEnchantable(value);
   }

   public static void write(PacketWrapper<?> wrapper, ItemEnchantable enchantable) {
      wrapper.writeVarInt(enchantable.value);
   }

   public int getValue() {
      return this.value;
   }

   public void setValue(int value) {
      this.value = value;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof ItemEnchantable)) {
         return false;
      } else {
         ItemEnchantable that = (ItemEnchantable)obj;
         return this.value == that.value;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.value);
   }

   @Override
   public String toString() {
      return "ItemEnchantable{value=" + this.value + '}';
   }
}
