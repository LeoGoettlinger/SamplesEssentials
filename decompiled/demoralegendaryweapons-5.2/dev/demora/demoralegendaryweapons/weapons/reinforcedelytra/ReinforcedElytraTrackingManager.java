package dev.demora.demoralegendaryweapons.weapons.reinforcedelytra;

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

public class ReinforcedElytraTrackingManager {
   private final DemoraLegendaryWeapons plugin;
   private final ReinforcedElytra reinforcedElytra;
   private final Map<UUID, Boolean> wasGliding = new HashMap<>();
   private final Map<UUID, Double> maxGlidingSpeed = new HashMap<>();
   private final Set<UUID> landingProcessed = new HashSet<>();
   private final ReinforcedElytraExplosionManager explosionManager;
   private ReinforcedElytraBossBarManager bossBarManager;

   public ReinforcedElytraTrackingManager(
      DemoraLegendaryWeapons plugin,
      ReinforcedElytra reinforcedElytra,
      ReinforcedElytraExplosionManager explosionManager,
      ReinforcedElytraBossBarManager bossBarManager
   ) {
      this.plugin = plugin;
      this.reinforcedElytra = reinforcedElytra;
      this.explosionManager = explosionManager;
      this.bossBarManager = bossBarManager;
      this.startTrackingTask();
   }

   public void setBossBarManager(ReinforcedElytraBossBarManager bossBarManager) {
      this.bossBarManager = bossBarManager;
   }

   private void startTrackingTask() {
      (new BukkitRunnable() {
         public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
               ItemStack chest = player.getInventory().getChestplate();
               if (!ReinforcedElytraTrackingManager.this.reinforcedElytra.isReinforcedElytra(chest)) {
                  UUID uid = player.getUniqueId();
                  if (ReinforcedElytraTrackingManager.this.wasGliding.containsKey(uid)) {
                     ReinforcedElytraTrackingManager.this.wasGliding.remove(uid);
                     ReinforcedElytraTrackingManager.this.maxGlidingSpeed.remove(uid);
                     ReinforcedElytraTrackingManager.this.landingProcessed.remove(uid);
                  }
               } else {
                  boolean gliding = player.isGliding();
                  UUID uid = player.getUniqueId();
                  boolean prevGliding = ReinforcedElytraTrackingManager.this.wasGliding.getOrDefault(uid, false);
                  if (gliding) {
                     double speed = player.getVelocity().length();
                     double current = ReinforcedElytraTrackingManager.this.maxGlidingSpeed.getOrDefault(uid, 0.0);
                     if (speed > current) {
                        ReinforcedElytraTrackingManager.this.maxGlidingSpeed.put(uid, speed);
                     }

                     ReinforcedElytraTrackingManager.this.landingProcessed.remove(uid);
                  } else if (prevGliding) {
                     if (!ReinforcedElytraTrackingManager.this.landingProcessed.contains(uid)) {
                        double speed = ReinforcedElytraTrackingManager.this.maxGlidingSpeed.getOrDefault(uid, 0.0);
                        ReinforcedElytraTrackingManager.this.explosionManager.triggerExplosiveLanding(player, speed);
                        ReinforcedElytraTrackingManager.this.landingProcessed.add(uid);
                     }

                     ReinforcedElytraTrackingManager.this.maxGlidingSpeed.remove(uid);
                     if (ReinforcedElytraTrackingManager.this.bossBarManager != null) {
                        ReinforcedElytraTrackingManager.this.bossBarManager.hideBossBar(player);
                     }
                  }

                  ReinforcedElytraTrackingManager.this.wasGliding.put(uid, gliding);
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
