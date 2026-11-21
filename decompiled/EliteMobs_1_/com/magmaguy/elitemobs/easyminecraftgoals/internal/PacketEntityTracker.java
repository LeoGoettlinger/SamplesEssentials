package com.magmaguy.elitemobs.easyminecraftgoals.internal;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class PacketEntityTracker {
   private static PacketEntityTracker instance;
   private final Set<TrackedPacketEntity> trackedEntities = ConcurrentHashMap.newKeySet();
   private BukkitTask tickTask;
   private Plugin plugin;
   private long tickCounter = 0L;
   private static final int VISIBILITY_UPDATE_INTERVAL = 20;
   private static final int REMOUNT_SWEEP_INTERVAL = 40;
   private double trackingRangeSquared = 4096.0;

   private PacketEntityTracker() {
   }

   public static PacketEntityTracker getInstance() {
      if (instance == null) {
         instance = new PacketEntityTracker();
      }

      return instance;
   }

   public void initialize(Plugin plugin) {
      if (this.plugin == null) {
         this.plugin = plugin;
         this.updateTrackingRange();
         Bukkit.getPluginManager().registerEvents(new PacketEntityEventListener(this), plugin);
         this.tickTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
      }
   }

   public void shutdown() {
      if (this.tickTask != null) {
         this.tickTask.cancel();
         this.tickTask = null;
      }

      for (TrackedPacketEntity entity : this.trackedEntities) {
         for (UUID viewerUUID : entity.getCurrentViewers()) {
            Player viewer = Bukkit.getPlayer(viewerUUID);
            if (viewer != null) {
               entity.hideFromPlayer(viewer);
            }
         }
      }

      this.trackedEntities.clear();
      PacketPassengerRegistry.clear();
      this.plugin = null;
   }

   public void register(TrackedPacketEntity entity) {
      this.trackedEntities.add(entity);
   }

   public void unregister(TrackedPacketEntity entity) {
      this.trackedEntities.remove(entity);
   }

   private void tick() {
      this.tickCounter++;

      for (TrackedPacketEntity entity : this.trackedEntities) {
         entity.updateTracking();
      }

      if (this.tickCounter % 40L == 0L) {
         this.remountSweep();
      }

      if (this.tickCounter % 20L == 0L) {
         this.updateVisibility();
      }
   }

   private void remountSweep() {
      Iterator<TrackedPacketEntity> iterator = this.trackedEntities.iterator();

      while (iterator.hasNext()) {
         TrackedPacketEntity entity = iterator.next();
         if (!entity.isValid()) {
            iterator.remove();
         } else if (entity.getVehicle() != null && !entity.getCurrentViewers().isEmpty()) {
            entity.remount();
         }
      }
   }

   private void updateVisibility() {
      Iterator<TrackedPacketEntity> iterator = this.trackedEntities.iterator();

      while (iterator.hasNext()) {
         TrackedPacketEntity entity = iterator.next();
         if (!entity.isValid()) {
            iterator.remove();
         } else {
            Location entityLocation = entity.getTrackingLocation();
            if (entityLocation != null) {
               World entityWorld = entity.getWorld();
               if (entityWorld != null) {
                  for (Player player : Bukkit.getOnlinePlayers()) {
                     this.updateVisibilityForPlayer(entity, entityLocation, entityWorld, player);
                  }
               }
            }
         }
      }
   }

   private void updateVisibilityForPlayer(TrackedPacketEntity entity, Location entityLocation, World entityWorld, Player player) {
      boolean shouldBeVisible = false;
      if (player.getWorld().equals(entityWorld)) {
         double distanceSquared = player.getLocation().distanceSquared(entityLocation);
         shouldBeVisible = distanceSquared <= this.trackingRangeSquared && entity.canBeSeenBy(player);
      }

      boolean isCurrentlyVisible = entity.isVisibleTo(player);
      if (shouldBeVisible && !isCurrentlyVisible) {
         entity.showToPlayer(player);
      } else if (!shouldBeVisible && isCurrentlyVisible) {
         entity.hideFromPlayer(player);
      }
   }

   void onPlayerJoin(Player player) {
      if (this.plugin != null) {
         Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            if (player.isOnline()) {
               this.updateVisibilityForAllEntities(player);
            }
         }, 1L);
      }
   }

   void onPlayerQuit(Player player) {
      for (TrackedPacketEntity entity : this.trackedEntities) {
         if (entity.isVisibleTo(player)) {
            entity.hideFromPlayer(player);
         }
      }
   }

   void onPlayerRespawn(Player player) {
      if (this.plugin != null) {
         Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            if (player.isOnline()) {
               this.updateVisibilityForAllEntities(player);

               for (TrackedPacketEntity entity : this.trackedEntities) {
                  Entity vehicle = entity.getVehicle();
                  if (vehicle != null && vehicle.getUniqueId().equals(player.getUniqueId())) {
                     entity.remount();
                  }
               }
            }
         }, 1L);
      }
   }

   void onPlayerChangedWorld(Player player, World fromWorld) {
      World toWorld = player.getWorld();

      for (TrackedPacketEntity entity : this.trackedEntities) {
         World entityWorld = entity.getWorld();
         if (entityWorld != null && entityWorld.equals(fromWorld) && entity.isVisibleTo(player)) {
            entity.hideFromPlayer(player);
         }
      }

      for (TrackedPacketEntity entityx : this.trackedEntities) {
         Entity vehicle = entityx.getVehicle();
         if (vehicle != null && vehicle.getUniqueId().equals(player.getUniqueId())) {
            for (UUID viewerUUID : new HashSet<>(entityx.getCurrentViewers())) {
               Player viewer = Bukkit.getPlayer(viewerUUID);
               if (viewer != null) {
                  entityx.hideFromPlayer(viewer);
               }
            }
         }
      }

      if (this.plugin != null) {
         Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            if (player.isOnline()) {
               World newWorld = player.getWorld();

               for (TrackedPacketEntity entityxx : this.trackedEntities) {
                  World entityWorld = entityxx.getWorld();
                  if (entityWorld != null && entityWorld.equals(newWorld)) {
                     Location entityLocation = entityxx.getTrackingLocation();
                     if (entityLocation != null) {
                        this.updateVisibilityForPlayer(entityxx, entityLocation, entityWorld, player);
                     }
                  }
               }

               for (TrackedPacketEntity entityxxx : this.trackedEntities) {
                  Entity vehiclex = entityxxx.getVehicle();
                  if (vehiclex != null && vehiclex.getUniqueId().equals(player.getUniqueId())) {
                     for (Player otherPlayer : newWorld.getPlayers()) {
                        if (!entityxxx.isVisibleTo(otherPlayer)) {
                           Location entityLocation = entityxxx.getTrackingLocation();
                           if (entityLocation != null) {
                              this.updateVisibilityForPlayer(entityxxx, entityLocation, newWorld, otherPlayer);
                           }
                        }
                     }

                     entityxxx.remount();
                  }
               }
            }
         }, 1L);
      }
   }

   private void updateVisibilityForAllEntities(Player player) {
      for (TrackedPacketEntity entity : this.trackedEntities) {
         if (entity.isValid()) {
            Location entityLocation = entity.getTrackingLocation();
            if (entityLocation != null) {
               World entityWorld = entity.getWorld();
               if (entityWorld != null) {
                  this.updateVisibilityForPlayer(entity, entityLocation, entityWorld, player);
               }
            }
         }
      }
   }

   private void updateTrackingRange() {
      try {
         int range = Bukkit.getViewDistance() * 16;
         this.trackingRangeSquared = range * range;
      } catch (Exception var2) {
         this.trackingRangeSquared = 4096.0;
      }
   }

   public double getTrackingRange() {
      return Math.sqrt(this.trackingRangeSquared);
   }

   public void setTrackingRange(double range) {
      this.trackingRangeSquared = range * range;
   }
}
