package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.dungeons.EliteMobsWorld;
import com.magmaguy.elitemobs.instanced.InstancePlayerMovement;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.wormhole.WormholeManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class PlayerTeleportEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final Location destination;
   private final Location originalLocation;
   private final Player player;
   private final boolean deferredExecution;
   private final PlayerTeleportEvent.Purpose purpose;
   private boolean isCancelled = false;
   private boolean teleportSucceeded;

   public PlayerTeleportEvent(Player player, Location destination) {
      this(player, destination, false, PlayerTeleportEvent.Purpose.NORMAL);
   }

   public PlayerTeleportEvent(Player player, Location destination, PlayerTeleportEvent.Purpose purpose) {
      this(player, destination, false, purpose);
   }

   private PlayerTeleportEvent(Player player, Location destination, boolean deferredExecution, PlayerTeleportEvent.Purpose purpose) {
      this.player = player;
      this.destination = destination;
      this.originalLocation = player.getLocation().clone();
      this.deferredExecution = deferredExecution;
      this.purpose = Objects.requireNonNull(purpose, "purpose");
   }

   public static boolean teleportPlayer(Player player, Location destination) {
      return teleportPlayer(player, destination, PlayerTeleportEvent.Purpose.NORMAL);
   }

   public static boolean teleportPlayer(Player player, Location destination, PlayerTeleportEvent.Purpose purpose) {
      if (destination != null && destination.getWorld() != null) {
         PlayerTeleportEvent event = new PlayerTeleportEvent(player, destination, purpose);
         new EventCaller(event);
         return !event.isCancelled && event.teleportSucceeded;
      } else {
         return false;
      }
   }

   public static boolean teleportPlayers(Collection<Player> players, Location destination) {
      if (destination != null && destination.getWorld() != null) {
         LinkedHashMap<UUID, Player> uniquePlayers = new LinkedHashMap<>();

         for (Player player : players) {
            if (player != null) {
               uniquePlayers.putIfAbsent(player.getUniqueId(), player);
            }
         }

         if (uniquePlayers.isEmpty()) {
            return false;
         } else {
            List<PlayerTeleportEvent> events = new ArrayList<>();

            for (Player playerx : uniquePlayers.values()) {
               if (!playerx.isOnline() || !playerx.isValid()) {
                  return false;
               }

               PlayerTeleportEvent event = new PlayerTeleportEvent(playerx, destination, true, PlayerTeleportEvent.Purpose.NORMAL);
               new EventCaller(event);
               if (event.isCancelled()) {
                  return false;
               }

               events.add(event);
            }

            if (events.stream().anyMatch(eventx -> !eventx.player.isOnline() || !eventx.player.isValid())) {
               return false;
            } else {
               boolean moved = true;

               for (PlayerTeleportEvent event : events) {
                  event.executeTeleport();
                  moved &= event.teleportSucceeded;
               }

               return moved;
            }
         }
      } else {
         return false;
      }
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public boolean isCancelled() {
      return this.isCancelled;
   }

   public void setCancelled(boolean b) {
      this.isCancelled = b;
   }

   public Location getOriginalLocation() {
      return this.originalLocation;
   }

   public void executeTeleport() {
      if (!this.isCancelled && this.destination != null && this.destination.getWorld() != null) {
         if (!this.player.getPassengers().isEmpty()) {
            this.player.getPassengers().forEach(this.player::removePassenger);
         }

         if (this.purpose == PlayerTeleportEvent.Purpose.LEAVE_INSTANCE) {
            this.teleportSucceeded = InstancePlayerMovement.teleportLeavingInstance(this.player, this.destination, TeleportCause.PLUGIN);
         } else {
            this.teleportSucceeded = this.player.teleport(this.destination);
         }

         if (this.teleportSucceeded) {
            if (!EliteMobsWorld.isEliteMobsWorld(this.originalLocation.getWorld().getUID())) {
               PlayerData.setBackTeleportLocation(this.player, this.originalLocation);
            }

            WormholeManager.getInstance(false).addPlayerToCooldown(this.player, this.destination);
         }
      }
   }

   @Generated
   public Location getDestination() {
      return this.destination;
   }

   @Generated
   public Player getPlayer() {
      return this.player;
   }

   public static class PlayerTeleportEventExecutor implements Listener {
      @EventHandler(
         priority = EventPriority.MONITOR,
         ignoreCancelled = true
      )
      public void onPlayerTeleport(PlayerTeleportEvent event) {
         if (!event.deferredExecution) {
            event.executeTeleport();
         }
      }
   }

   public static enum Purpose {
      NORMAL,
      LEAVE_INSTANCE;
   }
}
