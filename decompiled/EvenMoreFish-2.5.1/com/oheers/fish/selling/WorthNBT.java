package com.oheers.fish.selling;

import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.fishing.items.FishManager;
import java.util.Optional;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

public class WorthNBT {
   private WorthNBT() {
      throw new UnsupportedOperationException();
   }

   public static void setNBT(@NonNull ItemStack fishItem, @NonNull IFish fish) {
      FishManager.getInstance().setFishNbt(fishItem, fish);
   }

   public static void setNBT(@NonNull Skull skull, @NonNull IFish fish) {
      FishManager.getInstance().setFishNbt(skull, fish);
   }

   @NonNull
   public static Optional<Double> getValue(@NonNull IFish fish) {
      double setWorth = fish.getSetWorth();
      if (setWorth > 0.0) {
         return Optional.of(setWorth);
      } else {
         return fish.isLengthless() ? Optional.empty() : getMultipliedValue(fish.getLength(), fish);
      }
   }

   private static Optional<Double> getMultipliedValue(float length, @NonNull IFish fish) {
      double multiplier = fish.getWorthMultiplier();
      return multiplier <= 0.0 ? Optional.empty() : Optional.of(multiplier * length);
   }
}
