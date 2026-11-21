package com.magmaguy.elitemobs.advancedcombat.minions;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

final class ClassMinionControlState {
   private static final double MAX_COMBAT_DISTANCE_SQUARED = 1024.0;
   private final UUID playerOwnerId;
   private final int attackPeriodTicks;
   private UUID targetId;
   private long targetExpiresAtNanos;

   ClassMinionControlState(UUID playerOwnerId, int attackPeriodTicks) {
      this.playerOwnerId = Objects.requireNonNull(playerOwnerId, "playerOwnerId");
      if (attackPeriodTicks < 1) {
         throw new IllegalArgumentException("attackPeriodTicks must be positive");
      } else {
         this.attackPeriodTicks = attackPeriodTicks;
      }
   }

   UUID playerOwnerId() {
      return this.playerOwnerId;
   }

   int attackPeriodTicks() {
      return this.attackPeriodTicks;
   }

   Optional<Player> owner() {
      Player owner = Bukkit.getPlayer(this.playerOwnerId);
      return owner != null && owner.isOnline() && owner.isValid() && !owner.isDead() ? Optional.of(owner) : Optional.empty();
   }

   void commandTarget(LivingEntity target, int durationTicks) {
      Objects.requireNonNull(target, "target");
      if (durationTicks < 1) {
         throw new IllegalArgumentException("durationTicks must be positive");
      } else {
         this.targetId = target.getUniqueId();
         long duration = durationTicks * 50000000L;
         long now = System.nanoTime();
         this.targetExpiresAtNanos = Long.MAX_VALUE - now < duration ? Long.MAX_VALUE : now + duration;
      }
   }

   Optional<LivingEntity> target(LivingEntity minion) {
      if (this.targetId != null && System.nanoTime() < this.targetExpiresAtNanos) {
         Entity resolved = Bukkit.getEntity(this.targetId);
         Player owner = this.owner().orElse(null);
         if (resolved instanceof LivingEntity target
            && !(target instanceof Player)
            && owner != null
            && !target.isDead()
            && target.isValid()
            && target.getWorld().equals(minion.getWorld())
            && owner.getWorld().equals(minion.getWorld())
            && !target.getUniqueId().equals(minion.getUniqueId())
            && !(target.getLocation().distanceSquared(owner.getLocation()) > 1024.0)) {
            return Optional.of(target);
         } else {
            this.clearTarget();
            return Optional.empty();
         }
      } else {
         this.clearTarget();
         return Optional.empty();
      }
   }

   double ownerDistance(LivingEntity minion) {
      Player owner = this.owner().orElse(null);
      return owner != null && sameWorld(owner.getWorld(), minion.getWorld())
         ? Math.sqrt(owner.getLocation().distanceSquared(minion.getLocation()))
         : Double.POSITIVE_INFINITY;
   }

   void clearTarget() {
      this.targetId = null;
      this.targetExpiresAtNanos = 0L;
   }

   private static boolean sameWorld(World first, World second) {
      return first.getUID().equals(second.getUID());
   }
}
