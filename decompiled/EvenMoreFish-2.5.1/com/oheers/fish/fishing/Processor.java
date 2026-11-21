package com.oheers.fish.fishing;

import com.oheers.fish.Checks;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.fishing.FishingType;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.baits.BaitHandler;
import com.oheers.fish.baits.manager.BaitNBTManager;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionManager;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.fishing.broadcast.FishBroadcast;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.rods.CustomRod;
import com.oheers.fish.fishing.rods.RodManager;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class Processor<E extends Event> {
   public static final DecimalFormat LENGTH_FORMAT = new DecimalFormat("#.0");
   private final Random random = new Random();
   private final FishingType fishingType;

   public Processor() {
      this.fishingType = FishingType.VANILLA;
   }

   public Processor(@NonNull FishingType fishingType) {
      this.fishingType = fishingType;
   }

   protected abstract void process(@NonNull E event);

   protected abstract boolean isEnabled();

   @Nullable
   public ItemStack getCaughtItem(@NonNull Player player, @NonNull Location location, @Nullable ItemStack fishingRod) {
      if (!Checks.canFishInWorld(location)) {
         Logging.debug("Fish cannot be caught in this world.");
         return null;
      } else if (!Checks.canFishInRegion(location)) {
         Logging.debug("Fish cannot be caught in this region.");
         return null;
      } else if (Checks.isMcMMOOverfishing(player, location)) {
         Logging.debug("McMMO Overfishing is active.");
         return null;
      } else {
         double baitCatchPercentage = MainConfig.getInstance().getBaitCatchPercentage();
         if (this.shouldCatchBait() && baitCatchPercentage > 0.0 && this.random.nextDouble() * 100.0 < baitCatchPercentage) {
            Logging.debug("Bait should be caught.");
            return this.getBaitItem(player, location);
         } else {
            CustomRod customRod = null;
            BaitHandler bait = null;
            if (fishingRod != null && !fishingRod.isEmpty()) {
               customRod = RodManager.getInstance().getRod(fishingRod);
               bait = this.getBaitFromRod(fishingRod);
            }

            IFish fish = this.chooseFish(player, location, bait, customRod);
            if (fish == null) {
               Logging.debug("Could not choose a fish.");
               return null;
            } else {
               if (bait != null) {
                  Logging.debug("Handling bait.");
                  bait.handleFish(player, fish, fishingRod);
               }

               fish.init();
               if (!this.fireEvent(fish, player)) {
                  Logging.debug("Event has been cancelled.");
                  return null;
               } else {
                  this.handleCaughtFish(player, location, fish);
                  this.leaderboardCheck(fish, player, location);
                  return fish.give();
               }
            }
         }
      }
   }

   private void handleCaughtFish(@NonNull Player player, @NonNull Location location, @NonNull IFish fish) {
      fish.getCatchRewards().forEach(fishReward -> fishReward.give(player, location));
      EvenMoreFish.getInstance().getMetricsManager().incrementFishCaught(1);
      if (!fish.isSilent()) {
         EMFMessage message = fish.isLengthless() ? this.getLengthlessCaughtMessage().getMessage() : this.getCaughtMessage().getMessage();
         message.setPlayer(player);
         message.setFishCatchVariables(fish);
         if (fish.getRarity().getBroadcastEnabled()) {
            new FishBroadcast(message, player, fish).broadcast();
         } else if (!EvenMoreFish.getInstance().getToggle().isCatchMessageDisabled(player)) {
            message.send(player);
         }
      }
   }

   @Nullable
   private BaitHandler getBaitFromRod(@NonNull ItemStack rod) {
      if (MainConfig.getInstance().getBaitCompetitionDisable() && CompetitionManager.getInstance().isCompetitionActive()) {
         Logging.debug("Bait ignored because bait usage is disabled during competitions.");
         return null;
      } else if (!BaitNBTManager.isBaitedRod(rod)) {
         Logging.debug("Bait ignored because the fishing rod has no applied bait NBT.");
         return null;
      } else {
         BaitHandler bait = BaitNBTManager.randomBaitApplication(rod);
         if (bait == null) {
            Logging.debug("Bait ignored because no valid applied bait could be resolved from the rod.");
            return null;
         } else {
            Logging.debug("Fetched bait " + bait.getId() + " from the rod.");
            return bait;
         }
      }
   }

   @Nullable
   private ItemStack getBaitItem(@NonNull Player player, @NonNull Location location) {
      Optional<BaitHandler> caughtBait = BaitNBTManager.randomBaitCatch();
      if (caughtBait.isEmpty()) {
         Logging.debug("Could not determine bait for player " + player.getName() + ". This is usually a bug.");
         return null;
      } else {
         BaitHandler bait = caughtBait.get();
         ItemStack baitItem = bait.create(player);
         if (bait.hasCatchRewards()) {
            bait.getCatchRewards().forEach(reward -> reward.give(player, location));
         }

         if (!bait.isSilent()) {
            EMFMessage message = ConfigMessage.BAIT_CAUGHT.getMessage();
            message.setBait(bait, baitItem);
            message.setPlayer(player);
            message.send(player);
         }

         return baitItem;
      }
   }

   @Nullable
   private IFish chooseFish(@NonNull Player player, @NonNull Location location, @Nullable BaitHandler bait, @Nullable CustomRod customRod) {
      RequirementContext context = new RequirementContext(player.getWorld(), location, player, null, null, this.fishingType);
      if (bait != null) {
         return bait.chooseFish(player, location, context, customRod);
      } else {
         IRarity rarity = FishManager.getInstance()
            .getRandomWeightedRarity(player, 1.0, Set.of(), Set.copyOf(FishManager.getInstance().getRarityMap().values()), customRod, context);
         if (rarity == null) {
            Logging.error("Could not determine a fish rarity for " + player.getName());
            return null;
         } else {
            IFish fish = FishManager.getInstance().getFish(rarity, location, player, 1.0, null, true, this, customRod, context);
            if (fish == null) {
               EvenMoreFish.getInstance().getLogger().severe("Could not determine a fish for " + player.getName());
               return null;
            } else {
               fish.setFisherman(player);
               return fish;
            }
         }
      }
   }

   protected abstract boolean fireEvent(@NonNull IFish fish, @NonNull Player player);

   protected abstract ConfigMessage getCaughtMessage();

   protected abstract ConfigMessage getLengthlessCaughtMessage();

   protected boolean isCustomFishAllowed(Player player) {
      return this.isEnabled()
         && MainConfig.getInstance().getEnabled()
         && this.competitionOnlyCheck()
         && !EvenMoreFish.getInstance().getToggle().isCustomFishingDisabled(player);
   }

   protected abstract boolean competitionOnlyCheck();

   protected abstract boolean shouldCatchBait();

   public abstract boolean canUseFish(@NonNull IFish fish);

   protected void leaderboardCheck(@NonNull IFish fish, @NonNull Player fisherman, @NonNull Location location) {
      Competition active = CompetitionManager.getInstance().getActiveCompetition();
      if (active != null) {
         List<World> competitionWorlds = active.getCompetitionFile().getRequiredWorlds();
         if (!competitionWorlds.isEmpty()) {
            World world = location.getWorld();
            if (world == null || !competitionWorlds.contains(world)) {
               return;
            }
         }

         active.applyToLeaderboard(fish, fisherman);
      }
   }
}
