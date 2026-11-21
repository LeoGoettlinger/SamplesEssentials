package org.lushplugins.lushrewards.rewards.custom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.rewards.Reward;
import org.lushplugins.lushrewards.utils.SchedulerType;

public class PermissionReward extends WrapperReward {
   private final String permission;

   public PermissionReward(String permission, List<Reward> rewards) {
      super(rewards);
      this.permission = permission;
   }

   public PermissionReward(Map<?, ?> map) {
      super(map);
      this.permission = (String)map.get("permission");
   }

   @Override
   protected void giveTo(Player player) {
      if (player.hasPermission(this.permission)) {
         for (Reward reward : this.rewards) {
            try {
               reward.giveReward(player);
            } catch (Exception var5) {
               LushRewards.getInstance().getLogger().severe("Error occurred when giving reward (" + reward.toString() + ") to " + player.getName());
               var5.printStackTrace();
            }
         }
      }
   }

   @Override
   public Map<String, Object> asMap() {
      Map<String, Object> rewardMap = new HashMap<>();
      rewardMap.put("type", "permission");
      rewardMap.put("permission", this.permission);
      List<Map<String, Object>> rewardsMap = this.rewards.stream().map(Reward::asMap).toList();
      rewardMap.put("rewards", rewardsMap);
      return rewardMap;
   }

   @Override
   public SchedulerType getSchedulerType() {
      return SchedulerType.ASYNC;
   }
}
