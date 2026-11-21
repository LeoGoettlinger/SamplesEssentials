package xyz.lychee.lagfixer.nms.v1_20_R1;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.ai.goal.PathfinderGoal.Type;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;
import net.minecraft.world.entity.animal.EntityAnimal;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
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
      if (!this.animal.gc()) {
         return false;
      } else {
         this.partner = this.getFreePartner();
         if (this.partner == null || !this.partner.bs() || !this.partner.gc()) {
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
         this.animal.b(this.partner.dn(), this.partner.dp(), this.partner.dt());
      } else {
         this.animal.J().a(this.partner, this.module.getBreedSpeed());
      }

      this.animal.a(this.animal.dI().getMinecraftWorld(), this.partner);
   }

   private EntityAnimal getFreePartner() {
      List<? extends EntityAnimal> nearbyEntities = this.animal.dI().a(this.animal.getClass(), this.targeting, this.animal, this.animal.cE().g(8.0));
      return nearbyEntities.stream().filter(this.animal::a).min(Comparator.comparingDouble(other -> other.f(this.animal))).orElse(null);
   }
}
