package com.magmaguy.elitemobs.advancedcombat.abilities;

import com.magmaguy.elitemobs.advancedcombat.ClassAbilityEligibility;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Input;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

final class GuardianFlightManager implements Listener, AutoCloseable {
   private static final double ALLY_ARRIVAL_DISTANCE = 2.1;
   private static final double ANCHOR_ARRIVAL_DISTANCE = 1.15;
   private static final double RANGE_TOLERANCE = 2.0;
   private static final double MINIMUM_PROGRESS_PER_TICK = 0.025;
   private static final double COLLISION_LOOKAHEAD = 0.28;
   private final Map<UUID, GuardianFlightManager.Session> sessions = new LinkedHashMap<>();
   private final Logger logger;
   private final BukkitTask maintenanceTask;
   private boolean closed;

   GuardianFlightManager(Plugin plugin) {
      Objects.requireNonNull(plugin, "plugin");
      this.logger = plugin.getLogger();
      plugin.getServer().getPluginManager().registerEvents(this, plugin);
      this.maintenanceTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
   }

   boolean startToAlly(
      Player caster,
      Player ally,
      double maximumRange,
      int maximumTicks,
      BiPredicate<Player, Player> remainsAnAlly,
      GuardianFlightManager.FlightListener listener
   ) {
      Objects.requireNonNull(ally, "ally");
      Objects.requireNonNull(remainsAnAlly, "remainsAnAlly");
      return this.start(caster, new GuardianFlightManager.AllyDestination(ally, remainsAnAlly), maximumRange, maximumTicks, 2.1, listener);
   }

   boolean startToAnchor(Player caster, Location anchor, double maximumRange, int maximumTicks, GuardianFlightManager.FlightListener listener) {
      Objects.requireNonNull(anchor, "anchor");
      return this.start(caster, new GuardianFlightManager.AnchorDestination(anchor), maximumRange, maximumTicks, 1.15, listener);
   }

