package com.oheers.fish.api.fishing.items;

import com.oheers.fish.api.requirement.Requirement;
import com.oheers.fish.api.sort.Sortable;
import java.util.List;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface IRarity extends Sortable {
   @NonNull
   @Override
   String getId();

   boolean isDisabled();

   @Override
   double getWeight();

   @NonNull
   Component getDisplayName();

   boolean getBroadcastEnabled();

   boolean getBroadcastOnlyRods();

   int getBroadcastRange();

   boolean getUseConfigCasing();

   @Nullable
   String getPermission();

   @NonNull
   Requirement getRequirement();

   boolean isShouldDisableFisherman();

   double getSetWorth();

   @NonNull
   Optional<Double> getSetSize();

   double getMinSize();

   double getMaxSize();

   @NonNull
   List<? extends IFish> getOriginalFishList();

   @NonNull
   List<? extends IFish> getFishList();

   @Nullable
   IFish getEditableFish(@NonNull String var1);

   @Nullable
   IFish getFish(@NonNull String var1);

   double getWorthMultiplier();

   @NonNull
   ItemStack getJournalItem();

   boolean getShowInJournal();

   void setShowInJournal(boolean var1);

   boolean isFishWeighted();

   void setFishWeighted(boolean var1);

   @Deprecated(
      forRemoval = true
   )
   default boolean getAnnounce() {
      return this.getBroadcastEnabled();
   }

   @Deprecated(
      forRemoval = true
   )
   default ItemStack getMaterial() {
      return this.getJournalItem();
   }
}
