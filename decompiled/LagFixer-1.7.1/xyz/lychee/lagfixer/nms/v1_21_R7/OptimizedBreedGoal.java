package xyz.lychee.lagfixer.nms.v1_21_R7;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.ai.goal.PathfinderGoal.Type;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;
import net.minecraft.world.entity.animal.EntityAnimal;
import org.bukkit.craftbukkit.v1_21_R7.event.CraftEventFactory;
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

   public boolean b() {
      if (!this.animal.hb()) {
         return false;
      } else {
         this.partner = this.getFreePartner();
         if (this.partner == null || !this.partner.cb() || !this.partner.hb()) {
            return false;
         } else if (this.module.isBreedEvent()) {
            EntityTargetLivingEntityEvent event = CraftEventFactory.callEntityTargetLivingEvent(this.animal, this.partner, TargetReason.CUSTOM);
            return !event.isCancelled();
         } else {
            return true;
         }
      }
   }

   public void a() {
      if (this.module.isBreedTeleport()) {
         this.animal.b(this.partner.dP(), this.partner.dR(), this.partner.dV());
      } else {
         this.animal.N().a(this.partner, this.module.getBreedSpeed());
      }

      this.animal.a(this.animal.ao().getMinecraftWorld(), this.partner);
   }

   private EntityAnimal getFreePartner() {
      List<? extends EntityAnimal> nearbyEntities = a(this.animal).a(this.animal.getClass(), this.targeting, this.animal, this.animal.dj().g(8.0));
      return nearbyEntities.stream().filter(this.animal::a).min(Comparator.comparingDouble(other -> other.g(this.animal))).orElse(null);
   }
}
