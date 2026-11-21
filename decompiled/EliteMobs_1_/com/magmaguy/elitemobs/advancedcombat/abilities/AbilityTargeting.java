package com.magmaguy.elitemobs.advancedcombat.abilities;

import com.magmaguy.elitemobs.magmacore.util.EntityAimAssist;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

final class AbilityTargeting {
   private final AbilitySemantics semantics;

   AbilityTargeting(AbilitySemantics semantics) {
      this.semantics = Objects.requireNonNull(semantics, "semantics");
   }

   AbilityTargeting.TargetSelection select(Player caster, FixedAbilitySpec spec) {
      Location origin = spec.target() == AbilityTarget.AIMED_LOCATION ? this.aimedLocation(caster, spec.tuning().range()) : caster.getLocation();
      return this.select(caster, spec, origin);
   }

   AbilityTargeting.TargetSelection select(Player caster, FixedAbilitySpec spec, Location origin) {
      AbilityTuning tuning = spec.tuning();

      return switch (spec.target()) {
         case SELF -> new AbilityTargeting.TargetSelection(List.of(), List.of(caster), origin);
         case AIMED_ENEMY -> (AbilityTargeting.TargetSelection)this.aimedEnemy(caster, spec)
            .map(enemy -> new AbilityTargeting.TargetSelection(List.of(enemy), List.of(), enemy.getLocation()))
            .orElseGet(() -> new AbilityTargeting.TargetSelection(List.of(), List.of(), origin));
         case NEARBY_ENEMIES -> new AbilityTargeting.TargetSelection(this.enemiesNear(caster, origin, tuning.radius(), spec), List.of(), origin);
         case FORWARD_ENEMIES -> new AbilityTargeting.TargetSelection(this.enemiesForward(caster, tuning.range(), tuning.radius(), spec), List.of(), origin);
         case AIMED_ALLY -> (AbilityTargeting.TargetSelection)this.aimedAlly(caster, spec)
            .map(ally -> new AbilityTargeting.TargetSelection(List.of(), List.of(ally), ally.getLocation()))
            .orElseGet(() -> new AbilityTargeting.TargetSelection(List.of(), List.of(caster), caster.getLocation()));
         case NEARBY_ALLIES -> new AbilityTargeting.TargetSelection(List.of(), this.alliesNear(caster, origin, tuning.radius()), origin);
         case MIXED_NEARBY, AIMED_LOCATION -> new AbilityTargeting.TargetSelection(
            this.enemiesNear(caster, origin, tuning.radius(), spec), this.alliesNear(caster, origin, tuning.radius()), origin
         );
      };
   }

   AbilityTargeting.TargetSelection selectArea(Player caster, FixedAbilitySpec spec, Location origin, double radius) {
      return new AbilityTargeting.TargetSelection(this.enemiesNear(caster, origin, radius, spec), this.alliesNear(caster, origin, radius), origin);
   }

   List<LivingEntity> piercingEnemies(Player caster, FixedAbilitySpec spec) {
      Location eye = caster.getEyeLocation();
      Vector direction = eye.getDirection().normalize();
      double maximumDistance = obstructionDistance(eye, direction, spec.tuning().range());
      List<PiercingLinePolicy.Candidate<LivingEntity>> candidates = new ArrayList<>();

      for (Entity entity : caster.getWorld().getNearbyEntities(caster.getLocation(), maximumDistance, maximumDistance, maximumDistance)) {
         if (entity instanceof LivingEntity living && this.semantics.canTargetEnemy(caster, living, spec)) {
            BoundingBox box = living.getBoundingBox();
            candidates.add(
               new PiercingLinePolicy.Candidate<>(
                  living,
                  new PiercingLinePolicy.Box(
                     box.getMinX() - 0.35, box.getMinY() - 0.35, box.getMinZ() - 0.35, box.getMaxX() + 0.35, box.getMaxY() + 0.35, box.getMaxZ() + 0.35
                  )
               )
            );
         }
      }

      return PiercingLinePolicy.intersections(
            new PiercingLinePolicy.Point(eye.getX(), eye.getY(), eye.getZ()),
            new PiercingLinePolicy.Point(direction.getX(), direction.getY(), direction.getZ()),
            maximumDistance,
            candidates
         )
         .stream()
         .map(PiercingLinePolicy.Intersection::target)
         .toList();
   }

