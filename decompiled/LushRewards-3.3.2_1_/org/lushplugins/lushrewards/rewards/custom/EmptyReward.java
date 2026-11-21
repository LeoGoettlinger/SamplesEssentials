package org.lushplugins.lushrewards.rewards.custom;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.lushplugins.lushrewards.rewards.Reward;
import org.lushplugins.lushrewards.utils.SchedulerType;

public class EmptyReward extends Reward {
   public EmptyReward() {
   }

   public EmptyReward(Map<?, ?> map) {
      super(map);
   }

   @Override
   protected void giveTo(Player player) {
   }

   @Override
   protected SchedulerType getSchedulerType() {
      return SchedulerType.ASYNC;
   }

   @Override
   public Map<String, Object> asMap() {
      Map<String, Object> rewardMap = new HashMap<>();
      rewardMap.put("type", "empty");
      return rewardMap;
   }
}
