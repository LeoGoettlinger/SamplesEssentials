package com.github.retrooper.packetevents.protocol.component.builtin.item;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ItemMinimumAttackCharge {
   private float value;

   public ItemMinimumAttackCharge(float value) {
      this.value = value;
   }

   public static ItemMinimumAttackCharge read(PacketWrapper<?> wrapper) {
      return new ItemMinimumAttackCharge(wrapper.readFloat());
   }

   public static void write(PacketWrapper<?> wrapper, ItemMinimumAttackCharge component) {
      wrapper.writeFloat(component.value);
   }

   public float getValue() {
      return this.value;
   }

   public void setValue(float value) {
      this.value = value;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj != null && this.getClass() == obj.getClass()) {
         ItemMinimumAttackCharge that = (ItemMinimumAttackCharge)obj;
         return Float.compare(that.value, this.value) == 0;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.value);
   }
}
