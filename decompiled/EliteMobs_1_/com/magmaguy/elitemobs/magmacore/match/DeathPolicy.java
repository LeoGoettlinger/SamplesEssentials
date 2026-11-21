package com.magmaguy.elitemobs.magmacore.match;

import java.util.Objects;
import java.util.function.Function;

public interface DeathPolicy {
   static DeathPolicy eliminate() {
      return new DeathPolicy() {
         @Override
         public int initialLives() {
            return 1;
         }

         @Override
         public void handle(Match match, MatchPlayer player) {
            match.leave(player.getPlayer(), LeaveReason.DIED);
         }
      };
   }

   static DeathPolicy spectateAndRevive(int lives) {
      return spectateAndRevive(lives, participant -> ReviveMarker.NONE);
   }

   static DeathPolicy spectateAndRevive(final int lives, final Function<MatchPlayer, ReviveMarker> markers) {
      if (lives < 1) {
         throw new IllegalArgumentException("A revive policy needs at least one life");
      } else {
         Objects.requireNonNull(markers, "markers");
         return new DeathPolicy() {
            @Override
            public int initialLives() {
               return lives;
            }

            @Override
            public void handle(Match match, MatchPlayer player) {
               match.spectateUntilRevived(player, markers);
            }
         };
      }
   }

   int initialLives();

   void handle(Match var1, MatchPlayer var2);
}
