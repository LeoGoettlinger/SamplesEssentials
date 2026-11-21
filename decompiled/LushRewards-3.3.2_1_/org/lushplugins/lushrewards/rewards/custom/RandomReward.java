package org.lushplugins.lushrewards.rewards.custom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.entity.Player;
import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.exceptions.InvalidRewardException;
import org.lushplugins.lushrewards.libraries.lushlib.utils.RandomCollection;
import org.lushplugins.lushrewards.rewards.Reward;
import org.lushplugins.lushrewards.utils.SchedulerType;

public class RandomReward extends Reward {
   private final RandomCollection<Reward> rewards;

   public RandomReward(RandomCollection<Reward> rewards) {
      this.rewards = rewards;
   }

   public RandomReward(Map<?, ?> map) {
      super(map);

      List<Map<?, ?>> rewardMaps;
      try {
         rewardMaps = map.containsKey("rewards") ? (List)map.get("rewards") : List.of(Collections.emptyMap());
      } catch (ClassCastException var7) {
         throw new InvalidRewardException("Invalid config format at '" + map + "'");
      }

      this.rewards = new RandomCollection<>();

      for (Map<?, ?> rewardMap : rewardMaps) {
         Reward reward = Reward.loadReward(rewardMap, rewardMap.toString());
         int weight = rewardMap.containsKey("weight") ? (Integer)rewardMap.get("weight") : 1;
         if (reward != null) {
            this.rewards.add(reward, weight);
         }
      }
   }

   @Override
   protected void giveTo(Player player) {
      if (this.rewards != null && !this.rewards.isEmpty()) {
         Reward reward = this.rewards.next();

         try {
            reward.giveReward(player);
         } catch (Exception var4) {
            LushRewards.getInstance().getLogger().severe("Error occurred when giving reward (" + reward.toString() + ") to " + player.getName());
            var4.printStackTrace();
         }
      }
   }

   @Override
   public Map<String, Object> asMap() {
      Map<String, Object> rewardMap = new HashMap<>();
      List<Map<String, Object>> rewardsMap = new ArrayList<>();

      for (Entry<Double, Reward> entry : this.rewards.getMap().entrySet()) {
         double weight = entry.getKey();
         Reward reward = entry.getValue();
         Map<String, Object> weightRewardMap = reward.asMap();
         weightRewardMap.put("weight", weight);
         rewardsMap.add(weightRewardMap);
      }

      rewardMap.put("type", "random");
      rewardMap.put("rewards", rewardsMap);
      return rewardMap;
   }

   @Override
   public SchedulerType getSchedulerType() {
      return SchedulerType.ASYNC;
   }
}
