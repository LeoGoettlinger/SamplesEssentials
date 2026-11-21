package com.magmaguy.elitemobs.advancedcombat.abilities;

import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.items.ItemTagger;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public final class ClassAbilityProjectileCarrier implements Listener, AutoCloseable {
   private static final byte MARKER_VALUE = 1;
   private static final double LAUNCH_ORIGIN_Y_OFFSET = -0.3;
   private static final Map<UUID, ClassAbilityProjectileCarrier> OWNERS = new ConcurrentHashMap<>();
   private static volatile NamespacedKey markerKey;
   private final Plugin plugin;
   private final ProjectileImpactLedger<ClassAbilityProjectileCarrier.Flight> flights = new ProjectileImpactLedger<>();
   private boolean closed;

   ClassAbilityProjectileCarrier(Plugin plugin) {
      this.plugin = Objects.requireNonNull(plugin, "plugin");
      markerKey = new NamespacedKey(plugin, "class_ability_projectile");
      plugin.getServer().getPluginManager().registerEvents(this, plugin);
   }

   boolean launch(
      final Player caster,
      Location destination,
      double maximumRange,
      double blocksPerTick,
      ClassAbilityProjectileCarrier.ImpactHandler impactHandler,
      final ClassAbilityProjectileCarrier.FlightRenderer renderer
   ) {
      Objects.requireNonNull(caster, "caster");
      Objects.requireNonNull(destination, "destination");
      Objects.requireNonNull(impactHandler, "impactHandler");
      Objects.requireNonNull(renderer, "renderer");
      if (!this.closed && caster.isOnline() && !caster.isDead()) {
         Location eyeLocation = caster.getEyeLocation().clone();
         Location plannedStart = launchOrigin(eyeLocation);
         if (plannedStart.getWorld() != null && destination.getWorld() != null && plannedStart.getWorld().equals(destination.getWorld())) {
            Vector delta = destination.toVector().subtract(plannedStart.toVector());
            if (delta.lengthSquared() < 1.0E-8) {
               return false;
            } else {
               final double safeRange = Math.max(1.0, maximumRange);
               double safeSpeed = Math.max(0.2, blocksPerTick);
               Vector velocity = delta.normalize().multiply(safeSpeed);
               final Arrow arrow = (Arrow)caster.launchProjectile(Arrow.class, velocity);

               final Location start;
               try {
                  Optional<Location> positionedStart = positionAtLaunchOrigin(arrow, eyeLocation);
                  if (positionedStart.isEmpty()) {
                     arrow.remove();
                     return false;
                  }

                  start = positionedStart.get();
                  this.configure(arrow, caster, velocity);
                  ItemTagger.clearArrowCombatData(arrow);
               } catch (Error | RuntimeException var21) {
                  arrow.remove();
                  throw var21;
               }

               if (!arrow.isValid()) {
                  arrow.remove();
                  return false;
               } else {
                  final ClassAbilityProjectileCarrier.Flight flight = new ClassAbilityProjectileCarrier.Flight(
                     arrow, caster, start, safeRange, Math.max(4, (int)Math.ceil(safeRange / safeSpeed) + 4), impactHandler, renderer
                  );
                  final UUID projectileId = arrow.getUniqueId();
                  if (!this.flights.register(projectileId, caster.getUniqueId(), flight)) {
                     arrow.remove();
                     return false;
                  } else {
                     OWNERS.put(projectileId, this);
                     flight.visualTask = (new BukkitRunnable() {
                           private int age;

                           public void run() {
                              this.age++;
                              if (!ClassAbilityProjectileCarrier.this.closed
                                 && caster.isOnline()
                                 && !caster.isDead()
                                 && arrow.isValid()
                                 && !arrow.isDead()
                                 && arrow.getWorld().equals(start.getWorld())
                                 && this.age <= flight.maximumTicks
                                 && !(arrow.getLocation().distanceSquared(start) > safeRange * safeRange)) {
                                 double progress = Math.min(1.0, Math.sqrt(arrow.getLocation().distanceSquared(start)) / safeRange);

                                 try {
                                    renderer.render(arrow.getLocation(), progress);
                                 } catch (Error | RuntimeException var4) {
                                    ClassAbilityProjectileCarrier.this.retire(projectileId);
                                    throw var4;
                                 }
                              } else {
                                 ClassAbilityProjectileCarrier.this.retire(projectileId);
                              }
                           }
                        })
                        .runTaskTimer(this.plugin, 1L, 1L);
                     return true;
                  }
               }
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   static Optional<Location> positionAtLaunchOrigin(Projectile carrier, Location eyeLocation) {
      Objects.requireNonNull(carrier, "carrier");
      Location start = launchOrigin(Objects.requireNonNull(eyeLocation, "eyeLocation"));
      return carrier.teleport(start) ? Optional.of(start) : Optional.empty();
   }

   private static Location launchOrigin(Location eyeLocation) {
      return eyeLocation.clone().add(0.0, -0.3, 0.0);
   }

   public static boolean isCarrier(Projectile projectile) {
      if (projectile == null) {
         return false;
      } else {
         NamespacedKey key = markerKey;
         return key != null && projectile.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
      }
   }

   public static boolean tryModeledImpact(Projectile projectile, Entity underlyingEntity) {
      if (!isCarrier(projectile)) {
         return false;
      } else {
         ClassAbilityProjectileCarrier owner = OWNERS.get(projectile.getUniqueId());
         return owner != null && owner.impact(projectile, underlyingEntity);
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = false
   )
   public void suppressVanillaCarrierDamage(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Projectile projectile && isCarrier(projectile)) {
         if (!CombatDamageContext.isClassAbilityDamageActive()) {
            event.setCancelled(true);
            this.impact(projectile, event.getEntity());
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = false
   )
   public void enforceSuppressedVanillaCarrierDamage(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Projectile projectile && isCarrier(projectile)) {
         if (!CombatDamageContext.isClassAbilityDamageActive()) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = false
   )
   public void retireCarrierOnProjectileHit(ProjectileHitEvent event) {
      Projectile projectile = event.getEntity();
      if (isCarrier(projectile)) {
         Entity hitEntity = event.getHitEntity();
         if (hitEntity == null) {
            projectile.getWorld().spawnParticle(Particle.SMOKE, projectile.getLocation(), 8, 0.12, 0.12, 0.12, 0.01);
            this.retire(projectile.getUniqueId());
         } else {
            this.impact(projectile, hitEntity);
         }
      }
   }

   void deactivate(Player caster) {
      for (ClassAbilityProjectileCarrier.Flight flight : this.flights.retireCaster(caster.getUniqueId())) {
         this.finish(flight);
      }
   }

   @Override
   public void close() {
      if (!this.closed) {
         this.closed = true;
         HandlerList.unregisterAll(this);

         for (ClassAbilityProjectileCarrier.Flight flight : this.flights.retireAll()) {
            this.finish(flight);
         }
      }
   }

   private void configure(Arrow arrow, Player caster, Vector velocity) {
      NamespacedKey key = markerKey;
      if (key == null) {
         throw new IllegalStateException("Projectile carrier marker is not initialized");
      } else {
         arrow.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte)1);
         arrow.setShooter(caster);
         arrow.setVelocity(velocity);
         arrow.setDamage(0.0);
         arrow.setKnockbackStrength(0);
         arrow.setCritical(false);
         arrow.setPierceLevel(0);
         arrow.setGravity(false);
         arrow.setSilent(true);
         arrow.setPersistent(false);
         arrow.setPickupStatus(PickupStatus.DISALLOWED);
         arrow.setVisibleByDefault(false);

         for (Player viewer : arrow.getWorld().getPlayers()) {
            viewer.hideEntity(this.plugin, arrow);
         }
      }
   }

   private boolean impact(Projectile projectile, Entity hitEntity) {
      ClassAbilityProjectileCarrier.Flight flight = this.flights.claim(projectile.getUniqueId()).orElse(null);
      if (flight == null) {
         if (isCarrier(projectile) && projectile.isValid()) {
            projectile.remove();
         }

         return false;
      } else {
         OWNERS.remove(projectile.getUniqueId(), this);
         boolean valid = hitEntity instanceof LivingEntity living
            && living.isValid()
            && !living.isDead()
            && flight.caster.isOnline()
            && !flight.caster.isDead()
            && living.getWorld().equals(flight.start.getWorld())
            && living.getWorld().equals(flight.caster.getWorld())
            && projectile.getLocation().distanceSquared(flight.start) <= flight.maximumRange * flight.maximumRange + 1.0;

         try {
            if (valid) {
               flight.impactHandler.impact(projectile, (LivingEntity)hitEntity);
            }
         } finally {
            this.finish(flight);
         }

         return valid;
      }
   }

   private void retire(UUID projectileId) {
      ClassAbilityProjectileCarrier.Flight flight = this.flights.retire(projectileId).orElse(null);
      if (flight != null) {
         OWNERS.remove(projectileId, this);
         this.finish(flight);
      }
   }

   private void finish(ClassAbilityProjectileCarrier.Flight flight) {
      OWNERS.remove(flight.arrow.getUniqueId(), this);
      BukkitTask visualTask = flight.visualTask;
      if (visualTask != null) {
         visualTask.cancel();
      }

      if (flight.arrow.isValid()) {
         flight.arrow.remove();
      }
   }

   private static final class Flight {
      private final Arrow arrow;
      private final Player caster;
      private final Location start;
      private final double maximumRange;
      private final int maximumTicks;
      private final ClassAbilityProjectileCarrier.ImpactHandler impactHandler;
      private final ClassAbilityProjectileCarrier.FlightRenderer renderer;
      private BukkitTask visualTask;

      private Flight(
         Arrow arrow,
         Player caster,
         Location start,
         double maximumRange,
         int maximumTicks,
         ClassAbilityProjectileCarrier.ImpactHandler impactHandler,
         ClassAbilityProjectileCarrier.FlightRenderer renderer
      ) {
         this.arrow = arrow;
         this.caster = caster;
         this.start = start.clone();
         this.maximumRange = maximumRange;
         this.maximumTicks = maximumTicks;
         this.impactHandler = impactHandler;
         this.renderer = renderer;
      }
   }

   @FunctionalInterface
   interface FlightRenderer {
      void render(Location var1, double var2);
   }

   @FunctionalInterface
   interface ImpactHandler {
      void impact(Projectile var1, LivingEntity var2);
   }
}
