package com.oheers.fish.addons.internal.reward.message;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.reward.RewardType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.evenmorefish.fish.libs.daisylib.messages.message.ComponentMessage;
import org.evenmorefish.fish.libs.daisylib.messages.message.MessageType;
import org.jspecify.annotations.NonNull;

abstract class MessageRewardBase extends RewardType {
   @Override
   public void doReward(@NonNull Player player, @NonNull String key, @NonNull String value, Location hookLocation) {
      ComponentMessage.componentMessage(value, this.getMessageType()).replace("{player}", player.name()).parsePlaceholderAPI(player).send(player);
   }

   @NonNull
   public JavaPlugin getPlugin() {
      return EvenMoreFish.getInstance();
   }

   @NonNull
   public abstract MessageType getMessageType();
}
