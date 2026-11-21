package com.oheers.fish.placeholders.impl.player;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.placeholders.abstracted.EMFPlaceholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CustomFishingBooleanPlaceholder implements EMFPlaceholder {
   @Override
   public boolean shouldProcess(@NonNull String identifier) {
      return identifier.equalsIgnoreCase("custom_fishing_boolean");
   }

   @Nullable
   @Override
   public String parsePAPI(@Nullable OfflinePlayer player, @NonNull String identifier) {
      return player instanceof Player online ? String.valueOf(!EvenMoreFish.getInstance().getToggle().isCustomFishingDisabled(online)) : null;
   }
}
