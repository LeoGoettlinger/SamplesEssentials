package me.byteful.plugin.leveltools.profile.reward;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class RewardProfile {
   private final String id;
   private final Map<Integer, List<RewardEntry>> levelRewards;

   public RewardProfile(@NotNull String id, @NotNull Map<Integer, List<RewardEntry>> levelRewards) {
      this.id = id;
      this.levelRewards = Collections.unmodifiableMap(levelRewards);
   }

   @NotNull
   public String getId() {
      return this.id;
   }

   @NotNull
   public Map<Integer, List<RewardEntry>> getLevelRewards() {
      return this.levelRewards;
   }

   @NotNull
   public List<RewardEntry> getRewardsForLevel(int level) {
      return this.levelRewards.getOrDefault(level, Collections.emptyList());
   }

   public boolean hasRewardsForLevel(int level) {
      return this.levelRewards.containsKey(level);
   }
}
