package com.magmaguy.elitemobs.magmacore.match;

import org.bukkit.entity.Player;

public interface MatchApi {
   MatchApi NONE = new MatchApi() {};

   default boolean instantiateAttempt(Match match) {
      return true;
   }

   default boolean joinAttempt(Match match, Player player) {
      return true;
   }

   default void joined(Match match, MatchPlayer player) {
   }

   default boolean startAttempt(Match match) {
      return true;
   }

   default void started(Match match) {
   }

   default void left(Match match, MatchPlayer player, LeaveReason reason) {
   }

   default void ended(Match match, MatchOutcome outcome) {
   }

   default void destroyed(Match match) {
   }
}
