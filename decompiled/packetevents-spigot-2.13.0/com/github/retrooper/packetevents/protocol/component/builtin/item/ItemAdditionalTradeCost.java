package com.github.retrooper.packetevents.protocol.component.builtin.item;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ItemAdditionalTradeCost {
   private final int cost;

   public ItemAdditionalTradeCost(int cost) {
      this.cost = cost;
   }

   public static ItemAdditionalTradeCost read(PacketWrapper<?> wrapper) {
      return new ItemAdditionalTradeCost(wrapper.readVarInt());
   }

   public static void write(PacketWrapper<?> wrapper, ItemAdditionalTradeCost cost) {
      wrapper.writeVarInt(cost.cost);
   }

   public ItemAdditionalTradeCost withCost(int cost) {
      return new ItemAdditionalTradeCost(cost);
   }

   public int getCost() {
      return this.cost;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof ItemAdditionalTradeCost)) {
         return false;
      } else {
         ItemAdditionalTradeCost that = (ItemAdditionalTradeCost)obj;
         return this.cost == that.cost;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.cost);
   }
}
