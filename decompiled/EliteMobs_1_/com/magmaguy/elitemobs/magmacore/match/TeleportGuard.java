package com.magmaguy.elitemobs.magmacore.match;

import java.util.IdentityHashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

final class TeleportGuard implements Listener {
   private final Map<PlayerTeleportEvent, Match> staffTeleports = new IdentityHashMap<>();

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.LOW
   )
   public void onPlayerTeleport(PlayerTeleportEvent event) {
      if (!MatchMovement.authorizes(event)) {
         Player player = event.getPlayer();
         Match current = MatchCore.matchOf(player);
         if (MatchMovement.permitsStaffMove(event, current)) {
            this.staffTeleports.put(event, current);
         } else if (current != null) {
            if (!touches(current, event.getFrom()) && !touches(current, event.getTo())) {
               MatchPlayer participant = current.getMatchPlayer(player);
               if (event.getCause() == TeleportCause.SPECTATE && participant != null && participant.role == MatchRole.SPECTATOR) {
                  event.setCancelled(true);
               } else if (current.phase == MatchPhase.WAITING) {
                  current.leave(player, LeaveReason.QUIT);
               } else {
                  event.setCancelled(true);
               }
            } else {
               event.setCancelled(true);
            }
         } else {
            refuseStrangers(event, player);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void validateAuthorizedMovement(PlayerTeleportEvent event) {
      Match permitted = this.staffTeleports.remove(event);
      if (!event.isCancelled()) {
         if (permitted != null && !MatchMovement.permitsStaffMove(event, permitted)) {
            event.setCancelled(true);
         } else {
            if (MatchMovement.hasAuthorization(event.getPlayer()) && !MatchMovement.authorizes(event)) {
               event.setCancelled(true);
            }
         }
      }
   }

   private static void refuseStrangers(PlayerTeleportEvent event, Player player) {
      Location to = event.getTo();
      if (to != null) {
         for (Match match : MatchCore.matches()) {
            if (match.isOpen()) {
               MatchSettings settings = match.getSettings();
               if (settings.getSpace().guardsEntryDuring(match.phase) && match.contains(to) && !match.contains(event.getFrom())) {
                  String bypass = settings.getBypassPermission();
                  if (bypass == null || !player.hasPermission(bypass)) {
                     event.setCancelled(true);
                     return;
                  }
               }
            }
         }
      }
   }

   private static boolean touches(Match match, Location location) {
      if (location == null) {
         return false;
      } else {
         World world = location.getWorld();
         return world != null && match.getSettings().getSpace().worlds().contains(world);
      }
   }
}
