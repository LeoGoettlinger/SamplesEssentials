package org.lushplugins.lushrewards.rewards.custom;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.lushrewards.rewards.Reward;
import org.lushplugins.lushrewards.utils.SchedulerType;

@Deprecated(
   since = "3.0.0"
)
public class MessageReward extends Reward {
   private final String message;

   public MessageReward(String message) {
      this.message = message;
   }

   public MessageReward(Map<?, ?> map) {
      this.message = (String)map.get("message");
      LushRewards.getInstance()
         .getLogger()
         .warning("Deprecated: Reward type 'message' is scheduled for removal, use the new 'message' option in any reward type");
   }

   @Override
   protected void giveTo(Player player) {
      ChatColorHandler.sendMessage(player, this.message.replace("%player%", player.getDisplayName()));
   }

   @Override
   public Map<String, Object> asMap() {
      Map<String, Object> rewardMap = new HashMap<>();
      rewardMap.put("type", "message");
      rewardMap.put("message", this.message);
      return rewardMap;
   }

   @Override
   public SchedulerType getSchedulerType() {
      return SchedulerType.ASYNC;
   }
}
