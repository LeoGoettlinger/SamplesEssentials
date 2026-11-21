package com.oheers.fish.api.fishing.rods;

import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import java.util.List;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

public interface ICustomRod {
   @NonNull
   String getId();

   boolean isDisabled();

   @NonNull
   ItemStack create();

   @NonNull
   List<? extends IRarity> getAllowedRarities();

   @NonNull
   List<? extends IFish> getAllowedFish();
}
