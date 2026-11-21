package com.magmaguy.elitemobs.instanced;

import com.magmaguy.elitemobs.api.PlayerTeleportEvent;
import com.magmaguy.elitemobs.magmacore.match.MatchPlayer;
import com.magmaguy.elitemobs.magmacore.match.MoveReason;
import org.bukkit.Location;
import org.bukkit.entity.Player;

final class EliteMatchPlayer extends MatchPlayer {
   private final MatchInstance instance;

   EliteMatchPlayer(Player player, MatchInstance instance) {
      super(player, instance);
      this.instance = instance;
   }

   @Override
   protected boolean teleport(Location destination, MoveReason reason) {
      if (reason != MoveReason.ENTRY) {
         return super.teleport(destination, reason);
      } else {
         boolean moved = PlayerTeleportEvent.teleportPlayer(this.getPlayer(), destination);
         if (moved) {
            this.instance.playerLives.put(this.getPlayer(), this.getLives());
         }

         return moved;
      }
   }
}
