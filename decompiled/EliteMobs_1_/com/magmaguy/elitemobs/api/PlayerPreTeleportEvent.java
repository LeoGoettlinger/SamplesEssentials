package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CombatTagConfig;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import com.magmaguy.elitemobs.utils.EventCaller;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.Generated;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerPreTeleportEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final Location destination;
   private final Location originalLocation;
   private final Player player;
   private final boolean deferredStart;
   private final PlayerTeleportEvent.Purpose purpose;
   private boolean isCancelled = false;

   public PlayerPreTeleportEvent(Player player, Location destination) {
      this(player, destination, false, PlayerTeleportEvent.Purpose.NORMAL);
   }

   public PlayerPreTeleportEvent(Player player, Location destination, PlayerTeleportEvent.Purpose purpose) {
      this(player, destination, false, purpose);
   }

   private PlayerPreTeleportEvent(Player player, Location destination, boolean deferredStart, PlayerTeleportEvent.Purpose purpose) {
      this.player = player;
      this.destination = destination.clone();
      this.originalLocation = player.getLocation().clone();
      this.deferredStart = deferredStart;
      this.purpose = Objects.requireNonNull(purpose, "purpose");
   }

   public static void teleportPlayer(Player player, Location destination) {
      if (destination.getWorld() != null) {
         if (CombatTagConfig.isEnableTeleportTimer()) {
            new EventCaller(new PlayerPreTeleportEvent(player, destination));
         } else {
            PlayerTeleportEvent.teleportPlayer(player, destination);
         }
      }
   }

   public static boolean teleportPlayers(Collection<Player> players, Location destination) {
      if (destination == null || destination.getWorld() == null) {
         return false;
      } else if (!CombatTagConfig.isEnableTeleportTimer()) {
         return PlayerTeleportEvent.teleportPlayers(players, destination);
      } else {
         LinkedHashMap<UUID, Player> uniquePlayers = new LinkedHashMap<>();

         for (Player player : players) {
            if (player != null) {
               uniquePlayers.putIfAbsent(player.getUniqueId(), player);
            }
         }

         if (uniquePlayers.isEmpty()) {
            return false;
         } else {
            List<PlayerPreTeleportEvent> events = new ArrayList<>();

            for (Player playerx : uniquePlayers.values()) {
               if (!playerx.isOnline() || !playerx.isValid()) {
                  return false;
               }

               PlayerPreTeleportEvent event = new PlayerPreTeleportEvent(playerx, destination, true, PlayerTeleportEvent.Purpose.NORMAL);
               new EventCaller(event);
               if (event.isCancelled()) {
                  return false;
               }

               events.add(event);
            }

            if (events.stream().anyMatch(eventx -> !eventx.player.isOnline() || !eventx.player.isValid())) {
               return false;
            } else {
               events.forEach(PlayerPreTeleportEvent::startTeleport);
               return true;
            }
         }
      }
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public void startTeleport() {
      (new BukkitRunnable() {
            int timerLeft = 3;

            public void run() {
               if (!PlayerPreTeleportEvent.this.player.isValid()) {
                  this.cancel();
               } else {
                  if (!PlayerPreTeleportEvent.this.player.getWorld().equals(PlayerPreTeleportEvent.this.originalLocation.getWorld())
                     || PlayerPreTeleportEvent.this.player.getLocation().getX() != PlayerPreTeleportEvent.this.originalLocation.getX()
                     || PlayerPreTeleportEvent.this.player.getLocation().getY() != PlayerPreTeleportEvent.this.originalLocation.getY()
                     || PlayerPreTeleportEvent.this.player.getLocation().getZ() != PlayerPreTeleportEvent.this.originalLocation.getZ()) {
                     PlayerPreTeleportEvent.this.isCancelled = true;
                  }

                  if (PlayerPreTeleportEvent.this.isCancelled) {
                     PlayerPreTeleportEvent.this.sendCombatTagMessage(CombatTagConfig.getTeleportCancelled());
                     this.cancel();
                  } else {
                     PlayerPreTeleportEvent.this.sendCombatTagMessage(CombatTagConfig.getTeleportTimeLeft().replace("$time", this.timerLeft + ""));
                     if (this.timerLeft == 0) {
                        PlayerTeleportEvent.teleportPlayer(
                           PlayerPreTeleportEvent.this.player, PlayerPreTeleportEvent.this.destination, PlayerPreTeleportEvent.this.purpose
                        );
                        this.cancel();
                     } else {
                        this.timerLeft--;
                     }
                  }
               }
            }
         })
         .runTaskTimer(MetadataHandler.PLUGIN, 0L, 20L);
   }

   private void sendCombatTagMessage(String message) {
      if (CombatTagConfig.isUseActionBarMessagesInsteadOfChat()) {
         ActionBarCompositor.show(this.player, ActionBarCompositor.Source.COMBAT_TRANSITION, message);
      } else {
         this.player.spigot().sendMessage(ChatMessageType.CHAT, TextComponent.fromLegacyText(message));
      }
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

   @Generated
   public Location getDestination() {
      return this.destination;
   }

   @Generated
   public Location getOriginalLocation() {
      return this.originalLocation;
   }

   @Generated
   public Player getPlayer() {
      return this.player;
   }

   public static class PlayerPreTeleportEventEvents implements Listener {
      @EventHandler(
         ignoreCancelled = true,
         priority = EventPriority.MONITOR
      )
      public void onTeleportEvent(PlayerPreTeleportEvent event) {
         if (!event.deferredStart) {
            event.startTeleport();
         }
      }
   }
}
