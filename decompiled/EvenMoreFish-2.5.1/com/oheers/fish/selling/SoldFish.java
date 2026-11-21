package com.oheers.fish.selling;

import com.oheers.fish.fishing.items.Fish;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

@Deprecated(
   forRemoval = true
)
public class SoldFish {
   private final com.oheers.fish.api.economy.selling.SoldFish ref;

   public SoldFish(@NonNull ItemStack item) throws IllegalArgumentException {
      com.oheers.fish.api.economy.selling.SoldFish ref = com.oheers.fish.api.economy.selling.SoldFish.get(null, item);
      if (ref == null) {
         throw new IllegalArgumentException("Item is not a sellable fish.");
      } else {
         this.ref = ref;
      }
   }

   public SoldFish(@NonNull com.oheers.fish.api.economy.selling.SoldFish ref) {
      this.ref = ref;
   }

   public void setAmount(int amount) {
      this.ref.setQuantity(amount);
   }

   public void setTotalValue(double totalValue) {
      this.ref.setValue(totalValue);
   }

   @NonNull
   public String getName() {
      return this.ref.getFish().getId();
   }

   public int getAmount() {
      return this.ref.getQuantity();
   }

   public double getTotalValue() {
      return this.ref.getFinalValue();
   }

   @NonNull
   public String getRarity() {
      return this.ref.getFish().getRarity().getId();
   }

   public double getLength() {
      return this.ref.getFish().getLength();
   }

   @NonNull
   public Fish getFish() {
      if (this.ref.getFish() instanceof Fish fish) {
         return fish;
      } else {
         throw new IllegalArgumentException("SoldFish is not an internal Fish object.");
      }
   }
}
