package com.magmaguy.elitemobs.testing;

import com.magmaguy.elitemobs.combatsystem.DamageBreakdown;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

final class CombatBreakdownDiagnostics {
   private final CombatSimulator simulator;
   private final Player player;

   CombatBreakdownDiagnostics(CombatSimulator simulator, Player player) {
      this.simulator = simulator;
      this.player = player;
   }

   DamageBreakdown simulateMeleeAttack(String skillId) {
      LivingEntity target = this.simulator.getDummyEntity(skillId);
      if (target != null && target.isValid()) {
         DamageBreakdown.startTracking(this.player);
         NMSManager.getAdapter().setDamageCooldownTicks(target, 0);
         this.player.attack(target);
         DamageBreakdown result = DamageBreakdown.stopTracking(this.player);
         if (result != null) {
            result.compute();
         }

         return result;
      } else {
         return null;
      }
   }

   String simulateMultipleAttacks(String skillId, int hitCount) {
      LivingEntity target = this.simulator.getDummyEntity(skillId);
      if (target != null && target.isValid()) {
         EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(target);
         if (eliteEntity == null) {
            return "§cTarget is not an elite entity!";
         } else {
            StringBuilder report = new StringBuilder();
            report.append("§6=== COMBAT SIMULATION REPORT ===\n");
            report.append(String.format("§7Target: §f%s §7(Lv %d, HP: %.0f)\n", target.getName(), eliteEntity.getLevel(), target.getMaxHealth()));
            report.append(String.format("§7Hits simulated: §f%d\n\n", hitCount));
            double totalDamage = 0.0;
            double minDamage = Double.MAX_VALUE;
            double maxDamage = 0.0;
            int completedHits = 0;
            int crits = 0;
            DamageBreakdown sampleBreakdown = null;

            for (int index = 0; index < hitCount; index++) {
               if (!target.isValid() || target.isDead() || target.getHealth() <= 0.0) {
                  this.simulator.healDummy(skillId);
                  target = this.simulator.getDummyEntity(skillId);
                  if (target == null) {
                     break;
                  }
               }

               DamageBreakdown breakdown = this.simulateMeleeAttack(skillId);
               if (breakdown != null) {
                  double damage = breakdown.getFinalDamage();
                  totalDamage += damage;
                  minDamage = Math.min(minDamage, damage);
                  maxDamage = Math.max(maxDamage, damage);
                  completedHits++;
                  if (breakdown.isCriticalHit()) {
                     crits++;
                  }

                  if (sampleBreakdown == null && !breakdown.isCriticalHit()) {
                     sampleBreakdown = breakdown;
                  }
               }

               this.simulator.healDummy(skillId);
            }

            double averageDamage = completedHits > 0 ? totalDamage / completedHits : 0.0;
            double critRate = completedHits > 0 ? (double)crits / completedHits * 100.0 : 0.0;
            double hitsToKill = averageDamage > 0.0 ? target.getMaxHealth() / averageDamage : 0.0;
            report.append("§6--- DAMAGE STATISTICS ---\n");
            report.append(String.format("§7Average Damage: §f%.1f\n", averageDamage));
            report.append(String.format("§7Min/Max: §f%.1f §7/ §f%.1f\n", completedHits > 0 ? minDamage : 0.0, maxDamage));
            report.append(String.format("§7Crit Rate: §f%.1f%% §7(%d/%d)\n", critRate, crits, completedHits));
            report.append(String.format("§7Estimated Hits to Kill: §f%.1f\n", hitsToKill));
            report.append(String.format("§7Total DPS (1 hit/s): §f%.1f\n\n", averageDamage));
            if (sampleBreakdown != null) {
               report.append("§6--- SAMPLE HIT BREAKDOWN ---\n");
               report.append(sampleBreakdown.toFormattedString());
            }

            return report.toString();
         }
      } else {
         return "§cNo valid target found!";
      }
   }

   void sendToPlayer(DamageBreakdown breakdown) {
      if (breakdown == null) {
         Logger.sendMessage(this.player, "§cNo damage breakdown available.");
      } else {
         for (String line : breakdown.toFormattedString().split("\n")) {
            Logger.sendMessage(this.player, line);
         }
      }
   }

   void runQuickDamageTest(int dummyLevel) {
      String dummyConfig = "training_dummy_lv" + dummyLevel + ".yml";
      CustomBossesConfigFields config = CustomBossesConfig.getCustomBoss(dummyConfig);
      if (config == null) {
         Logger.sendMessage(this.player, "§cDummy config not found: " + dummyConfig);
      } else {
         Vector direction = this.player.getLocation().getDirection();
         direction.setY(0).normalize();
         Location spawnLocation = this.player.getLocation().add(direction.multiply(5));
         int highestY = spawnLocation.getWorld().getHighestBlockYAt(spawnLocation);
         spawnLocation.setY(highestY + 1);
         spawnLocation.setDirection(this.player.getLocation().toVector().subtract(spawnLocation.toVector()).normalize());
         CustomBossEntity dummy = RegionalBossEntity.createTemporaryRegionalBossEntity(dummyConfig, spawnLocation);
         if (dummy == null) {
            Logger.sendMessage(this.player, "§cFailed to spawn dummy!");
         } else {
            dummy.spawn(true);
            if (dummy.getLivingEntity() == null) {
               Logger.sendMessage(this.player, "§cDummy entity is null!");
            } else {
               LivingEntity entity = dummy.getLivingEntity();
               entity.setMaximumNoDamageTicks(0);
               NMSManager.getAdapter().setDamageCooldownTicks(entity, 0);
               entity.setCustomName("§eDamage Test Dummy §7(Lv " + dummyLevel + ")");
               entity.setCustomNameVisible(true);
               entity.setGlowing(true);
               Logger.sendMessage(this.player, "§aSpawned Lv" + dummyLevel + " test dummy. Attack it to see damage breakdown!");
               Logger.sendMessage(this.player, "§7Dummy HP: §f" + String.format("%.0f", entity.getMaxHealth()));
               DamageBreakdown.startTracking(this.player);
            }
         }
      }
   }

   boolean isTracking() {
      return DamageBreakdown.isTracking(this.player);
   }

   DamageBreakdown stopTracking() {
      return DamageBreakdown.stopTracking(this.player);
   }
}
