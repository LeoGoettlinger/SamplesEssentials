package com.magmaguy.elitemobs.easyminecraftgoals.v26.mind;

import com.magmaguy.elitemobs.magmacore.ai.MindNavigationStatus;
import com.magmaguy.elitemobs.magmacore.ai.MindPerception;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

final class NativeMindPerception implements MindPerception {
   private final Mob mob;
   private final NativeMindNavigationTracker navigation;

   NativeMindPerception(Mob mob, NativeMindNavigationTracker navigation) {
      this.mob = mob;
      this.navigation = navigation;
   }

   @Override
   public Optional<LivingEntity> currentTarget() {
      return Optional.ofNullable(this.mob.getTarget()).map(target -> (LivingEntity)target.getBukkitEntity());
   }

   @Override
   public Optional<Player> nearestPlayer(double maxDistance) {
      NativeMindHost.requirePrimaryThread();
      if (Double.isFinite(maxDistance) && !(maxDistance <= 0.0)) {
         double maximumSquared = maxDistance * maxDistance;
         ServerPlayer nearest = null;
         double nearestSquared = maximumSquared;

         for (ServerPlayer player : ((ServerLevel)this.mob.level()).players()) {
            if (player.isAlive() && !player.isSpectator() && !player.isCreative()) {
               double distanceSquared = this.mob.distanceToSqr(player);
               if (!(distanceSquared > nearestSquared)
                  && (distanceSquared != nearestSquared || nearest == null || player.getUUID().compareTo(nearest.getUUID()) < 0)) {
                  nearest = player;
                  nearestSquared = distanceSquared;
               }
            }
         }

         return nearest == null ? Optional.empty() : Optional.of(nearest.getBukkitEntity());
      } else {
         throw new IllegalArgumentException("maxDistance must be finite and positive");
      }
   }

   @Override
   public boolean lineOfSight(LivingEntity target) {
      NativeMindHost.requirePrimaryThread();
      if (target != null && target.getWorld() == this.mob.getBukkitEntity().getWorld()) {
         net.minecraft.world.entity.LivingEntity nativeTarget = NativeMindVersion.getNMSLivingEntity(target);
         return this.mob.getSensing().hasLineOfSight(nativeTarget);
      } else {
         return false;
      }
   }

   @Override
   public MindNavigationStatus navigation() {
      NativeMindHost.requirePrimaryThread();
      return this.navigation.status();
   }
}
