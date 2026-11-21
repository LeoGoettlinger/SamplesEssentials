package com.magmaguy.elitemobs.wormhole;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.PlayerTeleportEvent;
import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.InitializeConfig;
import com.magmaguy.elitemobs.config.WormholesConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.magmacore.util.ChunkLocationChecker;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.SpigotMessage;
import com.magmaguy.elitemobs.quests.playercooldowns.PlayerQuestCooldowns;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import lombok.NonNull;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class WormholeManager {
   private static final long COOLDOWN_DURATION_SECONDS = 5L;
   private static final double PARTICLE_RENDER_DISTANCE = 30.0;
   private static final double SAFE_DISTANCE = 2.0;
   private static final double TELEPORT_DISTANCE_MULTIPLIER = 1.5;
   private static WormholeManager instance;
   private final Map<UUID, WormholeManager.PlayerWormholeData> playerTeleportData = new ConcurrentHashMap<>();
   private final Set<UUID> activeTravel = new HashSet<>();
   private static final Map<UUID, WormholeManager.TravelRecovery> unresolvedTravel = new HashMap<>();
   private BukkitTask wormholeTask;
   private static final int TELEPORT_CHECK_INTERVAL = 5;
   private int tickCounter = 0;

   private WormholeManager() {
      this.startWormholeTask();
   }

   public static WormholeManager getInstance(boolean shuttingDown) {
      if (shuttingDown) {
         return instance;
      } else {
         if (instance == null) {
            instance = new WormholeManager();
         }

         if (instance.wormholeTask == null || instance.wormholeTask.isCancelled()) {
            instance.startWormholeTask();
         }

         return instance;
      }
   }

   private List<Player> getNearbyPlayers(WormholeEntry wormholeEntry) {
      List<Player> nearbyPlayers = new ArrayList<>();
      Location wormholeLocation = wormholeEntry.getLocation();
      if (wormholeLocation == null) {
         return nearbyPlayers;
      } else {
         try {
            if (wormholeLocation.getWorld() == null) {
               return nearbyPlayers;
            }
         } catch (IllegalArgumentException var9) {
            return nearbyPlayers;
         }

         double maxDistanceSquared = 900.0;

         for (Entity entity : wormholeLocation.getWorld().getNearbyEntities(wormholeLocation, 30.0, 30.0, 30.0, candidate -> candidate instanceof Player)) {
            Player player = (Player)entity;
            if (player.getWorld().equals(wormholeLocation.getWorld()) && player.getLocation().distanceSquared(wormholeLocation) <= maxDistanceSquared) {
               nearbyPlayers.add(player);
            }
         }

         return nearbyPlayers;
      }
   }

   private void checkForTeleports(WormholeEntry wormholeEntry, List<Player> nearbyPlayers) {
      double teleportDistanceSquared = Math.pow(1.5 * wormholeEntry.getWormhole().getWormholeConfigFields().getSizeMultiplier(), 2.0);

      for (Player player : nearbyPlayers) {
         if (player.getWorld() == wormholeEntry.getLocation().getWorld()
            && !(player.getLocation().distanceSquared(wormholeEntry.getLocation()) > teleportDistanceSquared)
            && this.canPlayerTeleport(wormholeEntry, player)) {
            this.teleportPlayer(wormholeEntry, player);
         }
      }
   }

   private boolean canPlayerTeleport(WormholeEntry wormholeEntry, Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.activeTravel.contains(playerId) && !unresolvedTravel.containsKey(playerId)) {
         WormholeManager.PlayerWormholeData cooldown = this.playerTeleportData.get(playerId);
         return cooldown != null && !cooldown.canTeleport()
            ? false
            : PlayerQuestCooldowns.getBypassedPlayers().contains(player)
               || wormholeEntry.getWormhole().getWormholeConfigFields().getPermission() == null
               || wormholeEntry.getWormhole().getWormholeConfigFields().getPermission().isEmpty()
               || player.hasPermission(wormholeEntry.getWormhole().getWormholeConfigFields().getPermission());
      } else {
         return false;
      }
   }

   private void teleportPlayer(WormholeEntry sourceEntry, Player player) {
      WormholeEntry destinationEntry;
      if (sourceEntry == sourceEntry.getWormhole().getWormholeEntry1()) {
         destinationEntry = sourceEntry.getWormhole().getWormholeEntry2();
      } else {
         destinationEntry = sourceEntry.getWormhole().getWormholeEntry1();
      }

      Location destination = destinationEntry.getLocation();
      boolean destinationInvalid = destination == null;
      if (!destinationInvalid) {
         try {
            destinationInvalid = destination.getWorld() == null;
         } catch (IllegalArgumentException var106) {
            destinationInvalid = true;
         }
      }

      if (destinationInvalid) {
         String missingMessage = destinationEntry.getPortalMissingMessage();
         if (missingMessage == null) {
            missingMessage = sourceEntry.getPortalMissingMessage();
         }

         if (missingMessage == null) {
            player.sendMessage(WormholesConfig.getDefaultPortalMissingMessage());
         } else {
            player.sendMessage(missingMessage);
         }

         if (player.isOp() || player.hasPermission("elitemobs.*")) {
            boolean showDownload = destinationEntry.isShowDownloadHint() || sourceEntry.isShowDownloadHint();
            if (showDownload) {
               this.sendDownloadHint(player);
            } else {
               String opMsg = destinationEntry.getOpMessage();
               if (opMsg == null) {
                  opMsg = sourceEntry.getOpMessage();
               }

               if (opMsg != null) {
                  Logger.sendSimpleMessage(player, opMsg);
               }
            }
         }
      } else {
         UUID playerId = player.getUniqueId();
         if (this.activeTravel.add(playerId)) {
            double price = sourceEntry.getWormhole().getWormholeConfigFields().getCoinCost();
            if (Double.isFinite(price) && !(price < 0.0)) {
               Location target = destination.clone();
               boolean paid = false;
               boolean arrived = false;

               try {
                  if (price > 0.0) {
                     try {
                        paid = EconomyHandler.tryWithdraw(playerId, price);
                     } catch (RuntimeException var108) {
                        retainRecovery(playerId, price, "withdrawal-unknown", sourceEntry, var108);
                        return;
                     }

                     if (!paid) {
                        player.sendMessage(WormholesConfig.getInsufficientCurrencyForWormholeMessage().replace("$amount", EconomyHandler.formatCurrency(price)));
                        return;
                     }
                  }

                  boolean accepted = PlayerTeleportEvent.teleportPlayer(player, target);
                  arrived = accepted && atDestination(player, target);
               } catch (RuntimeException var109) {
                  arrived = atDestination(player, target);
                  Logger.warn("Wormhole travel failed for " + playerId + ": " + var109);
               } finally {
                  if (paid && !arrived) {
                     try {
                        if (!EconomyHandler.refundPayment(playerId, price)) {
                           retainRecovery(playerId, price, "refund-rejected", sourceEntry, null);
                        }
                     } catch (RuntimeException var105) {
                        retainRecovery(playerId, price, "refund-unknown", sourceEntry, var105);
                     }
                  }

                  try {
                     if (arrived) {
                        this.addPlayerToCooldown(player, destinationEntry);
                     }
                  } finally {
                     this.activeTravel.remove(playerId);
                  }
               }

               if (arrived) {
                  if (sourceEntry.getWormhole().getWormholeConfigFields().isBlindPlayer()) {
                     player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                  }

                  player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1.0F, 1.0F);
                  player.setFlying(false);
               }
            } else {
               this.activeTravel.remove(playerId);
               Logger.warn("Invalid wormhole price in " + sourceEntry.getWormhole().getWormholeConfigFields().getFilename());
            }
         }
      }
   }

   private static boolean atDestination(Player player, Location destination) {
      Location actual = player.getLocation();
      return actual.getWorld() == destination.getWorld() && actual.distanceSquared(destination) < 1.0E-6;
   }

   private static void retainRecovery(UUID playerId, double amount, String status, WormholeEntry source, RuntimeException failure) {
      String filename = source.getWormhole().getWormholeConfigFields().getFilename();
      unresolvedTravel.put(playerId, new WormholeManager.TravelRecovery(amount, status, filename));
      Player player = Bukkit.getPlayer(playerId);
      if (player != null) {
         Logger.sendMessage(player, "&cWormhole payment could not be settled. Contact an administrator before trying again.");
      }

      Logger.warn(
         "Wormhole payment requires manual reconciliation: player="
            + playerId
            + ", amount="
            + amount
            + ", status="
            + status
            + ", wormhole="
            + filename
            + (failure == null ? "" : ", failure=" + failure)
            + ". Further wormhole travel is blocked for this player; no automatic payment retry."
      );
   }

   private void sendDownloadHint(Player player) {
      player.spigot()
         .sendMessage(
            new BaseComponent[]{
               SpigotMessage.simpleMessage(CommandMessagesConfig.getWormholeOpDownloadMessage()),
               SpigotMessage.commandHoverMessage(InitializeConfig.getEmSetupDisplay(), InitializeConfig.getEmSetupHover(), "/em setup"),
               SpigotMessage.simpleMessage(" &8| "),
               SpigotMessage.hoverLinkMessage(InitializeConfig.getDiscordLinkDisplay(), InitializeConfig.getDiscordLinkHover(), "https://discord.gg/eSxvPbWYy4")
            }
         );
   }

   public void addPlayerToCooldown(Player player, @NonNull WormholeEntry destinationEntry) {
      if (destinationEntry == null) {
         throw new NullPointerException("destinationEntry is marked non-null but is null");
      } else {
         this.playerTeleportData.put(player.getUniqueId(), new WormholeManager.PlayerWormholeData(player, destinationEntry, System.currentTimeMillis()));
      }
   }

   public void addPlayerToCooldown(Player player, Location destination) {
      if (!this.activeTravel.contains(player.getUniqueId())) {
         WormholeEntry destinationEntry = null;

         for (WormholeEntry wormholeEntry : WormholeEntry.getWormholeEntries()) {
            try {
               if (wormholeEntry.getLocation() != null
                  && wormholeEntry.getLocation().getWorld() != null
                  && wormholeEntry.getLocation().getWorld().equals(destination.getWorld())
                  && destination.distanceSquared(wormholeEntry.getLocation())
                     <= Math.pow(1.5 * wormholeEntry.getWormhole().getWormholeConfigFields().getSizeMultiplier(), 2.0)) {
                  destinationEntry = wormholeEntry;
                  break;
               }
            } catch (IllegalArgumentException var7) {
            }
         }

         if (destinationEntry != null) {
            this.addPlayerToCooldown(player, destinationEntry);
         }
      }
   }

   public void shutdown() {
      if (this.wormholeTask != null) {
         this.wormholeTask.cancel();
         this.wormholeTask = null;
      }

      for (WormholeEntry wormholeEntry : WormholeEntry.getWormholeEntries()) {
         if (wormholeEntry != null) {
            wormholeEntry.stop();
         }
      }

      this.playerTeleportData.clear();
      instance = null;
      unresolvedTravel.forEach((playerId, recovery) -> Logger.warn("Unresolved wormhole payment at shutdown: " + playerId + " " + recovery));
   }

   private void startWormholeTask() {
      this.wormholeTask = (new BukkitRunnable() {
         public void run() {
            WormholeManager.this.processWormholes();
         }
      }).runTaskTimer(MetadataHandler.PLUGIN, 0L, 1L);
   }

   private void processWormholes() {
      this.tickCounter++;

      for (WormholeManager.PlayerWormholeData value : new ArrayList<>(this.playerTeleportData.values())) {
         value.tick();
      }

      Iterator var5 = new ArrayList<>(WormholeEntry.getWormholeEntries()).iterator();

      while (true) {
         WormholeEntry wormholeEntry;
         while (true) {
            if (!var5.hasNext()) {
               return;
            }

            wormholeEntry = (WormholeEntry)var5.next();
            if (wormholeEntry.getLocation() != null) {
               try {
                  if (wormholeEntry.getLocation().getWorld() != null && ChunkLocationChecker.chunkAtLocationIsLoaded(wormholeEntry.getLocation())) {
                     break;
                  }
               } catch (IllegalArgumentException var4) {
               }
            }
         }

         List<Player> nearbyPlayers = this.getNearbyPlayers(wormholeEntry);
         if (nearbyPlayers.isEmpty()) {
            wormholeEntry.onNoNearbyPlayers();
         } else {
            wormholeEntry.tick(nearbyPlayers);
            if (this.tickCounter % 5 == 0) {
               this.checkForTeleports(wormholeEntry, nearbyPlayers);
            }
         }
      }
   }

   @Generated
   public Map<UUID, WormholeManager.PlayerWormholeData> getPlayerTeleportData() {
      return this.playerTeleportData;
   }

   private class PlayerWormholeData {
      private final Player player;
      private final Location destination;
      private final long timeStamp;
      private final WormholeEntry wormholeEntry;
      private boolean hasLeftTeleportRadius = false;

      public PlayerWormholeData(Player player, WormholeEntry destinationWormhole, long timeStamp) {
         this.player = player;
         this.destination = destinationWormhole.getLocation().clone();
         this.timeStamp = timeStamp;
         this.wormholeEntry = destinationWormhole;
      }

      public boolean canTeleport() {
         return this.timeStamp + 5000L < System.currentTimeMillis() && this.isHasLeftTeleportRadius();
      }

      public void tick() {
         if (this.isHasLeftTeleportRadius() && this.enoughTimeHasPassed()) {
            WormholeManager.this.playerTeleportData.remove(this.player.getUniqueId(), this);
         }
      }

      private boolean enoughTimeHasPassed() {
         return this.timeStamp + 5000L < System.currentTimeMillis();
      }

      private boolean isHasLeftTeleportRadius() {
         if (this.hasLeftTeleportRadius) {
            return true;
         } else {
            try {
               if (!this.player.getWorld().equals(this.destination.getWorld())) {
                  return this.hasLeftTeleportRadius = true;
               }
            } catch (IllegalArgumentException var2) {
               return this.hasLeftTeleportRadius = true;
            }

            return this.destination.distanceSquared(this.player.getLocation())
                  > Math.pow(1.5 * this.wormholeEntry.getWormhole().getWormholeConfigFields().getSizeMultiplier() + 2.0, 2.0)
               ? (this.hasLeftTeleportRadius = true)
               : false;
         }
      }
   }

   private record TravelRecovery(double amount, String status, String wormhole) {
   }
}
