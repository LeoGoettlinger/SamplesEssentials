package com.oheers.fish.competition.types;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

public class LargestFishCompetitionType implements CompetitionType {
   @NonNull
   @Override
   public Component getTypeVariable(@NonNull Competition competition) {
      return ConfigMessage.COMPETITION_TYPE_LARGEST.getMessage().getComponentMessage();
   }

   @NonNull
   @Override
   public Component getBossbarPrefix() {
      return Component.text("Largest Fish");
   }

   @Override
   public boolean shouldReverseLeaderboard() {
      return false;
   }

   @Override
   public boolean isUsable(@NonNull Competition competition) {
      return true;
   }

   @Override
   public void applyToLeaderboard(@NonNull IFish fish, @NonNull UUID fisher, @NonNull Leaderboard leaderboard, @NonNull Competition competition) {
      if (!fish.isLengthless()) {
         CompetitionEntry entry = leaderboard.getEntry(fisher);
         if (entry == null) {
            leaderboard.addEntry(new CompetitionEntry(fisher, fish, this));
         } else {
            if (fish.getLength() > entry.getFish().getLength()) {
               leaderboard.addEntry(new CompetitionEntry(fisher, fish, competition.getCompetitionType()));
               leaderboard.removeEntry(entry);
            }
         }
      }
   }

   @NonNull
   @Override
   public Component formatLeaderboardEntry(@NonNull CompetitionEntry entry) {
      EMFMessage message = ConfigMessage.LEADERBOARD_LARGEST_FISH.getMessage();
      message.setLength(FishUtils.roundFloat(entry.getValue(), 1));
      return message.getComponentMessage();
   }

   @Override
   public boolean useFishLength() {
      return true;
   }

   @Override
   public boolean isSingleReward() {
      return false;
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
      return "largest_fish";
   }
}
