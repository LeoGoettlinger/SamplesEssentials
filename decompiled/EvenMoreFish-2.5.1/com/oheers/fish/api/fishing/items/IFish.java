package com.oheers.fish.api.fishing.items;

import com.oheers.fish.api.fishing.CatchType;
import com.oheers.fish.api.items.AbstractItemFactory;
import com.oheers.fish.api.requirement.Requirement;
import com.oheers.fish.api.reward.Reward;
import com.oheers.fish.api.sort.Sortable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface IFish extends Sortable {
   @NonNull
   ItemStack give(int var1);

   @NonNull
   ItemStack give();

   double getWorthMultiplier();

   void init();

   void checkSilent();

   @NonNull
   IFish createCopy();

   boolean hasFishermanDisabled();

   @NonNull
   Optional<Double> getSetSize();

   double getMinSize();

   double getMaxSize();

   @Nullable
   UUID getFishermanUUID();

   void setFisherman(@Nullable UUID var1);

   void setFisherman(@Nullable OfflinePlayer var1);

   double getSetWorth();

   @NonNull
   @Override
   String getId();

   @NonNull
   IRarity getRarity();

   float getLength();

   default boolean isLengthless() {
      return this.getLength() <= 0.0F;
   }

   void setLength(@Nullable Float var1);

   @Override
   double getWeight();

   @NonNull
   Component getDisplayName();

   int getGlobalCatchLimit();

   int getPlayerCatchLimit();

   void setWeight(double var1);

   @NonNull
   AbstractItemFactory getFactory();

   @NonNull
   Requirement getRequirement();

   void setRequirement(@NonNull Requirement var1);

   boolean isWasBaited();

   void setWasBaited(boolean var1);

   boolean isSilent();

   void setSilent(boolean var1);

   @NonNull
   CatchType getCatchType();

   boolean getShowInJournal();

   void setShowInJournal(boolean var1);

   default RarityKey getRarityKey() {
      return RarityKey.of(this);
   }

   default boolean hasEatRewards() {
      return !this.getEatRewards().isEmpty();
   }

   default boolean hasCatchRewards() {
      return !this.getCatchRewards().isEmpty();
   }

   default boolean hasSellRewards() {
      return !this.getSellRewards().isEmpty();
   }

   default boolean hasInteractRewards() {
      return this.getInteractRewards().isEmpty();
   }

   @NonNull
   List<Reward> getInteractRewards();

   @NonNull
   List<Reward> getEatRewards();

   @NonNull
   List<Reward> getCatchRewards();

   @NonNull
   List<Reward> getSellRewards();

   @ApiStatus.Internal
   boolean isTrackInDatabase();

   @Deprecated(
      since = "2.4.5"
   )
   @NonNull
   default String getName() {
      return this.getId();
   }

   @Deprecated(
      forRemoval = true
   )
   @Nullable
   default UUID getFisherman() {
      return this.getFishermanUUID();
   }

   @Deprecated(
      forRemoval = true,
      since = "2.4.5"
   )
   default boolean isCompExemptFish() {
      return false;
   }

   @Deprecated(
      forRemoval = true,
      since = "2.4.5"
   )
   default void setCompExemptFish(boolean compExemptFish) {
   }

   @Deprecated(
      since = "2.3.5"
   )
   @NonNull
   default List<Reward> getFishRewards() {
      return this.getCatchRewards();
   }

   @Deprecated(
      since = "2.3.5"
   )
   default boolean hasFishRewards() {
      return this.hasCatchRewards();
   }

   @Deprecated(
      since = "2.4.5"
   )
   default boolean hasIntRewards() {
      return this.getInteractRewards().isEmpty();
   }

   @Deprecated(
      since = "2.4.5"
   )
   @NonNull
   default List<Reward> getActionRewards() {
      return this.hasInteractRewards() ? this.getInteractRewards() : this.getEatRewards();
   }

   @Deprecated(
      since = "2.4.7"
   )
   default int getCatchLimit() {
      return this.getGlobalCatchLimit();
   }
}
