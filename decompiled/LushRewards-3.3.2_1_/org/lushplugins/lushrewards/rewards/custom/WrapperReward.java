package org.lushplugins.lushrewards.rewards.custom;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.lushplugins.lushrewards.exceptions.InvalidRewardException;
import org.lushplugins.lushrewards.rewards.Reward;

public abstract class WrapperReward extends Reward {
   protected final List<Reward> rewards;

   public WrapperReward(List<Reward> rewards) {
      this.rewards = rewards;
   }

   public WrapperReward(Map<?, ?> map) {
      super(map);

      List<Map<?, ?>> rewardMaps;
      try {
         rewardMaps = map.containsKey("rewards") ? (List)map.get("rewards") : List.of(Collections.emptyMap());
      } catch (ClassCastException var4) {
         throw new InvalidRewardException("Invalid config format at '" + map + "'");
      }

      this.rewards = rewardMaps.stream().map(rewardMap -> Reward.loadReward((Map<?, ?>)rewardMap, rewardMap.toString())).toList();
   }
}
