package com.magmaguy.elitemobs.pathfinding;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.combatsystem.displays.LeashReturnDamageIndicator;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.easyminecraftgoals.events.WanderBackToPointEndEvent;
import com.magmaguy.elitemobs.easyminecraftgoals.events.WanderBackToPointStartEvent;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.AbstractWanderBackToPoint;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.util.AttributeManager;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.utils.EntityFinder;
import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Navigation implements Listener {
   private static final HashMap<CustomBossEntity, BukkitTask> currentlyNavigating = new HashMap<>();
   private static final HashMap<RegionalBossEntity, AbstractWanderBackToPoint> softLeashes = new HashMap<>();
   private static final HashMap<RegionalBossEntity, AbstractWanderBackToPoint> hardLeashes = new HashMap<>();
   private static final LeashReturnTracker activeLeashReturns = new LeashReturnTracker();

   public static void stopMoving(LivingEntity livingEntity) {
      NMSManager.getAdapter().doNotMove(livingEntity);
   }

   public static void addSoftLeashAI(RegionalBossEntity regionalBossEntity) {
      if (NMSManager.getAdapter() != null) {
         if (regionalBossEntity.getUnsyncedLivingEntity() == null || regionalBossEntity.getUnsyncedLivingEntity().getType() != EntityType.ENDER_DRAGON) {
            if (regionalBossEntity.getLivingEntity() instanceof Creature) {
               AbstractWanderBackToPoint leash = NMSManager.getAdapter()
                  .wanderBackToPoint(
                     regionalBossEntity.getLivingEntity(), regionalBossEntity.getSpawnLocation(), regionalBossEntity.getLeashRadius() / 2.0, 100
                  )
                  .setSpeed(1.2F)
                  .setStopReturnDistance(1)
                  .setGoalRefreshCooldownTicks(60)
                  .setHardObjective(false)
                  .setReturnDuringCombat(true)
                  .setTeleportOnFail(true)
                  .setStartWithCooldown(true);
               replaceLeash(softLeashes, regionalBossEntity, leash);
            }
         }
      }
   }

   public static void addHardLeashAI(RegionalBossEntity regionalBossEntity) {
      if (NMSManager.getAdapter() != null) {
         if (regionalBossEntity.getUnsyncedLivingEntity() == null || regionalBossEntity.getUnsyncedLivingEntity().getType() != EntityType.ENDER_DRAGON) {
            AbstractWanderBackToPoint leash = NMSManager.getAdapter()
               .wanderBackToPoint(regionalBossEntity.getLivingEntity(), regionalBossEntity.getSpawnLocation(), regionalBossEntity.getLeashRadius(), 100)
               .setSpeed(2.0F)
               .setStopReturnDistance(0)
               .setGoalRefreshCooldownTicks(60)
               .setHardObjective(true)
               .setTeleportOnFail(true)
               .setStartWithCooldown(true);
            replaceLeash(hardLeashes, regionalBossEntity, leash);
         }
      }
   }

   private static void replaceLeash(
      HashMap<RegionalBossEntity, AbstractWanderBackToPoint> leashes, RegionalBossEntity boss, AbstractWanderBackToPoint replacement
   ) {
      AbstractWanderBackToPoint previous = leashes.put(boss, replacement);
      if (previous != null) {
         previous.unregister();
      }

      replacement.register();
   }

   public static void updateLeashAnchor(RegionalBossEntity boss, Location location) {
      if (location != null && location.getWorld() != null) {
         AbstractWanderBackToPoint soft = softLeashes.get(boss);
         if (soft != null) {
            soft.setReturnLocation(location);
         }

         AbstractWanderBackToPoint hard = hardLeashes.get(boss);
         if (hard != null) {
            hard.setReturnLocation(location);
         }
      }
   }

   private static void removeLeashes(RegionalBossEntity boss) {
      AbstractWanderBackToPoint soft = softLeashes.remove(boss);
      if (soft != null) {
         soft.unregister();
      }

      AbstractWanderBackToPoint hard = hardLeashes.remove(boss);
      if (hard != null) {
         hard.unregister();
      }
   }

   public static void shutdown() {
      currentlyNavigating.values().forEach(BukkitTask::cancel);
      currentlyNavigating.clear();
      softLeashes.values().forEach(AbstractWanderBackToPoint::unregister);
      hardLeashes.values().forEach(AbstractWanderBackToPoint::unregister);
      softLeashes.clear();
      hardLeashes.clear();
      activeLeashReturns.clear();
      LeashReturnDamageIndicator.shutdown();
   }

   public static void navigateTo(final CustomBossEntity customBossEntity, final Double speed, final Location destination, final boolean force, int duration) {
      if (duration == 0) {
         duration = 100;
      }

      if (customBossEntity.getLivingEntity() != null) {
         if (destination != null && destination.getWorld() != null) {
            if (speed == null) {
               speed = AttributeManager.getAttributeBaseValue(customBossEntity.getLivingEntity(), "generic_movement_speed");
            }

            if (currentlyNavigating.get(customBossEntity) != null) {
               currentlyNavigating.get(customBossEntity).cancel();
            }

            final int finalDuration = duration;
            currentlyNavigating.put(
               customBossEntity,
               (new BukkitRunnable() {
                     int counter = 0;

                     public void run() {
                        if (this.counter >= finalDuration
                           || !customBossEntity.exists()
                           || customBossEntity.getLivingEntity() == null
                           || !customBossEntity.getLivingEntity().getWorld().equals(destination.getWorld())
                           || customBossEntity.getLivingEntity() != null
                              && customBossEntity.getLivingEntity().getLocation().distanceSquared(destination) < Math.pow(1.0, 2.0)) {
                           if (customBossEntity.exists() && this.counter >= finalDuration && force) {
                              customBossEntity.getLivingEntity().teleport(destination);
                           }

                           this.cancel();
                           Navigation.currentlyNavigating.remove(customBossEntity);
                        } else {
                           NMSManager.getAdapter().move(customBossEntity.getLivingEntity(), speed.floatValue(), destination);
                           this.counter++;
                        }
                     }
                  })
                  .runTaskTimer(MetadataHandler.PLUGIN, 0L, 1L)
            );
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void beginLeashReturn(WanderBackToPointStartEvent event) {
      if (event.getLivingEntity() != null) {
         if (event.getLivingEntity().getType() != EntityType.ENDER_DRAGON) {
            if (EntityTracker.getEliteMobEntity(event.getLivingEntity()) instanceof RegionalBossEntity regionalBossEntity) {
               activeLeashReturns.begin(event.getLivingEntity().getUniqueId(), event.isHardObjective());
               if (event.isHardObjective()) {
                  AttributeManager.setAttribute(
                     event.getLivingEntity(), "generic_follow_range", regionalBossEntity.getCustomBossesConfigFields().getLeashRadius() * 1.5
                  );
               }
            }
         }
      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void endLeashReturn(WanderBackToPointEndEvent event) {
      if (event.getLivingEntity() != null) {
         activeLeashReturns.end(event.getLivingEntity().getUniqueId(), event.isHardObjective());
         if (event.getLivingEntity().getType() != EntityType.ENDER_DRAGON) {
            EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getLivingEntity());
            if (eliteEntity != null && eliteEntity.getLivingEntity() != null) {
               if (eliteEntity instanceof RegionalBossEntity regionalBossEntity) {
                  if (event.isHardObjective()) {
                     if (regionalBossEntity.getCustomBossesConfigFields().getFollowDistance() != 0) {
                        AttributeManager.setAttribute(
                           event.getLivingEntity(), "generic_follow_range", regionalBossEntity.getCustomBossesConfigFields().getFollowDistance().intValue()
                        );
                     } else {
                        AttributeManager.setAttribute(
                           event.getLivingEntity(),
                           "generic_follow_range",
                           AttributeManager.getAttributeDefaultValue(regionalBossEntity.getLivingEntity(), "generic_follow_range")
                        );
                     }
                  }

                  if (MobCombatSettingsConfig.isRegenerateCustomBossHealthOnCombatEnd()) {
                     regionalBossEntity.fullHeal();
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void protectReturningBoss(EntityDamageEvent event) {
      if (!activeLeashReturns.isEmpty()) {
         if (activeLeashReturns.isReturning(event.getEntity().getUniqueId())) {
            if (EntityTracker.getEliteMobEntity(event.getEntity()) instanceof RegionalBossEntity regionalBossEntity) {
               event.setCancelled(true);
               if (event instanceof EntityDamageByEntityEvent damageByEntityEvent) {
                  if (EntityFinder.filterRangedDamagers(damageByEntityEvent.getDamager()) instanceof Player player) {
                     LeashReturnDamageIndicator.show(regionalBossEntity, player);
                  }
               }
            } else {
               activeLeashReturns.clear(event.getEntity().getUniqueId());
            }
         }
      }
   }

   @EventHandler
   public void clearLeashReturnOnDeath(EntityDeathEvent event) {
      activeLeashReturns.clear(event.getEntity().getUniqueId());
   }

   @EventHandler
   public void clearLeashReturnOnRemoval(EliteMobRemoveEvent event) {
      if (event.getEntity() != null) {
         activeLeashReturns.clear(event.getEntity().getUniqueId());
      }

      if (event.getEliteMobEntity() instanceof RegionalBossEntity regionalBossEntity) {
         removeLeashes(regionalBossEntity);
      }
   }
}
