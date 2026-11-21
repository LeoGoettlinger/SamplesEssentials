package com.oheers.fish.api.baits;

import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.api.reward.Reward;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface IBait {
   @NonNull
   ItemStack create(@NonNull OfflinePlayer var1);

   @NonNull
   ItemStack create();

   @NonNull
   List<? extends IRarity> getRarities();

   @NonNull
   IFish chooseFish(@NonNull Player var1, @NonNull Location var2);

   void handleFish(@NonNull Player var1, @NonNull IFish var2, @NonNull ItemStack var3);

   @NonNull
   String getId();

   @NonNull
   String getDisplayName();

   boolean isSilent();

   boolean hasCatchRewards();

   @NonNull
   List<Reward> getCatchRewards();

   double getPurchasePrice();

   int getPurchaseQuantity();

   @Nullable
   Economy getEconomy();

   boolean attemptPurchase(@NonNull Player var1);
}
