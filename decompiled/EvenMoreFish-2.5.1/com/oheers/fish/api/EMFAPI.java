package com.oheers.fish.api;

import com.oheers.fish.baits.manager.BaitManager;
import com.oheers.fish.exceptions.InvalidFishException;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class EMFAPI {
   @Deprecated(
      forRemoval = true
   )
   public boolean isFish(@Nullable ItemStack item) {
      return FishManager.getInstance().isFish(item);
   }

   @Deprecated(
      forRemoval = true
   )
   public boolean isFish(@Nullable Skull skull) {
      return FishManager.getInstance().isFish(skull);
   }

   @Deprecated(
      forRemoval = true
   )
   public boolean isBait(@NonNull ItemStack item) {
      return BaitManager.getInstance().isBait(item);
   }

   @Deprecated(
      forRemoval = true
   )
   @Nullable
   public Fish getFish(ItemStack item) {
      return FishManager.getInstance().getFish(item) instanceof Fish fish ? fish : null;
   }

   @Deprecated(
      forRemoval = true
   )
   @Nullable
   public Fish getFish(Skull skull, Player fisher) throws InvalidFishException {
      return FishManager.getInstance().getFish(skull, fisher) instanceof Fish fish ? fish : null;
   }

   @Deprecated(
      forRemoval = true
   )
   @Nullable
   public Fish getFish(String rarityName, String fishName) {
      return FishManager.getInstance().getFish(rarityName, fishName) instanceof Fish fish ? fish : null;
   }
}
