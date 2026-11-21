package com.magmaguy.elitemobs.mobspawning;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.ValidWorldsConfig;
import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.items.EliteEnchantmentCatalog;
import com.magmaguy.elitemobs.magmacore.util.AttributeManager;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.EliteMindServiceModule;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.peacebanner.PeaceBannerManager;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardFlagChecker;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardSpawnEventBypasser;
import com.magmaguy.elitemobs.utils.PlayerScanner;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class NaturalMobSpawnEventHandler implements Listener {
   public static int getNaturalMobLevel(Location spawnLocation, List<Player> nearbyPlayers) {
      return getNaturalMobLevel(spawnLocation, nearbyPlayers, SpawnReason.NATURAL);
   }

   public static int getNaturalMobLevel(Location spawnLocation, List<Player> nearbyPlayers, SpawnReason spawnReason) {
      if (spawnReason == SpawnReason.NATURAL
         && spawnLocation.getWorld() != null
         && MobCombatSettingsConfig.isUseDistanceBasedNaturalEliteLevelsForWorld(spawnLocation.getWorld().getName())) {
         return nearbyPlayers.isEmpty() ? 0 : SpawnRadiusDifficultyIncrementer.distanceFromSpawnLevel(spawnLocation);
      } else {
         int eliteMobLevel = 1;
         int playerCount = 0;

         for (Player player : nearbyPlayers) {
            ElitePlayerInventory inventory = ElitePlayerInventory.playerInventories.get(player.getUniqueId());
            if (inventory != null) {
               int individualPlayerThreat = inventory.getNaturalMobSpawnLevel(true);
               playerCount++;
               if (individualPlayerThreat > eliteMobLevel) {
                  eliteMobLevel = individualPlayerThreat;
               }
            }
         }

         eliteMobLevel = (int)(eliteMobLevel + playerCount * 2 * 1.0);
         if (playerCount != 0 && eliteMobLevel >= 1) {
            int wiggle = ThreadLocalRandom.current().nextInt(5) - 2;
            return Math.max(wiggle + eliteMobLevel, 1);
         } else {
            return 0;
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onSpawn(CreatureSpawnEvent event) {
      if (!NMSManager.isEnabled() || !NMSManager.getAdapter().isMindBody(event.getEntity())) {
         if (!WorldGuardSpawnEventBypasser.isForcedSpawn()) {
            if (!EntityTracker.isEliteMob(event.getEntity())) {
               if (!EntityTracker.isNPCEntity(event.getEntity()) && !EntityTracker.isVisualEffect(event.getEntity())) {
                  if (!(event.getEntity() instanceof Tameable tameable && tameable.isTamed())) {
                     if (event.getSpawnReason() != SpawnReason.CUSTOM) {
                        if (event.getEntity().getCustomName() == null || !DefaultConfig.isPreventEliteMobConversionOfNamedMobs()) {
                           boolean providerEligible = EliteNaturalSpawnReasonPolicy.isProviderEligible(event.getSpawnReason())
                              && EliteMindServiceModule.hasNaturalSpawnProviders();
                           if (providerEligible || genericEligible(event)) {
                              LivingEntity livingEntity = event.getEntity();
                              List<Player> nearbyPlayers = PlayerScanner.getNearbyPlayers(livingEntity.getLocation());
                              int eliteMobLevel = getNaturalMobLevel(livingEntity.getLocation(), nearbyPlayers, event.getSpawnReason());
                              if (EliteMobs.worldGuardIsEnabled) {
                                 Integer minLevel = WorldGuardFlagChecker.getRegionMinimumLevel(livingEntity.getLocation());
                                 Integer maxLevel = WorldGuardFlagChecker.getRegionMaximumLevel(livingEntity.getLocation());
                                 if (minLevel != null) {
                                    eliteMobLevel = minLevel > eliteMobLevel ? minLevel : eliteMobLevel;
                                 }

                                 if (maxLevel != null) {
                                    eliteMobLevel = maxLevel < eliteMobLevel ? maxLevel : eliteMobLevel;
                                 }
                              }

                              if (providerEligible) {
                                 try {
                                    int providerLevel = Math.max(0, Math.min(eliteMobLevel, Math.max(0, MobCombatSettingsConfig.getNaturalEliteMobLevelCap())));
                                    if (EliteMindServiceModule.replaceNaturalSpawn(event, providerLevel)) {
                                       event.setCancelled(true);
                                       return;
                                    }
                                 } catch (Error | RuntimeException var11) {
                                    event.setCancelled(true);
                                    Logger.warn(
                                       "Environment-owned Mind replacement failed closed for " + event.getEntityType().getKey() + ": " + var11.getMessage()
                                    );
                                    return;
                                 }
                              }

                              if (eliteMobLevel >= 0) {
                                 if (eliteMobLevel > MobCombatSettingsConfig.getNaturalEliteMobLevelCap()) {
                                    eliteMobLevel = MobCombatSettingsConfig.getNaturalEliteMobLevelCap();
                                 }

                                 if (!providerEligible || genericEligible(event)) {
                                    double validChance = MobCombatSettingsConfig.getAggressiveMobConversionPercentage()
                                       + EliteEnchantmentCatalog.huntingGearBonus(nearbyPlayers);
                                    if (!(ThreadLocalRandom.current().nextDouble() >= validChance)) {
                                       EliteEntity eliteEntity;
                                       if (EliteMobProperties.getPluginData(event.getEntityType()).getBehavior() != null) {
                                          try {
                                             eliteEntity = EliteMindServiceModule.spawnBehaviorElite(
                                                event.getLocation(), event.getEntityType(), eliteMobLevel, event.getSpawnReason(), null
                                             );
                                             event.setCancelled(true);
                                             livingEntity = eliteEntity.getLivingEntity();
                                          } catch (RuntimeException var10) {
                                             Logger.warn("Could not apply elite behavior to " + event.getEntityType() + ": " + var10.getMessage());
                                             return;
                                          }
                                       } else {
                                          eliteEntity = new EliteEntity(livingEntity, eliteMobLevel, event.getSpawnReason());
                                       }

                                       if (eliteEntity.getLivingEntity() != null) {
                                          if (DefaultConfig.isUseRandomizedScalingForElites()) {
                                             AttributeManager.setAttribute(livingEntity, "generic_scale", ThreadLocalRandom.current().nextDouble(0.8, 1.2));
                                          }

                                          if (event.getSpawnReason().equals(SpawnReason.SPAWNER)) {
                                             eliteEntity.setEliteLoot(false);
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean genericEligible(CreatureSpawnEvent event) {
      if (event.getSpawnReason().equals(SpawnReason.DROWNED) || event.getSpawnReason().equals(SpawnReason.BREEDING)) {
         return false;
      } else if (EliteMobs.worldGuardIsEnabled && !WorldGuardFlagChecker.checkFlag(event.getLocation(), WorldGuardCompatibility.getELITEMOBS_SPAWN_FLAG())) {
         return false;
      } else if (!MobCombatSettingsConfig.isDoNaturalMobSpawning()) {
         return false;
      } else if (!ValidWorldsConfig.getInstance().getFileConfiguration().getBoolean("validWorlds." + event.getEntity().getWorld().getName())) {
         return false;
      } else if ((!event.getSpawnReason().equals(SpawnReason.SPAWNER) || MobCombatSettingsConfig.isDoSpawnersSpawnEliteMobs())
         && (event.getSpawnReason() == SpawnReason.NATURAL || !DefaultConfig.isDoStrictSpawningRules())) {
         return PeaceBannerManager.isProtected(event.getLocation())
            ? false
            : !event.getEntity().getType().equals(EntityType.BEE)
               && !event.getEntity().getType().equals(EntityType.VEX)
               && MobPropertiesConfig.getMobProperties().get(event.getEntityType()) != null
               && MobPropertiesConfig.getMobProperties().get(event.getEntityType()).isEnabled()
               && EliteMobProperties.isValidEliteMobType(event.getEntityType());
      } else {
         return false;
      }
   }
}
