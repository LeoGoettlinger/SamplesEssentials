package com.magmaguy.elitemobs.magmacore.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public final class EntityAimAssist {
   public static final double DEFAULT_CONE_DEGREES = 45.0;

   private EntityAimAssist() {
   }

   public static <T extends LivingEntity> List<T> select(
      Player owner, Collection<T> nearby, double range, double assistDegrees, Predicate<T> eligible, ToIntFunction<T> priority, int limit
   ) {
      if (Double.isFinite(range) && !(range <= 0.0) && limit > 0) {
         Location eye = owner.getEyeLocation();
         Vector aim = eye.getDirection();
         if (aim.lengthSquared() < 1.0E-9) {
            return List.of();
         } else {
            aim.normalize();
            double minimumDot = Math.cos(Math.toRadians(assistDegrees));
            List<EntityAimAssist.Candidate<T>> candidates = new ArrayList<>();

            for (T target : nearby) {
               if (target != null
                  && target != owner
                  && target.isValid()
                  && !target.isDead()
                  && !(target.getHealth() <= 0.0)
                  && !(target instanceof ArmorStand)
                  && target.getWorld().equals(eye.getWorld())) {
                  Location center = center(target);
                  Vector offset = center.toVector().subtract(eye.toVector());
                  double distance = offset.length();
                  if (!(distance <= 1.0E-6) && !(distance > range)) {
                     double dot = offset.multiply(1.0 / distance).dot(aim);
                     if (!(dot < minimumDot) && eligible.test(target) && unobstructed(eye, center)) {
                        candidates.add(new EntityAimAssist.Candidate<>(target, priority.applyAsInt(target), (1.0 - dot) * 100.0 + distance / range, distance));
                     }
                  }
               }
            }

            return candidates.stream()
               .sorted(
                  Comparator.<EntityAimAssist.Candidate<T>>comparingInt(candidate -> candidate.tier())
                     .thenComparingDouble(EntityAimAssist.Candidate::score)
                     .thenComparingDouble(EntityAimAssist.Candidate::distance)
               )
               .limit(limit)
               .map(EntityAimAssist.Candidate::target)
               .toList();
         }
      } else {
         return List.of();
      }
   }

   public static Location center(LivingEntity entity) {
      return entity.getLocation().add(0.0, Math.max(0.35, entity.getHeight() * 0.52), 0.0);
   }

   public static boolean unobstructed(Location source, Location destination) {
      World world = source.getWorld();
      if (world != null && world.equals(destination.getWorld())) {
         Vector delta = destination.toVector().subtract(source.toVector());
         double distance = delta.length();
         if (distance < 1.0E-6) {
            return true;
         } else {
            RayTraceResult hit = world.rayTraceBlocks(source, delta.normalize(), distance, FluidCollisionMode.NEVER, true);
            return hit == null || hit.getHitPosition() == null || hit.getHitPosition().distance(source.toVector()) >= distance - 0.08;
         }
      } else {
         return false;
      }
   }

   private record Candidate<T>(T target, int tier, double score, double distance) {
   }
}
