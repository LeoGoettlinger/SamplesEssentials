package com.magmaguy.elitemobs.magmacore.projectiles;

import com.magmaguy.elitemobs.magmacore.util.EntityAimAssist;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.ItemDisplay;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class MagicProjectileEngine<C extends MagicProjectileEngine.Source> implements Listener, AutoCloseable {
   private static final double PROJECTILE_ORIGIN_DROP = 0.3;
   protected final Plugin plugin;
   private final MagicProjectileEngine.ImpactHandler<C> impactHandler;
   private final MagicImpactLedger<MagicProjectileEngine<C>.Flight> flights = new MagicImpactLedger<>();
   private BukkitTask tickTask;
   private long currentTick;
   private boolean closed;

   public MagicProjectileEngine(Plugin plugin, MagicProjectileEngine.ImpactHandler<C> impactHandler) {
      this.plugin = Objects.requireNonNull(plugin, "plugin");
      this.impactHandler = Objects.requireNonNull(impactHandler, "impactHandler");
   }

   public void start() {
      if (!this.closed && this.tickTask == null) {
         this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
         this.tickTask = this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, this::tick, 1L, 1L);
      } else {
         throw new IllegalStateException("Magic projectile engine cannot start");
      }
   }

   public long currentTick() {
      return this.currentTick;
   }

   public Optional<LivingEntity> acquireTarget(
      Player owner, double range, double assistDegrees, Predicate<LivingEntity> eligibility, ToIntFunction<LivingEntity> priority
   ) {
      return this.acquireTargets(owner, range, assistDegrees, eligibility, priority, 1).stream().findFirst();
   }

   public List<LivingEntity> acquireTargets(
      Player owner, double range, double assistDegrees, Predicate<LivingEntity> eligibility, ToIntFunction<LivingEntity> priority, int limit
   ) {
      List<LivingEntity> nearby = owner.getWorld()
         .getNearbyEntities(owner.getEyeLocation(), range, range, range)
         .stream()
         .filter(LivingEntity.class::isInstance)
         .map(LivingEntity.class::cast)
         .toList();
      return EntityAimAssist.select(owner, nearby, range, assistDegrees, eligibility, priority, Math.max(1, limit));
   }

   public boolean launchWand(C cast, LivingEntity target) {
      return this.launchWand(cast, target == null ? List.of() : List.of(target));
   }

   public boolean launchWand(C cast, List<LivingEntity> targets) {
      if (this.closed) {
         return false;
      } else {
         int launched = 0;
         int missileCount = cast.traits().missileCount();

         for (int missileIndex = 0; missileIndex < missileCount; missileIndex++) {
            LivingEntity target = targets.isEmpty() ? null : targets.get(missileIndex % targets.size());
            if (target != null && !validTarget(cast.owner(), target)) {
               target = null;
            }

            Location eye = cast.owner().getEyeLocation();
            Vector direction = target == null ? eye.getDirection() : center(target).toVector().subtract(eye.toVector());
            if (!(direction.lengthSquared() < 1.0E-9)) {
               Location start = eye.clone().add(direction.normalize().multiply(0.35)).subtract(0.0, 0.3, 0.0);
               Arrow marker = this.spawnMarker(cast.owner(), start, direction);
               if (marker != null) {
                  MagicProjectileEngine<C>.Flight flight = (MagicProjectileEngine<C>.Flight)(target == null
                     ? new MagicProjectileEngine.StraightWandFlight(marker, cast, start, direction)
                     : new MagicProjectileEngine.WandFlight(marker, cast, start, target, missileIndex, missileCount));
                  this.flights.register(marker.getUniqueId(), flight);
                  launched++;
               }
            }
         }

         return launched > 0;
      }
   }

   public boolean launchStraight(C cast, Location start, Vector direction) {
      if (!this.closed
         && cast.active()
         && start != null
         && cast.owner().getWorld().equals(start.getWorld())
         && direction != null
         && Double.isFinite(direction.lengthSquared())
         && !(direction.lengthSquared() < 1.0E-9)) {
         Arrow marker = this.spawnMarker(cast.owner(), start, direction);
         if (marker == null) {
            return false;
         } else {
            marker.setVelocity(direction.clone().normalize().multiply(cast.traits().projectileSpeed()));
            this.flights.register(marker.getUniqueId(), new MagicProjectileEngine.StraightWandFlight(marker, cast, start, direction));
            return true;
         }
      } else {
         return false;
      }
   }

   protected void renderStraightFlight(C cast, Location location) {
      location.getWorld().spawnParticle(Particle.WITCH, location, 3, 0.06, 0.06, 0.06, 0.01);
      location.getWorld().spawnParticle(Particle.END_ROD, location, 1, 0.0, 0.0, 0.0, 0.0);
   }

   public boolean launchStaff(C cast, Vector direction) {
      if (!this.closed && direction != null && !(direction.lengthSquared() < 1.0E-9)) {
         Vector normalized = direction.clone().normalize();
         Location start = cast.owner().getEyeLocation().add(normalized.clone().multiply(0.45)).subtract(0.0, 0.3, 0.0);
         Arrow marker = this.spawnMarker(cast.owner(), start, normalized);
         if (marker == null) {
            return false;
         } else {
            this.flights.register(marker.getUniqueId(), new MagicProjectileEngine.StaffFlight(marker, cast, start, normalized));
            return true;
         }
      } else {
         return false;
      }
   }

   protected ItemDisplay spawnFireballVisual(Arrow marker) {
      return null;
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = false
   )
   public void onNativeMarkerDamage(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Projectile projectile && MagicProjectileMarker.isMarked(projectile)) {
         event.setCancelled(true);
         MagicProjectileEngine<C>.Flight flight = this.flights.lookup(projectile.getUniqueId());
         if (flight != null) {
            LivingEntity target = event.getEntity() instanceof LivingEntity living ? living : null;
            this.resolve(flight, target, projectile.getLocation(), projectile.getVelocity());
         }
      }
   }

   public boolean resolveMarker(Projectile projectile, LivingEntity target) {
      MagicProjectileEngine<C>.Flight flight = this.flights.lookup(projectile.getUniqueId());
      if (flight == null) {
         return false;
      } else {
         this.resolve(flight, target, projectile.getLocation(), projectile.getVelocity());
         return true;
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = false
   )
   public void onMarkerHit(ProjectileHitEvent event) {
      if (MagicProjectileMarker.isMarked(event.getEntity())) {
         MagicProjectileEngine<C>.Flight flight = this.flights.lookup(event.getEntity().getUniqueId());
         if (flight != null) {
            LivingEntity target = event.getHitEntity() instanceof LivingEntity living ? living : null;
            this.resolve(flight, target, event.getEntity().getLocation(), event.getEntity().getVelocity());
         }
      }
   }

   private Arrow spawnMarker(Player owner, Location start, Vector initialDirection) {
      World world = start.getWorld();
      return world != null && loaded(start) ? (Arrow)world.spawn(start, Arrow.class, marker -> {
         marker.setShooter(owner);
         marker.setGravity(false);
         marker.setSilent(true);
         marker.setPersistent(false);
         marker.setCritical(false);
         marker.setDamage(0.0);
         marker.setKnockbackStrength(0);
         marker.setPierceLevel(0);
         marker.setPickupStatus(PickupStatus.DISALLOWED);
         marker.setVisibleByDefault(false);
         MagicProjectileMarker.mark(marker, this.plugin, target -> this.resolveMarker(marker, target));
         marker.setVelocity(initialDirection.clone().normalize().multiply(0.05));
      }) : null;
   }

   private void tick() {
      this.currentTick++;

      for (MagicProjectileEngine<C>.Flight flight : this.flights.snapshot()) {
         try {
            if (!flight.valid()) {
               this.finish(flight);
            } else if (!this.absorbed(flight, flight.previous, flight.marker.getLocation())) {
               flight.previous = flight.marker.getLocation().clone();
               flight.tick();
               if (this.flights.lookup(flight.marker.getUniqueId()) == flight) {
                  this.absorbed(flight, flight.marker.getLocation(), flight.marker.getLocation().add(flight.marker.getVelocity()));
               }
            }
         } catch (LinkageError | RuntimeException var4) {
            this.finish(flight);
            this.plugin.getLogger().warning("Magic projectile stopped after a flight error: " + var4.getMessage());
         }
      }
   }

   private void resolve(MagicProjectileEngine<C>.Flight flight, LivingEntity target, Location impact, Vector incoming) {
      if (!this.absorbed(flight, flight.previous, impact)) {
         if (this.flights.claim(flight.marker.getUniqueId(), flight)) {
            this.cleanup(flight);
            this.impactHandler.onImpact(flight.cast, target, impact.clone(), incoming.clone());
         }
      }
   }

   private boolean absorbed(MagicProjectileEngine<C>.Flight flight, Location from, Location to) {
      if (!this.isAbsorbed(flight.cast, from, to)) {
         return false;
      } else {
         this.finish(flight);
         return true;
      }
   }

   protected boolean isAbsorbed(C source, Location from, Location to) {
      return false;
   }

   private void finish(MagicProjectileEngine<C>.Flight flight) {
      if (this.flights.claim(flight.marker.getUniqueId(), flight)) {
         this.cleanup(flight);
      }
   }

   private void cleanup(MagicProjectileEngine<C>.Flight flight) {
      try {
         flight.cleanup();
      } catch (RuntimeException var11) {
         this.plugin.getLogger().warning("Magic projectile visual cleanup failed: " + var11.getMessage());
      } finally {
         try {
            flight.marker.remove();
         } catch (RuntimeException var10) {
            this.plugin.getLogger().warning("Magic projectile removal failed: " + var10.getMessage());
         }
      }
   }

   public void cancelAll() {
      for (MagicProjectileEngine<C>.Flight flight : this.flights.snapshot()) {
         this.finish(flight);
      }
   }

   public void cancel(C cast) {
      for (MagicProjectileEngine<C>.Flight flight : this.flights.snapshot()) {
         if (flight.cast == cast) {
            this.finish(flight);
         }
      }
   }

   @Override
   public void close() {
      if (!this.closed) {
         this.closed = true;
         HandlerList.unregisterAll(this);
         if (this.tickTask != null) {
            this.tickTask.cancel();
         }

         this.tickTask = null;
         this.cancelAll();
      }
   }

   public static boolean validTarget(Player owner, LivingEntity target) {
      return target != null && target != owner && target.isValid() && !target.isDead() && target.getHealth() > 0.0 && !(target instanceof ArmorStand);
   }

   public static Location center(LivingEntity entity) {
      return EntityAimAssist.center(entity);
   }

   public static boolean unobstructed(Location source, Location destination) {
      return EntityAimAssist.unobstructed(source, destination);
   }

   private static boolean loaded(Location location) {
      World world = location.getWorld();
      return world != null && world.isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
   }

   private static Vector cubic(Vector start, Vector first, Vector second, Vector end, double t) {
      double inverse = 1.0 - t;
      return start.clone()
         .multiply(inverse * inverse * inverse)
         .add(first.clone().multiply(3.0 * inverse * inverse * t))
         .add(second.clone().multiply(3.0 * inverse * t * t))
         .add(end.clone().multiply(t * t * t));
   }

   public static double arcSpreadOffset(double spreadDegrees, double distance, int missileIndex, int missileCount) {
      if (!(spreadDegrees <= 0.0) && !(distance <= 0.0) && missileCount > 1) {
         double position = missileCount == 2 ? (missileIndex == 0 ? -1.0 : 1.0) : -1.0 + 2.0 * missileIndex / (missileCount - 1.0);
         double clampedDegrees = Math.min(60.0, spreadDegrees);
         return Math.tan(Math.toRadians(clampedDegrees)) * distance * position;
      } else {
         return 0.0;
      }
   }

   private abstract class Flight {
      final Arrow marker;
      final C cast;
      final UUID worldId;
      Location previous;
      int elapsed;

      Flight(Arrow marker, C cast) {
         this.marker = marker;
         this.cast = cast;
         this.worldId = marker.getWorld().getUID();
         this.previous = marker.getLocation().clone();
      }

      boolean valid() {
         Player owner = this.cast.owner();
         return !MagicProjectileEngine.this.closed
            && this.cast.active()
            && this.marker.isValid()
            && owner.isOnline()
            && owner.isValid()
            && !owner.isDead()
            && owner.getWorld().getUID().equals(this.worldId)
            && MagicProjectileEngine.loaded(this.marker.getLocation());
      }

      void cleanup() {
      }

      abstract void tick();
   }

   @FunctionalInterface
   public interface ImpactHandler<T> {
      void onImpact(T var1, LivingEntity var2, Location var3, Vector var4);
   }

   public interface Source {
      Player owner();

      MagicProjectileEngine.Traits traits();

      default boolean active() {
         return true;
      }
   }

   private final class StaffFlight extends MagicProjectileEngine<C>.Flight {
      private static final double SPIRAL_RADIUS = 0.5;
      private static final double SPIRAL_RADIANS_PER_TICK = 0.45;
      private final Location start;
      private final Vector direction;
      private final Vector side;
      private final Vector vertical;
      private final ItemDisplay fireballVisual;

      StaffFlight(Arrow marker, C cast, Location start, Vector direction) {
         super(marker, cast);
         this.start = start.clone();
         this.direction = direction.clone();
         Vector sideAxis = direction.clone().crossProduct(new Vector(0.0, 1.0, 0.0));
         this.side = sideAxis.lengthSquared() < 1.0E-6 ? new Vector(1.0, 0.0, 0.0) : sideAxis.normalize();
         this.vertical = direction.clone().crossProduct(this.side).normalize();
         this.fireballVisual = MagicProjectileEngine.this.spawnFireballVisual(marker);
      }

      @Override
      void cleanup() {
         if (this.fireballVisual != null && this.fireballVisual.isValid()) {
            this.fireballVisual.remove();
         }
      }

      @Override
      void tick() {
         this.elapsed++;
         MagicProjectileEngine.Traits traits = this.cast.traits();
         double angle = this.elapsed * 0.45;
         double radius = 0.5 * Math.min(1.0, this.elapsed / 6.0);
         Vector desired = this.start
            .toVector()
            .add(this.direction.clone().multiply(traits.projectileSpeed() * this.elapsed))
            .add(this.side.clone().multiply(Math.cos(angle) * radius))
            .add(this.vertical.clone().multiply(Math.sin(angle) * radius));
         this.marker.setVelocity(desired.subtract(this.marker.getLocation().toVector()));
         Location location = this.marker.getLocation();
         if (this.fireballVisual != null && this.fireballVisual.isValid()) {
            this.fireballVisual.teleport(location);
         }

         this.marker.getWorld().spawnParticle(Particle.FLAME, location, 3, 0.12, 0.12, 0.12, 0.025);
         this.marker.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, location, 1, 0.07, 0.07, 0.07, 0.01);
         if (location.distanceSquared(this.start) >= traits.range() * traits.range() || this.elapsed >= traits.travelTicks()) {
            MagicProjectileEngine.this.resolve(this, null, location, this.direction);
         }
      }
   }

   private final class StraightWandFlight extends MagicProjectileEngine<C>.Flight {
      private final Location start;
      private final Vector direction;

      StraightWandFlight(Arrow marker, C cast, Location start, Vector direction) {
         super(marker, cast);
         this.start = start.clone();
         this.direction = direction.clone().normalize();
      }

      @Override
      void tick() {
         this.elapsed++;
         MagicProjectileEngine.Traits traits = this.cast.traits();
         this.marker.setVelocity(this.direction.clone().multiply(traits.projectileSpeed()));
         Location location = this.marker.getLocation();
         MagicProjectileEngine.this.renderStraightFlight(this.cast, location);
         if (location.distanceSquared(this.start) >= traits.range() * traits.range() || this.elapsed >= traits.travelTicks()) {
            this.marker.getWorld().spawnParticle(Particle.SMOKE, location, 5, 0.08, 0.08, 0.08, 0.01);
            MagicProjectileEngine.this.finish(this);
         }
      }
   }

   public interface Traits {
      int missileCount();

      double spreadDegrees();

      double projectileSpeed();

      double range();

      int travelTicks();
   }

   private final class WandFlight extends MagicProjectileEngine<C>.Flight {
      private final LivingEntity target;
      private final Vector start;
      private final Vector initialEnd;
      private final Vector firstControl;
      private final Vector secondControl;

      WandFlight(Arrow marker, C cast, Location startLocation, LivingEntity target, int missileIndex, int missileCount) {
         super(marker, cast);
         this.target = target;
         this.start = startLocation.toVector();
         this.initialEnd = MagicProjectileEngine.center(target).toVector();
         Vector forward = this.initialEnd.clone().subtract(this.start).normalize();
         Vector side = forward.clone().crossProduct(new Vector(0.0, 1.0, 0.0));
         if (side.lengthSquared() < 1.0E-6) {
            side = new Vector(1.0, 0.0, 0.0);
         } else {
            side.normalize();
         }

         ThreadLocalRandom random = ThreadLocalRandom.current();
         double sign = random.nextBoolean() ? 1.0 : -1.0;
         double pathSpread = MagicProjectileEngine.arcSpreadOffset(
            cast.traits().spreadDegrees(), this.start.distance(this.initialEnd), missileIndex, missileCount
         );
         this.firstControl = this.start
            .clone()
            .add(this.initialEnd.clone().subtract(this.start).multiply(0.32))
            .add(side.clone().multiply(sign * random.nextDouble(0.75, 1.75) + pathSpread))
            .add(new Vector(0.0, random.nextDouble(0.8, 1.9), 0.0));
         this.secondControl = this.start
            .clone()
            .add(this.initialEnd.clone().subtract(this.start).multiply(0.68))
            .add(side.multiply(-sign * random.nextDouble(0.15, 1.05) - pathSpread * 0.35))
            .add(new Vector(0.0, random.nextDouble(0.45, 1.5), 0.0));
      }

      @Override
      boolean valid() {
         return super.valid() && MagicProjectileEngine.validTarget(this.cast.owner(), this.target) && this.target.getWorld().getUID().equals(this.worldId);
      }

      @Override
      void tick() {
         this.elapsed++;
         MagicProjectileEngine.Traits traits = this.cast.traits();
         Location currentTarget = MagicProjectileEngine.center(this.target);
         if (currentTarget.distanceSquared(this.start.toLocation(this.marker.getWorld())) > Math.pow(traits.range() + 4.0, 2.0)) {
            MagicProjectileEngine.this.finish(this);
         } else {
            double t = Math.min(1.0, (double)this.elapsed / traits.travelTicks());
            Vector currentEnd = currentTarget.toVector();
            Vector shift = currentEnd.clone().subtract(this.initialEnd);
            Vector desired = MagicProjectileEngine.cubic(
               this.start, this.firstControl.clone().add(shift.clone().multiply(0.32)), this.secondControl.clone().add(shift.multiply(0.68)), currentEnd, t
            );
            Vector velocity = desired.subtract(this.marker.getLocation().toVector());
            if (velocity.length() > traits.projectileSpeed()) {
               velocity.normalize().multiply(traits.projectileSpeed());
            }

            this.marker.setVelocity(velocity);
            this.marker.getWorld().spawnParticle(Particle.WITCH, this.marker.getLocation(), 3, 0.06, 0.06, 0.06, 0.01);
            this.marker.getWorld().spawnParticle(Particle.END_ROD, this.marker.getLocation(), 1, 0.0, 0.0, 0.0, 0.0);
            if (this.elapsed >= traits.travelTicks() + 5) {
               Location markerLocation = this.marker.getLocation();
               this.marker.getWorld().spawnParticle(Particle.SMOKE, markerLocation, 5, 0.08, 0.08, 0.08, 0.01);
               MagicProjectileEngine.this.finish(this);
            }
         }
      }
   }
}