   private boolean start(
      Player caster,
      GuardianFlightManager.Destination destination,
      double maximumRange,
      int maximumTicks,
      double arrivalDistance,
      GuardianFlightManager.FlightListener listener
   ) {
      Objects.requireNonNull(caster, "caster");
      Objects.requireNonNull(destination, "destination");
      Objects.requireNonNull(listener, "listener");
      if (!this.closed && Double.isFinite(maximumRange) && !(maximumRange <= 0.0) && maximumTicks >= 1) {
         GuardianFlightManager.DestinationSnapshot target = destination.resolve(caster).orElse(null);
         if (casterValid(caster) && target != null && sameWorld(caster.getLocation(), target.location())) {
            double targetDistance = center(caster).distance(target.location());
            if (targetDistance > maximumRange + 2.0) {
               return false;
            } else {
               if (targetDistance > arrivalDistance) {
                  Vector initial = GuardianFlightTrajectory.steer(flightMomentum(caster), target.location().toVector().subtract(center(caster).toVector()));
                  if (!this.collisionClear(caster, initial)) {
                     return false;
                  }
               }

               GuardianFlightManager.Session previous = this.sessions.get(caster.getUniqueId());
               if (previous != null) {
                  this.terminate(previous, GuardianFlightManager.EndReason.REPLACED, null);
               }

               GuardianFlightManager.Session session = new GuardianFlightManager.Session(
                  caster, destination, maximumRange + 2.0, maximumTicks, arrivalDistance, listener
               );
               this.sessions.put(caster.getUniqueId(), session);
               caster.setFallDistance(0.0F);
               Location start = caster.getLocation();
               start.getWorld().spawnParticle(Particle.END_ROD, start.clone().add(0.0, 0.9, 0.0), 12, 0.3, 0.45, 0.3, 0.025);
               start.getWorld().playSound(start, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 0.6F, 1.45F);
               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   void cancel(Player caster) {
      Objects.requireNonNull(caster, "caster");
      GuardianFlightManager.Session session = this.sessions.get(caster.getUniqueId());
      if (session != null) {
         this.terminate(session, GuardianFlightManager.EndReason.CANCELLED, null);
      }
   }

   boolean active(Player caster) {
      return caster != null && this.sessions.containsKey(caster.getUniqueId());
   }

   private void tick() {
      for (GuardianFlightManager.Session session : List.copyOf(this.sessions.values())) {
         try {
            this.tick(session);
         } catch (RuntimeException var4) {
            this.logger.log(Level.WARNING, "Guardian Flight session failed safely", (Throwable)var4);
            this.terminate(session, GuardianFlightManager.EndReason.TARGET_INVALID, null);
         }
      }
   }

   private void tick(GuardianFlightManager.Session session) {
      if (this.sessions.get(session.caster().getUniqueId()) == session) {
         Player caster = session.caster();
         Location current = caster.getLocation();
         session.captureProgress(current);
         GuardianFlightManager.DestinationSnapshot target = session.destination().resolve(caster).orElse(null);
         double targetDistance = target != null && sameWorld(current, target.location()) ? center(caster).distance(target.location()) : Double.NaN;
         GuardianFlightSessionPolicy.Decision decision = GuardianFlightSessionPolicy.evaluate(
            new GuardianFlightSessionPolicy.Frame(
               casterValid(caster),
               target != null && sameWorld(current, target.location()),
               session.elapsedTicks(),
               session.maximumTicks(),
               targetDistance,
               session.maximumTargetDistance(),
               session.arrivalDistance(),
               false,
               session.stalledTicks()
            )
         );
         if (decision != GuardianFlightSessionPolicy.Decision.CONTINUE) {
            this.terminate(session, endReason(decision), null);
         } else {
            Vector displacement = target.location().toVector().subtract(center(caster).toVector());
            Vector velocity = GuardianFlightTrajectory.steer(flightMomentum(caster), displacement);
            if (!this.collisionClear(caster, velocity)) {
               this.terminate(session, GuardianFlightManager.EndReason.OBSTRUCTED, null);
            } else {
               Location predicted = current.clone().add(velocity);
               if (!loaded(predicted)) {
                  this.terminate(session, GuardianFlightManager.EndReason.OBSTRUCTED, null);
               } else {
                  caster.setVelocity(velocity);
                  caster.setFallDistance(0.0F);
                  Location trail = center(caster);
                  trail.getWorld().spawnParticle(Particle.END_ROD, trail, 2, 0.08, 0.12, 0.08, 0.01);
                  trail.getWorld().spawnParticle(Particle.CLOUD, trail, 1, 0.04, 0.06, 0.04, 0.005);
                  session.elapseTick();
               }
            }
         }
      }
   }

   private void terminate(GuardianFlightManager.Session session, GuardianFlightManager.EndReason reason, Vector exitVelocity) {
      if (this.sessions.remove(session.caster().getUniqueId(), session)) {
         Player caster = session.caster();
         if (caster.isValid()) {
            caster.setFallDistance(0.0F);
            if (exitVelocity != null) {
               caster.setVelocity(exitVelocity);
            }

            if (reason == GuardianFlightManager.EndReason.ARRIVED) {
               Location arrival = caster.getLocation();
               arrival.getWorld().spawnParticle(Particle.ENCHANT, arrival.clone().add(0.0, 0.9, 0.0), 18, 0.4, 0.65, 0.4, 0.06);
               arrival.getWorld().playSound(arrival, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.65F, 1.65F);
            }
         }

         Player ally = session.destination().ally().orElse(null);
         GuardianFlightManager.FlightResult result = new GuardianFlightManager.FlightResult(
            reason, caster, ally, session.start(), caster.getLocation(), session.travelledDistance()
         );

         try {
            session.listener().onEnd(result);
         } catch (RuntimeException var8) {
            this.logger.log(Level.WARNING, "Guardian Flight completion callback failed", (Throwable)var8);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onInput(PlayerInputEvent event) {
      GuardianFlightManager.Session session = this.sessions.get(event.getPlayer().getUniqueId());
      if (session != null) {
         GuardianFlightInputState.Intent intent = session.updateInput(event.getInput());
         switch (intent) {
            case FORWARD_CANCEL:
               this.terminate(
                  session,
                  GuardianFlightManager.EndReason.JUMP_CANCELLED,
                  GuardianFlightTrajectory.forwardCancel(event.getPlayer().getEyeLocation().getDirection(), session.travelledDistance())
               );
               break;
            case UPWARD_CANCEL:
               this.terminate(
                  session,
                  GuardianFlightManager.EndReason.SNEAK_CANCELLED,
                  GuardianFlightTrajectory.upwardCancel(event.getPlayer().getVelocity(), session.travelledDistance())
               );
            case NONE:
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = false
   )
   public void onFallDamage(EntityDamageEvent event) {
      if (event.getCause() == DamageCause.FALL && event.getEntity() instanceof Player player && this.sessions.containsKey(player.getUniqueId())) {
         event.setCancelled(true);
         player.setFallDistance(0.0F);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onQuit(PlayerQuitEvent event) {
      this.terminateForLifecycle(event.getPlayer(), GuardianFlightManager.EndReason.CASTER_INVALID);
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onDeath(PlayerDeathEvent event) {
      this.terminateForLifecycle(event.getEntity(), GuardianFlightManager.EndReason.CASTER_INVALID);
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onChangedWorld(PlayerChangedWorldEvent event) {
      this.terminateForLifecycle(event.getPlayer(), GuardianFlightManager.EndReason.TARGET_INVALID);
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onTeleport(PlayerTeleportEvent event) {
      this.terminateForLifecycle(event.getPlayer(), GuardianFlightManager.EndReason.TELEPORTED);
   }

   private void terminateForLifecycle(Player player, GuardianFlightManager.EndReason reason) {
      GuardianFlightManager.Session session = this.sessions.get(player.getUniqueId());
      if (session != null) {
         this.terminate(session, reason, null);
      }
   }

   private static Vector flightMomentum(Player caster) {
      Vector momentum = caster.getVelocity();
      if (caster.isOnGround() && momentum.getY() < 0.0) {
         momentum.setY(0.0);
      }

      return momentum;
   }

   private boolean collisionClear(Player caster, Vector velocity) {
      double speed = velocity.length();
      if (Double.isFinite(speed) && !(speed <= 1.0E-6)) {
         Vector direction = velocity.clone().multiply(1.0 / speed);
         double distance = speed + 0.28;
         Vector side = new Vector(-direction.getZ(), 0.0, direction.getX());
         if (side.lengthSquared() <= 1.0E-9) {
            side.setX(1.0);
         } else {
            side.normalize();
         }

         side.multiply(Math.max(0.22, caster.getWidth() * 0.45));
         double height = Math.max(1.0, caster.getHeight());
         Location base = caster.getLocation();
         List<Location> samples = List.of(
            base.clone().add(0.0, 0.12, 0.0),
            base.clone().add(0.0, height * 0.5, 0.0),
            base.clone().add(0.0, Math.max(0.25, height - 0.12), 0.0),
            base.clone().add(side).add(0.0, height * 0.5, 0.0),
            base.clone().subtract(side).add(0.0, height * 0.5, 0.0)
         );
         World world = caster.getWorld();
         BoundingBox footprint = caster.getBoundingBox().clone();
         Vector lookahead = direction.clone().multiply(distance);
         footprint.union(footprint.clone().shift(lookahead));

         for (Location sample : samples) {
            footprint.union(sample.toVector());
            footprint.union(sample.toVector().add(lookahead));
         }

         if (!SafeMovement.volumeAvailable(caster, world, footprint)) {
            return false;
         } else {
            for (Location sample : samples) {
               RayTraceResult collision = world.rayTraceBlocks(sample, direction, distance, FluidCollisionMode.NEVER, true);
               if (collision != null) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   private static GuardianFlightManager.EndReason endReason(GuardianFlightSessionPolicy.Decision decision) {
      return switch (decision) {
         case ARRIVED -> GuardianFlightManager.EndReason.ARRIVED;
         case OBSTRUCTED -> GuardianFlightManager.EndReason.OBSTRUCTED;
         case TARGET_INVALID -> GuardianFlightManager.EndReason.TARGET_INVALID;
         case CASTER_INVALID -> GuardianFlightManager.EndReason.CASTER_INVALID;
         case OUT_OF_RANGE -> GuardianFlightManager.EndReason.OUT_OF_RANGE;
         case EXPIRED -> GuardianFlightManager.EndReason.EXPIRED;
         case CONTINUE -> throw new IllegalArgumentException("A continuing flight cannot end");
      };
   }

   private static boolean casterValid(Player caster) {
      try {
         return caster.isOnline() && caster.isValid() && !caster.isDead() && ClassAbilityEligibility.isEligible(caster);
      } catch (RuntimeException var2) {
         return false;
      }
   }

   private static boolean sameWorld(Location first, Location second) {
      return first.getWorld() != null && second.getWorld() != null && first.getWorld().equals(second.getWorld());
   }

   private static Location center(Player player) {
      return player.getLocation().add(0.0, Math.max(0.45, player.getHeight() * 0.5), 0.0);
   }

   private static boolean loaded(Location location) {
      World world = location.getWorld();
      return world != null && world.isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
   }

   @Override
   public void close() {
      if (!this.closed) {
         this.closed = true;
         this.maintenanceTask.cancel();

         for (GuardianFlightManager.Session session : List.copyOf(this.sessions.values())) {
            this.terminate(session, GuardianFlightManager.EndReason.SHUTDOWN, null);
         }

         this.sessions.clear();
         HandlerList.unregisterAll(this);
      }
   }

   private static final class AllyDestination implements GuardianFlightManager.Destination {
      private final Player ally;
      private final BiPredicate<Player, Player> validator;

      private AllyDestination(Player ally, BiPredicate<Player, Player> validator) {
         this.ally = ally;
         this.validator = validator;
      }

      @Override
      public Optional<GuardianFlightManager.DestinationSnapshot> resolve(Player caster) {
         if (!this.ally.getUniqueId().equals(caster.getUniqueId())
            && this.ally.isOnline()
            && this.ally.isValid()
            && !this.ally.isDead()
            && this.ally.getWorld().equals(caster.getWorld())) {
            boolean valid;
            try {
               valid = this.validator.test(caster, this.ally);
            } catch (RuntimeException var4) {
               valid = false;
            }

            return valid ? Optional.of(new GuardianFlightManager.DestinationSnapshot(GuardianFlightManager.center(this.ally))) : Optional.empty();
         } else {
            return Optional.empty();
         }
      }

      @Override
      public Optional<Player> ally() {
         return Optional.of(this.ally);
      }
   }

   private static final class AnchorDestination implements GuardianFlightManager.Destination {
      private final Location anchor;

      private AnchorDestination(Location anchor) {
         this.anchor = anchor.clone();
      }

      @Override
      public Optional<GuardianFlightManager.DestinationSnapshot> resolve(Player caster) {
         return GuardianFlightManager.sameWorld(caster.getLocation(), this.anchor) && GuardianFlightManager.loaded(this.anchor)
            ? Optional.of(new GuardianFlightManager.DestinationSnapshot(this.anchor))
            : Optional.empty();
      }
   }

   private interface Destination {
      Optional<GuardianFlightManager.DestinationSnapshot> resolve(Player var1);

      default Optional<Player> ally() {
         return Optional.empty();
      }
   }

   private record DestinationSnapshot(Location location) {
      private DestinationSnapshot(Location location) {
         Objects.requireNonNull(location, "location");
         location = location.clone();
         this.location = location;
      }

      public Location location() {
         return this.location.clone();
      }
   }

   static enum EndReason {
      ARRIVED,
      JUMP_CANCELLED,
      SNEAK_CANCELLED,
      OBSTRUCTED,
      TARGET_INVALID,
      CASTER_INVALID,
      OUT_OF_RANGE,
      EXPIRED,
      TELEPORTED,
      REPLACED,
      CANCELLED,
      SHUTDOWN;
   }

   @FunctionalInterface
   interface FlightListener {
      GuardianFlightManager.FlightListener NOOP = ignored -> {};

      void onEnd(GuardianFlightManager.FlightResult var1);
   }

   record FlightResult(GuardianFlightManager.EndReason reason, Player caster, Player ally, Location start, Location end, double travelledDistance) {
      FlightResult(GuardianFlightManager.EndReason reason, Player caster, Player ally, Location start, Location end, double travelledDistance) {
         Objects.requireNonNull(reason, "reason");
         Objects.requireNonNull(caster, "caster");
         Objects.requireNonNull(start, "start");
         Objects.requireNonNull(end, "end");
         start = start.clone();
         end = end.clone();
         if (Double.isFinite(travelledDistance) && !(travelledDistance < 0.0)) {
            this.reason = reason;
            this.caster = caster;
            this.ally = ally;
            this.start = start;
            this.end = end;
            this.travelledDistance = travelledDistance;
         } else {
            throw new IllegalArgumentException("Travelled distance must be finite and non-negative");
         }
      }

      boolean arrived() {
         return this.reason == GuardianFlightManager.EndReason.ARRIVED;
      }

      public Location start() {
         return this.start.clone();
      }

      public Location end() {
         return this.end.clone();
      }
   }

   private static final class Session {
      private final Player caster;
      private final GuardianFlightManager.Destination destination;
      private final double maximumTargetDistance;
      private final int maximumTicks;
      private final double arrivalDistance;
      private final GuardianFlightManager.FlightListener listener;
      private final Location start;
      private Location previousLocation;
      private double travelledDistance;
      private int elapsedTicks;
      private int stalledTicks;
      private GuardianFlightInputState inputState;

      private Session(
         Player caster,
         GuardianFlightManager.Destination destination,
         double maximumTargetDistance,
         int maximumTicks,
         double arrivalDistance,
         GuardianFlightManager.FlightListener listener
      ) {
         this.caster = caster;
         this.destination = destination;
         this.maximumTargetDistance = maximumTargetDistance;
         this.maximumTicks = maximumTicks;
         this.arrivalDistance = arrivalDistance;
         this.listener = listener;
         this.start = caster.getLocation().clone();
         this.previousLocation = this.start.clone();
         Input input = caster.getCurrentInput();
         this.inputState = input == null ? new GuardianFlightInputState(false, false) : new GuardianFlightInputState(input.isJump(), input.isSneak());
      }

      void captureProgress(Location current) {
         double moved = GuardianFlightManager.sameWorld(this.previousLocation, current) ? this.previousLocation.distance(current) : 0.0;
         if (moved >= 0.025) {
            this.travelledDistance += moved;
            this.stalledTicks = 0;
         } else if (this.elapsedTicks > 0) {
            this.stalledTicks++;
         }

         this.previousLocation = current.clone();
      }

      void elapseTick() {
         this.elapsedTicks++;
      }

      Player caster() {
         return this.caster;
      }

      GuardianFlightManager.Destination destination() {
         return this.destination;
      }

      double maximumTargetDistance() {
         return this.maximumTargetDistance;
      }

      int maximumTicks() {
         return this.maximumTicks;
      }

      double arrivalDistance() {
         return this.arrivalDistance;
      }

      GuardianFlightManager.FlightListener listener() {
         return this.listener;
      }

      Location start() {
         return this.start.clone();
      }

      double travelledDistance() {
         return this.travelledDistance;
      }

      int elapsedTicks() {
         return this.elapsedTicks;
      }

      int stalledTicks() {
         return this.stalledTicks;
      }

      GuardianFlightInputState.Intent updateInput(Input input) {
         GuardianFlightInputState.Transition transition = this.inputState.update(input != null && input.isJump(), input != null && input.isSneak());
         this.inputState = transition.next();
         return transition.intent();
      }
   }
}
