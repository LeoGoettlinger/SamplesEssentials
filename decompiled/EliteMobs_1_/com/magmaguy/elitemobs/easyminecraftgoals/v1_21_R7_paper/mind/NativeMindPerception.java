package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_paper.mind;

import com.magmaguy.elitemobs.magmacore.ai.MindNavigationStatus;
import com.magmaguy.elitemobs.magmacore.ai.MindPerception;
import java.util.Optional;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

final class NativeMindPerception implements MindPerception {
   private final EntityInsentient mob;
   private final NativeMindNavigationTracker navigation;

   NativeMindPerception(EntityInsentient mob, NativeMindNavigationTracker navigation) {
      this.mob = mob;
      this.navigation = navigation;
   }

   @Override
   public Optional<LivingEntity> currentTarget() {
      return Optional.ofNullable(this.mob.ag_()).map(target -> (LivingEntity)target.getBukkitEntity());
   }

   @Override
   public Optional<Player> nearestPlayer(double maxDistance) {
      NativeMindHost.requirePrimaryThread();
      if (Double.isFinite(maxDistance) && !(maxDistance <= 0.0)) {
         double maximumSquared = maxDistance * maxDistance;
         EntityPlayer nearest = null;
         double nearestSquared = maximumSquared;

         for (EntityPlayer player : ((WorldServer)this.mob.ao()).E()) {
            if (player.cb() && !player.au() && !player.ha()) {
               double distanceSquared = this.mob.g(player);
               if (!(distanceSquared > nearestSquared) && (distanceSquared != nearestSquared || nearest == null || player.cY().compareTo(nearest.cY()) < 0)) {
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
         EntityLiving nativeTarget = NativeMindVersion.getNMSLivingEntity(target);
         return this.mob.P().a(nativeTarget);
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