   Optional<Player> aimedAlly(Player caster, FixedAbilitySpec spec) {
      return spec.executionTraits().mechanics().contains(AbilityMechanic.ASSISTED_ALLY_TARGETING)
         ? EntityAimAssist.select(
               caster,
               this.validAllies(caster),
               spec.tuning().range(),
               45.0,
               ally -> ally.getGameMode() != GameMode.SPECTATOR && caster.canSee(ally),
               ally -> 0,
               1
            )
            .stream()
            .findFirst()
         : this.aimedAlly(caster, spec.tuning().range());
   }

   Optional<Player> aimedAlly(Player caster, double range) {
      if (range <= 0.0) {
         return Optional.empty();
      } else {
         Location eye = caster.getEyeLocation();
         Vector direction = eye.getDirection().normalize();
         double obstructionDistance = obstructionDistance(eye, direction, range);
         return this.validAllies(caster)
            .stream()
            .filter(candidate -> !candidate.getUniqueId().equals(caster.getUniqueId()))
            .map(candidate -> aimedCandidate(eye, direction, candidate, obstructionDistance))
            .filter(Objects::nonNull)
            .min(
               Comparator.comparingDouble(AbilityTargeting.AimedCandidate::perpendicularDistance)
                  .thenComparingDouble(AbilityTargeting.AimedCandidate::forwardDistance)
            )
            .map(AbilityTargeting.AimedCandidate::player);
      }
   }

   boolean unobstructedFrom(Location observerFeet, double observerHeight, LivingEntity target) {
      if (observerFeet.getWorld() != null && observerFeet.getWorld().equals(target.getWorld())) {
         Location source = observerFeet.clone().add(0.0, Math.max(0.5, observerHeight * 0.5), 0.0);
         Location destination = target.getLocation().add(0.0, target.getHeight() * 0.5, 0.0);
         return unobstructed(source, destination);
      } else {
         return false;
      }
   }

   Location aimedLocation(Player caster, double range) {
      Location eye = caster.getEyeLocation();
      Vector direction = eye.getDirection().normalize();
      RayTraceResult collision = caster.getWorld().rayTraceBlocks(eye, direction, Math.max(1.0, range), FluidCollisionMode.NEVER, true);
      return collision != null && collision.getHitPosition() != null
         ? collision.getHitPosition().toLocation(caster.getWorld())
         : eye.clone().add(direction.multiply(Math.max(1.0, range)));
   }

   private Optional<LivingEntity> aimedEnemy(Player caster, FixedAbilitySpec spec) {
      double range = spec.tuning().range();
      if (range <= 0.0) {
         return Optional.empty();
      } else {
         RayTraceResult result = caster.getWorld()
            .rayTraceEntities(
               caster.getEyeLocation(),
               caster.getEyeLocation().getDirection(),
               range,
               0.6,
               entity -> entity instanceof LivingEntity livingx && this.semantics.canTargetEnemy(caster, livingx, spec)
            );
         if (result != null && result.getHitEntity() instanceof LivingEntity living) {
            Vector var10 = caster.getEyeLocation().getDirection().normalize();
            double enemyDistance = living.getLocation().toVector().subtract(caster.getEyeLocation().toVector()).length();
            return enemyDistance <= obstructionDistance(caster.getEyeLocation(), var10, range) + 0.75 ? Optional.of(living) : Optional.empty();
         } else {
            return Optional.empty();
         }
      }
   }

   List<LivingEntity> enemiesNear(Player caster, Location center, double radius, FixedAbilitySpec spec) {
      if (center.getWorld() != null && !(radius <= 0.0)) {
         List<LivingEntity> enemies = new ArrayList<>();

         for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof LivingEntity living
               && this.semantics.canTargetEnemy(caster, living, spec)
               && living.getLocation().distanceSquared(center) <= radius * radius) {
               enemies.add(living);
            }
         }

