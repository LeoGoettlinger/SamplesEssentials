package com.oheers.fish.competition.types;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SpecificRarityCompetitionType implements CompetitionType {
   private IRarity selectedRarity;

   @NonNull
   @Override
   public Component getTypeVariable(@NonNull Competition competition) {
      EMFMessage message = ConfigMessage.COMPETITION_TYPE_SPECIFIC_RARITY.getMessage();
      message.setRarity(this.selectedRarity);
      message.setAmount(competition.getNumberNeeded());
      return message.getComponentMessage();
   }

   @NonNull
   @Override
   public Component getBossbarPrefix() {
      return Component.text("Specific Rarity");
   }

   @Override
   public boolean shouldReverseLeaderboard() {
      return false;
   }

   @Override
   public boolean isUsable(@NonNull Competition competition) {
      if (!this.chooseRarity(competition)) {
         Logging.warn("Failed to select a rarity for Competition " + competition.getCompetitionName());
         return false;
      } else {
         if (competition.getNumberNeeded() <= 0) {
            Logging.warn("Competition " + competition.getCompetitionName() + " does not have number-needed set. Defaulting to 1.");
            competition.setNumberNeeded(1);
         }

         return true;
      }
   }

   @Override
   public void applyToLeaderboard(@NonNull IFish fish, @NonNull UUID fisher, @NonNull Leaderboard leaderboard, @NonNull Competition competition) {
      if (fish.getRarity().equals(this.selectedRarity)) {
         CompetitionEntry entry = leaderboard.getEntry(fisher);
         if (entry == null) {
            entry = new CompetitionEntry(fisher, fish, this);
            leaderboard.addEntry(entry);
         } else {
            entry = leaderboard.trackFish(entry, fish);
         }

         if (entry.getValue() >= competition.getNumberNeeded()) {
            competition.setSingleWinner(fisher);
            competition.end(false);
         }
      }
   }

   @NonNull
   @Override
   public Component formatLeaderboardEntry(@NonNull CompetitionEntry entry) {
      EMFMessage message = ConfigMessage.LEADERBOARD_MOST_FISH.getMessage();
      message.setAmount((int)entry.getValue());
      return message.getComponentMessage();
   }

   @Override
   public boolean useFishLength() {
      return false;
   }

   @Override
   public boolean isSingleReward() {
      return true;
   }

   @NonNull
   @Override
   public String getAuthor() {
      return "FireML";
   }

   @NonNull
   @Override
   public Plugin getPlugin() {
      return EvenMoreFish.getInstance();
   }

   @NonNull
   @Override
   public String getKey() {
      return "specific_rarity";
   }

   @Nullable
   public IRarity getSelectedRarity() {
      return this.selectedRarity;
   }

   public boolean chooseRarity(@NonNull Competition competition) {
      List<IRarity> configRarities = this.getAllowedRaritiesOrLog(competition);
      if (configRarities == null) {
         return false;
      } else {
         Logger logger = EvenMoreFish.getInstance().getLogger();

         try {
            IRarity rarity = configRarities.get(EvenMoreFish.RANDOM.nextInt(configRarities.size()));
            if (rarity == null) {
               rarity = FishManager.getInstance()
                  .getRandomWeightedRarity(
                     null, 0.0, Collections.emptySet(), Set.copyOf(FishManager.getInstance().getRarityMap().values()), null, RequirementContext.empty()
                  );
            }

            if (rarity == null) {
               logger.severe("No rarity could be chosen for " + competition.getCompetitionName());
               return false;
            } else {
               this.selectedRarity = rarity;
               return true;
            }
         } catch (Exception var5) {
            logger.severe("Could not load: " + competition.getCompetitionName() + " because a random rarity could not be chosen.");
            logger.severe(() -> "rarityMap.size(): " + FishManager.getInstance().getRarityMap().size());
            logger.severe(() -> "configRarities.size(): " + configRarities.size());
            logger.log(Level.SEVERE, var5.getMessage(), (Throwable)var5);
            return false;
         }
      }
   }
}
