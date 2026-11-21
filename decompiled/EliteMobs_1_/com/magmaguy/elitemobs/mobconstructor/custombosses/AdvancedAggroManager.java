package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public final class AdvancedAggroManager {
   private static final double TARGETING_RADIUS = 35.0;
   private static final double TARGETING_RADIUS_SQUARED = 1225.0;

   private AdvancedAggroManager() {
   }

   public static void updateTarget(EliteEntity eliteEntity) {
      if (eliteEntity != null) {
         if (eliteEntity.getLivingEntity() instanceof Mob mob) {
            Player forcedTarget = resolveForcedTarget(eliteEntity);
            if (forcedTarget != null) {
               if (mob.getTarget() == null || !mob.getTarget().getUniqueId().equals(forcedTarget.getUniqueId())) {
                  mob.setTarget(forcedTarget);
               }
            } else if (!eliteEntity.getAggro().isEmpty()) {
               Player highestThreatPlayer = null;
               double highestThreat = Double.NEGATIVE_INFINITY;

               for (Entry<Player, Double> entry : eliteEntity.getAggro().entrySet()) {
                  Player player = Bukkit.getPlayer(entry.getKey().getUniqueId());
                  Double threat = entry.getValue();
                  if (isEligibleTarget(mob, player) && threat != null && Double.isFinite(threat) && !(threat <= highestThreat)) {
                     highestThreatPlayer = player;
                     highestThreat = threat;
                  }
               }

               if (highestThreatPlayer != null) {
                  if (mob.getTarget() == null || !mob.getTarget().getUniqueId().equals(highestThreatPlayer.getUniqueId())) {
                     mob.setTarget(highestThreatPlayer);
                  }
               }
            }
         }
      }
   }

   public static boolean enforceForcedTarget(EliteEntity eliteEntity, EntityTargetLivingEntityEvent event) {
      Player forcedTarget = resolveForcedTarget(eliteEntity);
      if (forcedTarget == null) {
         return false;
      } else {
         if (event.getTarget() == null || !event.getTarget().getUniqueId().equals(forcedTarget.getUniqueId())) {
            event.setTarget(forcedTarget);
         }

         return true;
      }
   }

   private static Player resolveForcedTarget(EliteEntity eliteEntity) {
      if (eliteEntity.getLivingEntity() instanceof Mob mob) {
         UUID targetId = eliteEntity.getForcedTargetPlayerId();
         if (targetId == null) {
            return null;
         } else {
            Player player = Bukkit.getPlayer(targetId);
            if (isEligibleTarget(mob, player)) {
               return player;
            } else {
               eliteEntity.clearForcedTarget(targetId);
               return null;
            }
         }
      } else {
         return null;
      }
   }

   private static boolean isEligibleTarget(Mob mob, Player player) {
      if (player != null && player.isOnline() && !player.isDead() && player.isValid()) {
         if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) {
            return false;
         } else {
            return !player.getWorld().equals(mob.getWorld()) ? false : player.getLocation().distanceSquared(mob.getLocation()) <= 1225.0;
         }
      } else {
         return false;
      }
   }
}
