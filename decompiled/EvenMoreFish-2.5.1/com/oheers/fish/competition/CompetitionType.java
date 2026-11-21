package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.api.registry.RegistryItem;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.competition.types.LargestFishCompetitionType;
import com.oheers.fish.competition.types.RandomCompetitionType;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface CompetitionType extends RegistryItem {
   LargestFishCompetitionType DEFAULT = new LargestFishCompetitionType();
   RandomCompetitionType RANDOM = new RandomCompetitionType();

   @NonNull
   Component getTypeVariable(@NonNull Competition competition);

   @NonNull
   Component getBossbarPrefix();

   boolean shouldReverseLeaderboard();

   boolean isUsable(@NonNull Competition competition);

   void applyToLeaderboard(@NonNull IFish fish, @NonNull UUID fisher, @NonNull Leaderboard leaderboard, @NonNull Competition competition);

   @NonNull
   Component formatLeaderboardEntry(@NonNull CompetitionEntry entry);

   boolean useFishLength();

   boolean isSingleReward();

   default boolean register() {
      return CompetitionTypeRegistry.getInstance().register(this);
   }

   default boolean unregister() {
      return CompetitionTypeRegistry.getInstance().unregister(this);
   }

   @NonNull
   String getAuthor();

   @NonNull
   Plugin getPlugin();

   @Nullable
   default List<IRarity> getAllowedRaritiesOrLog(@NonNull Competition competition) {
      List<IRarity> configRarities = competition.getCompetitionFile().getAllowedRarities();
      if (configRarities.isEmpty()) {
         EvenMoreFish.getInstance()
            .getLogger()
            .severe("No allowed-rarities list found in " + competition.getCompetitionFile().getFileName() + " competition config file.");
         return null;
      } else {
         return configRarities;
      }
   }

   @ApiStatus.Internal
   public static class Forwarding implements CompetitionType {
      private final CompetitionType.Random random;
      private final CompetitionType ref;

      public Forwarding(@NonNull CompetitionType.Random random, @NonNull CompetitionType ref) {
         this.random = random;
         this.ref = ref;
         if (ref instanceof CompetitionType.Random) {
            throw new UnsupportedOperationException("CompetitionType.Forwarding cannot forward to another Random type.");
         }
      }

      @NonNull
      @Override
      public Component getTypeVariable(@NonNull Competition competition) {
         return this.ref.getTypeVariable(competition);
      }

      @NonNull
      @Override
      public Component getBossbarPrefix() {
         return this.ref.getBossbarPrefix();
      }

      @Override
      public boolean shouldReverseLeaderboard() {
         return this.ref.shouldReverseLeaderboard();
      }

      @Override
      public boolean isUsable(@NonNull Competition competition) {
         return this.ref.isUsable(competition);
      }

      @Override
      public void applyToLeaderboard(@NonNull IFish fish, @NonNull UUID fisher, @NonNull Leaderboard leaderboard, @NonNull Competition competition) {
         this.ref.applyToLeaderboard(fish, fisher, leaderboard, competition);
      }

      @NonNull
      @Override
      public Component formatLeaderboardEntry(@NonNull CompetitionEntry entry) {
         return this.ref.formatLeaderboardEntry(entry);
      }

      @Override
      public boolean useFishLength() {
         return this.ref.useFishLength();
      }

      @Override
      public boolean isSingleReward() {
         return this.ref.isSingleReward();
      }

      @NonNull
      @Override
      public String getAuthor() {
         return this.ref.getAuthor();
      }

      @NonNull
      @Override
      public Plugin getPlugin() {
         return this.ref.getPlugin();
      }

      @NonNull
      @Override
      public String getKey() {
         return this.ref.getKey();
      }

      @NonNull
      public CompetitionType getRandom() {
         return this.random;
      }
   }

   public abstract static class Random implements CompetitionType {
      @NonNull
      @Override
      public final Component getTypeVariable(@NonNull Competition competition) {
         throw new UnsupportedOperationException("RANDOM CompetitionType should not be directly used.");
      }

      @NonNull
      @Override
      public final Component getBossbarPrefix() {
         throw new UnsupportedOperationException("RANDOM CompetitionType should not be directly used.");
      }

      @Override
      public final boolean shouldReverseLeaderboard() {
         throw new UnsupportedOperationException("RANDOM CompetitionType should not be directly used.");
      }

      @Override
      public final boolean isUsable(@NonNull Competition competition) {
         throw new UnsupportedOperationException("RANDOM CompetitionType should not be directly used.");
      }

      @Override
      public final void applyToLeaderboard(@NonNull IFish fish, @NonNull UUID fisher, @NonNull Leaderboard leaderboard, @NonNull Competition competition) {
         throw new UnsupportedOperationException("RANDOM CompetitionType should not be directly used.");
      }

      @NonNull
      @Override
      public final Component formatLeaderboardEntry(@NonNull CompetitionEntry entry) {
         throw new UnsupportedOperationException("RANDOM CompetitionType should not be directly used.");
      }

      @Override
      public final boolean useFishLength() {
         throw new UnsupportedOperationException("RANDOM CompetitionType should not be directly used.");
      }

      @Override
      public final boolean isSingleReward() {
         throw new UnsupportedOperationException("RANDOM CompetitionType should not be directly used.");
      }

      @NonNull
      public abstract CompetitionType getRandomType(@NonNull Competition competition);
   }
}
