package com.magmaguy.elitemobs.instanced;

import com.magmaguy.elitemobs.magmacore.match.MatchCore;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public final class InstancePlayerMovement {
   private InstancePlayerMovement() {
   }

   public static boolean teleportWithinWorld(Player player, Location destination, TeleportCause cause) {
      return MatchCore.moveWithin(player, destination, cause);
   }

   public static boolean teleportLeavingInstance(Player player, Location destination, TeleportCause cause) {
      return MatchCore.moveOut(player, destination, cause);
   }

   public static boolean teleportForMatch(Player player, Location destination, MatchInstance match, boolean notifyEliteMobs) {
      Objects.requireNonNull(player, "player");
      return match == null ? false : match.moveParticipant(player, destination);
   }
}