         enemies.sort(Comparator.comparingDouble(entityx -> entityx.getLocation().distanceSquared(center)));
         return List.copyOf(enemies);
      } else {
         return List.of();
      }
   }

   private List<LivingEntity> enemiesForward(Player caster, double range, double width, FixedAbilitySpec spec) {
      if (range <= 0.0) {
         return List.of();
      } else {
         Location origin = caster.getEyeLocation();
         Vector direction = origin.getDirection().normalize();
         List<LivingEntity> enemies = this.enemiesNear(caster, caster.getLocation(), range, spec);
         return enemies.stream()
            .map(enemy -> forwardCandidate(origin, direction, enemy, range, Math.max(1.0, width)))
            .filter(Objects::nonNull)
            .sorted(Comparator.comparingDouble(AbilityTargeting.ForwardCandidate::forwardDistance))
            .map(AbilityTargeting.ForwardCandidate::entity)
            .toList();
      }
   }

   private List<Player> alliesNear(Player caster, Location center, double radius) {
      if (center.getWorld() == null) {
         return List.of();
      } else {
         double effectiveRadius = Math.max(0.0, radius);
         return this.validAllies(caster)
            .stream()
            .filter(ally -> ally.getWorld().equals(center.getWorld()))
            .filter(ally -> effectiveRadius == 0.0 || ally.getLocation().distanceSquared(center) <= effectiveRadius * effectiveRadius)
            .sorted(Comparator.comparingDouble(ally -> ally.getLocation().distanceSquared(center)))
            .toList();
      }
   }

   private List<Player> validAllies(Player caster) {
      Map<UUID, Player> allies = new LinkedHashMap<>();
      allies.put(caster.getUniqueId(), caster);

      for (Player ally : this.semantics.alliesOf(caster)) {
         if (ally != null && ally.isOnline() && ally.isValid() && !ally.isDead() && ally.getWorld().equals(caster.getWorld())) {
            allies.put(ally.getUniqueId(), ally);
         }
      }

      return List.copyOf(allies.values());
   }

   private static AbilityTargeting.AimedCandidate aimedCandidate(Location eye, Vector direction, Player candidate, double obstructionDistance) {
      Vector toTarget = candidate.getEyeLocation().toVector().subtract(eye.toVector());
      double forward = toTarget.dot(direction);
      if (!(forward <= 0.0) && !(forward > obstructionDistance)) {
         double perpendicularSquared = Math.max(0.0, toTarget.lengthSquared() - forward * forward);
         return perpendicularSquared > 2.25 ? null : new AbilityTargeting.AimedCandidate(candidate, forward, Math.sqrt(perpendicularSquared));
      } else {
         return null;
      }
   }

   private static AbilityTargeting.ForwardCandidate forwardCandidate(Location origin, Vector direction, LivingEntity entity, double range, double width) {
      Vector toTarget = entity.getLocation().add(0.0, entity.getHeight() * 0.5, 0.0).toVector().subtract(origin.toVector());
      double forward = toTarget.dot(direction);
      if (!(forward <= 0.0) && !(forward > range)) {
         double perpendicularSquared = Math.max(0.0, toTarget.lengthSquared() - forward * forward);
         return perpendicularSquared > width * width ? null : new AbilityTargeting.ForwardCandidate(entity, forward);
      } else {
         return null;
      }
   }

   private static boolean unobstructed(Location source, Location destination) {
      World world = source.getWorld();
      if (world != null && destination.getWorld() != null && world.equals(destination.getWorld())) {
         Vector delta = destination.toVector().subtract(source.toVector());
         double distance = delta.length();
         if (distance < 1.0E-6) {
            return true;
         } else {
            RayTraceResult collision = world.rayTraceBlocks(source, delta.normalize(), distance, FluidCollisionMode.NEVER, true);
            return collision == null || collision.getHitPosition() == null || collision.getHitPosition().distance(source.toVector()) >= distance - 0.1;
         }
      } else {
         return false;
      }
   }

   private static double obstructionDistance(Location eye, Vector direction, double range) {
      World world = eye.getWorld();
      if (world == null) {
         return 0.0;
      } else {
         RayTraceResult collision = world.rayTraceBlocks(eye, direction, range, FluidCollisionMode.NEVER, true);
         return collision != null && collision.getHitPosition() != null ? collision.getHitPosition().distance(eye.toVector()) : range;
      }
   }

   private record AimedCandidate(Player player, double forwardDistance, double perpendicularDistance) {
   }

   private record ForwardCandidate(LivingEntity entity, double forwardDistance) {
   }

   record TargetSelection(List<LivingEntity> enemies, List<Player> allies, Location origin) {
      TargetSelection(List<LivingEntity> enemies, List<Player> allies, Location origin) {
         enemies = List.copyOf(enemies);
         allies = List.copyOf(allies);
         origin = origin.clone();
         this.enemies = enemies;
         this.allies = allies;
         this.origin = origin;
      }

      boolean isEmpty() {
         return this.enemies.isEmpty() && this.allies.isEmpty();
      }
   }
}
