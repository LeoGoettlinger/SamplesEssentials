package xyz.lychee.lagfixer.nms.v1_17_R1;

import java.util.EnumSet;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.ai.goal.PathfinderGoal.Type;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;
import net.minecraft.world.entity.animal.EntityAnimal;
import net.minecraft.world.entity.player.EntityHuman;
import org.bukkit.craftbukkit.v1_17_R1.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import xyz.lychee.lagfixer.modules.MobAiReducerModule;

public class OptimizedTemptGoal extends PathfinderGoal {
   private final MobAiReducerModule module;
   private final EntityCreature mob;
   private final PathfinderTargetCondition targeting;
   private int cooldown = 0;
   private EntityHuman targetPlayer;

   public OptimizedTemptGoal(MobAiReducerModule module, EntityCreature mob, PathfinderTargetCondition targeting) {
      this.module = module;
      this.mob = mob;
      this.targeting = targeting;
      this.a(EnumSet.of(Type.a));
   }

   public boolean a() {
      if (this.cooldown > 0) {
         this.cooldown--;
         return false;
      } else {
         this.targetPlayer = this.mob.t.a(this.targeting, this.mob);
         if (this.targetPlayer == null) {
            return false;
         } else if (this.module.isTemptEvent()) {
            EntityTargetLivingEntityEvent event = CraftEventFactory.callEntityTargetLivingEvent(this.mob, this.targetPlayer, TargetReason.TEMPT);
            return !event.isCancelled();
         } else {
            return true;
         }
      }
   }

   public void e() {
      if (!(this.mob.f(this.targetPlayer) >= 6.25) && !this.module.isTemptTeleport()) {
         this.mob.getNavigation().o();
      } else if (this.module.isTemptTeleport()) {
         this.mob.enderTeleportTo(this.targetPlayer.locX(), this.targetPlayer.locY(), this.targetPlayer.locZ());
      } else {
         this.mob.getNavigation().a(this.targetPlayer, this.mob instanceof EntityAnimal ? this.module.getTemptSpeed() : 0.35);
      }

      this.cooldown = this.module.getTemptCooldown();
   }
}
