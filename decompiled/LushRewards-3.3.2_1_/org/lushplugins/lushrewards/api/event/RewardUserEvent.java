package org.lushplugins.lushrewards.api.event;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushrewards.data.RewardUser;

public abstract class RewardUserEvent extends Event {
   protected final RewardUser rewardUser;

   public RewardUserEvent(@NotNull RewardUser rewardUser) {
      this.rewardUser = rewardUser;
   }

   public RewardUserEvent(@NotNull RewardUser rewardUser, boolean async) {
      super(async);
      this.rewardUser = rewardUser;
   }

   @NotNull
   public final RewardUser getRewardUser() {
      return this.rewardUser;
   }
}
