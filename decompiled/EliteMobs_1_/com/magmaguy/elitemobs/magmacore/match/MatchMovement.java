package com.magmaguy.elitemobs.magmacore.match;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

final class MatchMovement {
   private static final double DESTINATION_EPSILON_SQUARED = 1.0E-10;
   private static final ThreadLocal<Deque<MatchMovement.Authorization>> AUTHORIZATIONS = new ThreadLocal<>();

   private MatchMovement() {
   }

   static boolean moveForMatch(Match match, Player player, Location destination, MoveReason reason) {
      Match current = MatchCore.matchOf(player);
      return moveForMatch(match, player, current == match ? match.getMatchPlayer(player) : null, destination, reason);
   }

   static boolean moveForMatch(Match match, Player player, MatchPlayer participant, Location destination, MoveReason reason) {
      if (Bukkit.isPrimaryThread() && match != null && destination != null && destination.getWorld() != null && player.isOnline() && player.isValid()) {
         Match current = MatchCore.matchOf(player);
         return !permitsLifecycleMovement(player, destination, match, current)
            ? false
            : teleportAuthorized(
               player,
               destination,
               TeleportCause.PLUGIN,
               current,
               false,
               match,
               () -> participant != null ? participant.teleport(destination, reason) : player.teleport(destination, TeleportCause.PLUGIN)
            );
      } else {
         return false;
      }
   }

   static boolean moveWithin(Player player, Location destination, TeleportCause cause) {
      Objects.requireNonNull(player, "player");
      Objects.requireNonNull(destination, "destination");
      Objects.requireNonNull(cause, "cause");
      if (Bukkit.isPrimaryThread() && player.isOnline() && player.isValid()) {
         World source = player.getWorld();
         if (destination.getWorld() != null && source.equals(destination.getWorld())) {
            Match match = MatchCore.matchOf(player);
            if (match != null) {
               MatchPlayer participant = match.getMatchPlayer(player);
               if (!match.getSettings().getSpace().worlds().contains(source)
                  || match.phase != MatchPhase.ONGOING && !match.waitingToStart(player)
                  || participant == null
                  || participant.role != MatchRole.PLAYER
                  || !match.contains(player.getLocation())
                  || !match.contains(destination)) {
                  return false;
               }
            }

            return teleportAuthorized(player, destination, cause, match, false, null, () -> player.teleport(destination, cause));
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   static boolean moveOut(Player player, Location destination, TeleportCause cause) {
      Objects.requireNonNull(player, "player");
      Objects.requireNonNull(destination, "destination");
      Objects.requireNonNull(cause, "cause");
      if (Bukkit.isPrimaryThread() && player.isOnline() && player.isValid() && destination.getWorld() != null) {
         Match match = MatchCore.matchOf(player);
         if (match == null) {
            return player.teleport(destination, cause);
         } else if (match.contains(destination)) {
            return false;
         } else {
            boolean moved = teleportAuthorized(player, destination, cause, match, true, null, () -> player.teleport(destination, cause));
            if (moved && MatchCore.matchOf(player) == match && !match.contains(player.getLocation())) {
               match.leave(player, LeaveReason.QUIT);
            }

            return moved;
         }
      } else {
         return false;
      }
   }

   static boolean permitsStaffMove(PlayerTeleportEvent event, Match match) {
      Player player = event.getPlayer();
      Location destination = event.getTo();
      if (match != null && match.isOpen() && match.phase == MatchPhase.ONGOING && player.isOnline() && player.isValid()) {
         String permission = match.getSettings().getWithinTeleportPermission();
         if (permission != null && player.hasPermission(permission)) {
            return event.getCause() != TeleportCause.COMMAND && event.getCause() != TeleportCause.PLUGIN
               ? false
               : MatchCore.matchOf(player) == match
                  && destination != null
                  && destination.getWorld() != null
                  && destination.getWorld().equals(event.getFrom().getWorld())
                  && match.contains(event.getFrom())
                  && match.contains(destination);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean permitsLifecycleMovement(Player player, Location destination, Match match, Match current) {
      if (current != null && current != match) {
         return false;
      } else {
         return !match.contains(destination)
            ? match.contains(player.getLocation())
            : match.isOpen() && current == match && match.getMatchPlayer(player) != null;
      }
   }

   static boolean teleportAuthorized(
      Player player, Location destination, TeleportCause cause, Match match, boolean leaving, Match lifecycleOwner, BooleanSupplier teleport
   ) {
      MatchMovement.Authorization authorization = new MatchMovement.Authorization(
         player.getUniqueId(), player.getWorld().getUID(), destination.getWorld().getUID(), destination.clone(), cause, match, leaving, lifecycleOwner
      );
      Deque<MatchMovement.Authorization> stack = AUTHORIZATIONS.get();
      if (stack == null) {
         stack = new ArrayDeque<>();
         AUTHORIZATIONS.set(stack);
      }

      stack.push(authorization);

      boolean var9;
      try {
         var9 = teleport.getAsBoolean();
      } finally {
         MatchMovement.Authorization removed = stack.pop();
         if (removed != authorization) {
            throw new IllegalStateException("Match movement authorization stack was corrupted");
         }

         if (stack.isEmpty()) {
            AUTHORIZATIONS.remove();
         }
      }

      return var9;
   }

   static boolean authorizes(PlayerTeleportEvent event) {
      Location destination = event.getTo();
      if (destination != null && destination.getWorld() != null) {
         Deque<MatchMovement.Authorization> stack = AUTHORIZATIONS.get();
         if (stack == null) {
            return false;
         } else {
            Player player = event.getPlayer();

            for (MatchMovement.Authorization authorization : stack) {
               if (authorization.playerId().equals(player.getUniqueId())
                  && authorization.sourceWorldId().equals(event.getFrom().getWorld().getUID())
                  && authorization.destinationWorldId().equals(destination.getWorld().getUID())
                  && authorization.cause() == event.getCause()
                  && !(authorization.destination().distanceSquared(destination) > 1.0E-10)) {
                  Match current = MatchCore.matchOf(player);
                  if (current != authorization.match()) {
                     return false;
                  }

                  if (authorization.lifecycleOwner() != null) {
                     return permitsLifecycleMovement(player, destination, authorization.lifecycleOwner(), current);
                  }

                  if (current == null) {
                     return true;
                  }

                  if (authorization.leaving()) {
                     return !current.contains(destination) && current.getMatchPlayer(player) != null;
                  }

                  MatchPlayer participant = current.getMatchPlayer(player);
                  return (current.phase == MatchPhase.ONGOING || current.waitingToStart(player))
                     && participant != null
                     && participant.role == MatchRole.PLAYER
                     && spansWorld(current, authorization.sourceWorldId())
                     && current.contains(event.getFrom())
                     && current.contains(destination);
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   static boolean hasAuthorization(Player player) {
      Deque<MatchMovement.Authorization> stack = AUTHORIZATIONS.get();
      if (stack == null) {
         return false;
      } else {
         UUID playerId = player.getUniqueId();
         return stack.stream().anyMatch(authorization -> authorization.playerId().equals(playerId));
      }
   }

   private static boolean spansWorld(Match match, UUID worldId) {
      for (World world : match.getSettings().getSpace().worlds()) {
         if (world.getUID().equals(worldId)) {
            return true;
         }
      }

      return false;
   }

   private record Authorization(
      UUID playerId, UUID sourceWorldId, UUID destinationWorldId, Location destination, TeleportCause cause, Match match, boolean leaving, Match lifecycleOwner
   ) {
   }
}
