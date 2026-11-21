package com.magmaguy.elitemobs.powers.specialpowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobExitCombatEvent;
import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.combatsystem.antiexploit.PreventMountExploit;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SpiritWalkSupport implements Listener {
   private static final Map<UUID, Integer> hitCounters = new ConcurrentHashMap<>();

   public static void handleBossDamaged(EliteEntity eliteEntity, DamageCause cause) {
      if (eliteEntity != null && cause != null) {
         switch (cause) {
            case FIRE:
            case FIRE_TICK:
            case POISON:
            case WITHER:
            case THORNS:
            case ENTITY_SWEEP_ATTACK:
               return;
            default:
               if (cause != DamageCause.DROWNING && cause != DamageCause.SUFFOCATION) {
                  int hits = hitCounters.getOrDefault(eliteEntity.getEliteUUID(), 0) + 1;
                  hitCounters.put(eliteEntity.getEliteUUID(), hits);
                  if (hits >= 9) {
                     hitCounters.remove(eliteEntity.getEliteUUID());
                     initializeSpiritWalk(eliteEntity);
                  }
               } else {
                  initializeSpiritWalk(eliteEntity);
               }
         }
      }
   }

   @EventHandler
   public void onEliteMobRemove(EliteMobRemoveEvent event) {
      hitCounters.remove(event.getEliteMobEntity().getEliteUUID());
   }

   public static void shutdown() {
      hitCounters.clear();
   }

   public static void spiritWalkAnimation(final EliteEntity eliteEntity, Location entityLocation, final Location finalLocation) {
      eliteEntity.setAIEnabled(false);
      eliteEntity.getLivingEntity().setInvulnerable(true);
      eliteEntity.getLivingEntity().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 1));
      final Vector toDestination = finalLocation.clone().subtract(entityLocation.clone()).toVector().normalize().divide(new Vector(2, 2, 2));
      eliteEntity.setCombatGracePeriod(400);
      (new BukkitRunnable() {
         int counter = 0;

         public void run() {
            if (!eliteEntity.isValid()) {
               this.cancel();
            } else if (!(eliteEntity.getLivingEntity().getLocation().clone().distance(finalLocation) < 2.0) && this.counter <= 200) {
               eliteEntity.getLivingEntity().teleport(eliteEntity.getLivingEntity().getLocation().clone().add(toDestination.clone()));
               this.counter++;
            } else {
               eliteEntity.getLivingEntity().teleport(finalLocation);
               eliteEntity.setAIEnabled(true);
               eliteEntity.getLivingEntity().setInvulnerable(false);
               eliteEntity.getLivingEntity().removePotionEffect(PotionEffectType.GLOWING);
               this.cancel();
            }
         }
      }).runTaskTimer(MetadataHandler.PLUGIN, 0L, 1L);
   }

   public static void spiritWalkRegionalBossAnimation(EliteEntity eliteEntity, Location entityLocation, Location finalLocation) {
      Bukkit.getScheduler()
         .runTask(
            MetadataHandler.PLUGIN,
            bukkitTask -> {
               if (eliteEntity.getLivingEntity() != null) {
                  eliteEntity.setAIEnabled(false);
                  eliteEntity.getLivingEntity().setInvulnerable(true);
                  final Vector toDestination = finalLocation.clone().subtract(entityLocation.clone()).toVector().normalize().divide(new Vector(2, 2, 2));
                  Entity vehicle = null;
                  if (eliteEntity.getLivingEntity().isInsideVehicle()) {
                     vehicle = eliteEntity.getLivingEntity().getVehicle();
                     if (vehicle instanceof LivingEntity livingVehicle) {
                        EliteEntity vehicleElite = EntityTracker.getEliteMobEntity(livingVehicle);
                        if (vehicleElite == null) {
                           livingVehicle.setAI(false);
                        } else {
                           vehicleElite.setAIEnabled(false);
                        }
                     }

                     vehicle.setInvulnerable(true);
                     if (((CustomBossEntity)eliteEntity).getPhaseBossEntity() != null) {
                        vehicle.remove();
                     }
                  }

                  (new BukkitRunnable() {
                        final Entity finalVehicle = eliteEntity.getLivingEntity().getVehicle();
                        int counter = 0;

                        public void run() {
                           if (!eliteEntity.isValid()) {
                              this.cancel();
                           } else {
                              if (eliteEntity.getLivingEntity().isInsideVehicle()) {
                                 eliteEntity.getLivingEntity().leaveVehicle();
                              }

                              if (!(eliteEntity.getLivingEntity().getLocation().clone().distance(finalLocation) < 2.0) && this.counter <= 200) {
                                 if (this.finalVehicle != null && !this.finalVehicle.isDead()) {
                                    this.finalVehicle.teleport(eliteEntity.getLivingEntity().getLocation().clone().add(toDestination.clone()));
                                 }

                                 eliteEntity.getLivingEntity().teleport(eliteEntity.getLivingEntity().getLocation().clone().add(toDestination.clone()));
                                 this.counter++;
                              } else {
                                 eliteEntity.setAIEnabled(true);
                                 eliteEntity.getLivingEntity().setInvulnerable(false);
                                 if (this.finalVehicle != null && !this.finalVehicle.isDead()) {
                                    this.finalVehicle.teleport(finalLocation);
                                 }

                                 eliteEntity.getLivingEntity().teleport(finalLocation);
                                 if (this.finalVehicle != null && !this.finalVehicle.isDead()) {
                                    if (this.finalVehicle instanceof LivingEntity livingVehiclex) {
                                       EliteEntity vehicleBoss = EntityTracker.getEliteMobEntity(this.finalVehicle);
                                       if (vehicleBoss != null) {
                                          vehicleBoss.setAIEnabled(true);
                                          Bukkit.getServer()
                                             .getPluginManager()
                                             .callEvent(new EliteMobExitCombatEvent(vehicleBoss, EliteMobExitCombatEvent.EliteMobExitCombatReason.SPIRIT_WALK));
                                       } else {
                                          livingVehiclex.setAI(true);
                                       }
                                    }

                                    this.finalVehicle.setInvulnerable(false);
                                    final LivingEntity returningBody = eliteEntity.getLivingEntity();
                                    (new BukkitRunnable() {
                                       public void run() {
                                          if (eliteEntity.getLivingEntity() == returningBody) {
                                             PreventMountExploit.addPassenger(finalVehicle, returningBody);
                                          }
                                       }
                                    }).runTaskLater(MetadataHandler.PLUGIN, 1L);
                                 }

                                 this.cancel();
                                 Bukkit.getServer()
                                    .getPluginManager()
                                    .callEvent(new EliteMobExitCombatEvent(eliteEntity, EliteMobExitCombatEvent.EliteMobExitCombatReason.SPIRIT_WALK));
                                 if (eliteEntity.getLivingEntity() instanceof Mob
                                    && ((Mob)eliteEntity.getLivingEntity()).getTarget() == null
                                    && eliteEntity instanceof RegionalBossEntity regionalBossEntity) {
                                    CustomBossEntity.CustomBossEntityEvents.slowRegionalBoss(regionalBossEntity);
                                 }
                              }
                           }
                        }
                     })
                     .runTaskTimer(MetadataHandler.PLUGIN, 0L, 1L);
               }
            }
         );
   }

   public static void initializeSpiritWalk(final EliteEntity eliteEntity) {
      (new BukkitRunnable() {
            int counter = 1;

            public void run() {
               if (this.counter <= 3 && eliteEntity.isValid()) {
                  Location bossLocation = eliteEntity.getLocation().clone();

                  for (int i = 0; i < 20; i++) {
                     double randomizedX = (ThreadLocalRandom.current().nextDouble() - 0.5) * 5.0;
                     double randomizedY = ThreadLocalRandom.current().nextDouble() * 5.0;
                     double randomizedZ = (ThreadLocalRandom.current().nextDouble() - 0.5) * 5.0;
                     Vector normalizedVector = new Vector(randomizedX, randomizedY, randomizedZ).normalize().multiply(7).multiply(this.counter);
                     Location newSimulatedLocation = bossLocation.clone().add(normalizedVector);
                     Location newValidLocation = SpiritWalkSupport.scanVertically(newSimulatedLocation);
                     if (newValidLocation != null) {
                        SpiritWalkSupport.spiritWalkAnimation(
                           eliteEntity, eliteEntity.getLivingEntity().getLocation(), newValidLocation.add(new Vector(0.5, 1.0, 0.5))
                        );
                        this.cancel();
                        break;
                     }
                  }

                  this.counter++;
               } else {
                  this.cancel();
               }
            }
         })
         .runTaskTimer(MetadataHandler.PLUGIN, 0L, 1L);
   }

   private static Location scanVertically(Location simulatedLocation) {
      int counter = 0;

      do {
         Location newLocation = simulatedLocation.clone().subtract(new Vector(0, counter, 0));
         if (locationValid(newLocation)) {
            return newLocation;
         }
      } while (++counter <= 8);

      return null;
   }

   private static boolean locationValid(Location feetLocation) {
      Location groundLocation = feetLocation.clone().add(new Vector(0, -1, 0));
      Location headLocation = feetLocation.clone().add(new Vector(0, 1, 0));
      return isNonVoidAir(feetLocation.getBlock().getType())
         && isNonVoidAir(headLocation.getBlock().getType())
         && groundLocation.getBlock().getType().isSolid();
   }

   private static boolean isNonVoidAir(Material material) {
      return material == Material.AIR || material == Material.CAVE_AIR;
   }
}
