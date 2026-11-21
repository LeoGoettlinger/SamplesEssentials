package org.lushplugins.lushrewards.api.event;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushrewards.data.RewardUser;

public class RewardUserPlaytimeChangeEvent extends RewardUserEvent {
   private static final HandlerList handlers = new HandlerList();
   private final int oldMinutesPlayed;
   private final int newMinutesPlayed;

   public RewardUserPlaytimeChangeEvent(@NotNull RewardUser rewardUser, int oldMinutesPlayed, int newMinutesPlayed) {
      super(rewardUser);
      this.oldMinutesPlayed = oldMinutesPlayed;
      this.newMinutesPlayed = newMinutesPlayed;
   }

   public int getOldMinutesPlayed() {
      return this.oldMinutesPlayed;
   }

   public int getNewMinutesPlayed() {
      return this.newMinutesPlayed;
   }

   @NotNull
   public HandlerList getHandlers() {
      return handlers;
   }

   @NotNull
   public static HandlerList getHandlerList() {
      return handlers;
   }
}
