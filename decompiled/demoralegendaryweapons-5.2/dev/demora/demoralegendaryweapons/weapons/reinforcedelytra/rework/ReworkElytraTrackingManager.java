package dev.demora.demoralegendaryweapons.weapons.reinforcedelytra.rework;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class ReworkElytraTrackingManager {
   private final DemoraLegendaryWeapons plugin;
   private final ReworkElytra reworkElytra;
   private final Map<UUID, Boolean> wasGliding = new HashMap<>();
   private final Map<UUID, Double> maxGlidingSpeed = new HashMap<>();
   private final Set<UUID> landingProcessed = new HashSet<>();
   private final ReworkElytraExplosionManager explosionManager;
   private ReworkElytraBossBarManager bossBarManager;

   public ReworkElytraTrackingManager(
      DemoraLegendaryWeapons plugin, ReworkElytra reworkElytra, ReworkElytraExplosionManager explosionManager, ReworkElytraBossBarManager bossBarManager
   ) {
      this.plugin = plugin;
      this.reworkElytra = reworkElytra;
      this.explosionManager = explosionManager;
      this.bossBarManager = bossBarManager;
      this.startTrackingTask();
   }

   public void setBossBarManager(ReworkElytraBossBarManager bossBarManager) {
      this.bossBarManager = bossBarManager;
   }

   private void startTrackingTask() {
      (new BukkitRunnable() {
         public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
               ItemStack chest = player.getInventory().getChestplate();
               if (!ReworkElytraTrackingManager.this.reworkElytra.isReworkElytra(chest)) {
                  UUID uid = player.getUniqueId();
                  if (ReworkElytraTrackingManager.this.wasGliding.containsKey(uid)) {
                     ReworkElytraTrackingManager.this.wasGliding.remove(uid);
                     ReworkElytraTrackingManager.this.maxGlidingSpeed.remove(uid);
                     ReworkElytraTrackingManager.this.landingProcessed.remove(uid);
                  }
               } else {
                  boolean gliding = player.isGliding();
                  UUID uid = player.getUniqueId();
                  boolean prevGliding = ReworkElytraTrackingManager.this.wasGliding.getOrDefault(uid, false);
                  if (gliding) {
                     double speed = player.getVelocity().length();
                     double current = ReworkElytraTrackingManager.this.maxGlidingSpeed.getOrDefault(uid, 0.0);
                     if (speed > current) {
                        ReworkElytraTrackingManager.this.maxGlidingSpeed.put(uid, speed);
                     }

                     ReworkElytraTrackingManager.this.landingProcessed.remove(uid);
                  } else if (prevGliding) {
                     if (!ReworkElytraTrackingManager.this.landingProcessed.contains(uid)) {
                        double speed = ReworkElytraTrackingManager.this.maxGlidingSpeed.getOrDefault(uid, 0.0);
                        ReworkElytraTrackingManager.this.explosionManager.triggerExplosiveLanding(player, speed);
                        ReworkElytraTrackingManager.this.landingProcessed.add(uid);
                     }

                     ReworkElytraTrackingManager.this.maxGlidingSpeed.remove(uid);
                     if (ReworkElytraTrackingManager.this.bossBarManager != null) {
                        ReworkElytraTrackingManager.this.bossBarManager.hideBossBar(player);
                     }
                  }

                  ReworkElytraTrackingManager.this.wasGliding.put(uid, gliding);
               }
            }
         }
      }).runTaskTimer(this.plugin, 0L, 1L);
   }

   public double getMaxGlidingSpeed(UUID playerId) {
      return this.maxGlidingSpeed.getOrDefault(playerId, 0.0);
   }

   public void updateMaxGlidingSpeed(Player player) {
      UUID uid = player.getUniqueId();
      double currentSpeed = player.getVelocity().length();
      double maxSpeed = this.maxGlidingSpeed.getOrDefault(uid, currentSpeed);
      if (currentSpeed > maxSpeed) {
         this.maxGlidingSpeed.put(uid, currentSpeed);
      }
   }

   public void cleanupPlayer(UUID playerId) {
      this.wasGliding.remove(playerId);
      this.maxGlidingSpeed.remove(playerId);
      this.landingProcessed.remove(playerId);
   }
}
