package xyz.lychee.lagfixer.nms.v1_17_R1;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.ai.goal.PathfinderGoal.Type;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;
import net.minecraft.world.entity.animal.EntityAnimal;
import org.bukkit.craftbukkit.v1_17_R1.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import xyz.lychee.lagfixer.modules.MobAiReducerModule;

public class OptimizedBreedGoal extends PathfinderGoal {
   private final MobAiReducerModule module;
   private final PathfinderTargetCondition targeting;
   private final EntityAnimal animal;
   private EntityAnimal partner;

   public OptimizedBreedGoal(MobAiReducerModule module, EntityAnimal entityanimal, PathfinderTargetCondition targeting) {
      this.module = module;
      this.animal = entityanimal;
      this.targeting = targeting;
      this.a(EnumSet.of(Type.a));
   }

   public boolean a() {
      if (!this.animal.isInLove()) {
         return false;
      } else {
         this.partner = this.getFreePartner();
         if (this.partner == null || !this.partner.isAlive() || !this.partner.isInLove()) {
            return false;
         } else if (this.module.isBreedEvent()) {
            EntityTargetLivingEntityEvent event = CraftEventFactory.callEntityTargetLivingEvent(this.animal, this.partner, TargetReason.CUSTOM);
            return !event.isCancelled();
         } else {
            return true;
         }
      }
   }

   public void e() {
      if (this.module.isBreedTeleport()) {
         this.animal.enderTeleportTo(this.partner.locX(), this.partner.locY(), this.partner.locZ());
      } else {
         this.animal.getNavigation().a(this.partner, this.module.getBreedSpeed());
      }

      this.animal.a(this.animal.t.getMinecraftWorld(), this.partner);
   }

   private EntityAnimal getFreePartner() {
      List<? extends EntityAnimal> nearbyEntities = this.animal.t.a(this.animal.getClass(), this.targeting, this.animal, this.animal.getBoundingBox().g(8.0));
      return nearbyEntities.stream().filter(this.animal::mate).min(Comparator.comparingDouble(other -> other.f(this.animal))).orElse(null);
   }
}
