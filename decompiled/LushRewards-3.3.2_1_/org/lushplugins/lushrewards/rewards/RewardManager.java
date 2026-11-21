package org.lushplugins.lushrewards.rewards;

import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushrewards.libraries.lushlib.manager.Manager;
import org.lushplugins.lushrewards.rewards.custom.BroadcastReward;
import org.lushplugins.lushrewards.rewards.custom.ConsoleCommandReward;
import org.lushplugins.lushrewards.rewards.custom.EmptyReward;
import org.lushplugins.lushrewards.rewards.custom.ItemReward;
import org.lushplugins.lushrewards.rewards.custom.MessageReward;
import org.lushplugins.lushrewards.rewards.custom.PermissionReward;
import org.lushplugins.lushrewards.rewards.custom.PlayerCommandReward;
import org.lushplugins.lushrewards.rewards.custom.RandomReward;

public class RewardManager extends Manager {
   private ConcurrentHashMap<String, Reward.Constructor> rewardTypes;

   @Override
   public void onEnable() {
      this.rewardTypes = new ConcurrentHashMap<>();
      this.register("broadcast", BroadcastReward::new);
      this.register("command", ConsoleCommandReward::new);
      this.register("empty", EmptyReward::new);
      this.register("item", ItemReward::new);
      this.register("message", MessageReward::new);
      this.register("permission", PermissionReward::new);
      this.register("player-command", PlayerCommandReward::new);
      this.register("random", RandomReward::new);
   }

   @Override
   public void onDisable() {
      if (this.rewardTypes != null) {
         this.rewardTypes.clear();
         this.rewardTypes = null;
      }
   }

   public boolean isRegistered(String type) {
      return this.rewardTypes.containsKey(type);
   }

   public void register(String type, Reward.Constructor constructor) {
      this.rewardTypes.put(type, constructor);
   }

   public void unregister(String type) {
      this.rewardTypes.remove(type);
   }

   @Nullable
   public Reward.Constructor getConstructor(String type) {
      return this.rewardTypes.get(type);
   }

   public static class Type {
      public static final String BROADCAST = "broadcast";
      public static final String CONSOLE_COMMAND = "command";
      public static final String EMPTY = "empty";
      public static final String ITEM = "item";
      public static final String MESSAGE = "message";
      public static final String PERMISSION = "permission";
      public static final String PLAYER_COMMAND = "player-command";
      public static final String RANDOM = "random";
   }
}
