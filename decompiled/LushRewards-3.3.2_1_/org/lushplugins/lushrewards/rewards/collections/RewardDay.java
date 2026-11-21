package org.lushplugins.lushrewards.rewards.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushrewards.module.dailyrewards.DailyRewardCollection;

public class RewardDay {
   private final Collection<DailyRewardCollection> dailyRewardCollections = new ArrayList<>();

   @NotNull
   public DailyRewardCollection getHighestPriorityRewardCollection() {
      return this.dailyRewardCollections.stream().max(Comparator.comparingInt(RewardCollection::getPriority)).orElse(DailyRewardCollection.empty());
   }

   public void giveAllRewards(Player player) {
      for (DailyRewardCollection dailyRewardCollection : this.dailyRewardCollections) {
         dailyRewardCollection.giveAll(player);
      }
   }

   public void addCollection(DailyRewardCollection dailyRewardCollection) {
      this.dailyRewardCollections.add(dailyRewardCollection);
   }

   public void addCollections(Collection<DailyRewardCollection> dailyRewardCollections) {
      for (DailyRewardCollection dailyRewardCollection : dailyRewardCollections) {
         this.addCollection(dailyRewardCollection);
      }
   }

   public boolean containsRewardFromCategory(String category) {
      for (DailyRewardCollection dailyRewardCollection : this.dailyRewardCollections) {
         if (dailyRewardCollection.getCategory().equals(category)) {
            return true;
         }
      }

      return false;
   }

   public int getRewardCount() {
      int totalRewards = 0;

      for (DailyRewardCollection dailyRewardCollection : this.dailyRewardCollections) {
         totalRewards += dailyRewardCollection.getRewardCount();
      }

      return totalRewards;
   }

   public boolean isEmpty() {
      return this.dailyRewardCollections.isEmpty();
   }

   @NotNull
   public static RewardDay from(Collection<DailyRewardCollection> dailyRewardCollections) {
      RewardDay rewardDay = new RewardDay();
      rewardDay.addCollections(dailyRewardCollections);
      return rewardDay;
   }

   @NotNull
   public static RewardDay from(DailyRewardCollection... dailyRewardCollections) {
      RewardDay rewardDay = new RewardDay();

      for (DailyRewardCollection dailyRewardCollection : dailyRewardCollections) {
         rewardDay.addCollection(dailyRewardCollection);
      }

      return rewardDay;
   }
}
