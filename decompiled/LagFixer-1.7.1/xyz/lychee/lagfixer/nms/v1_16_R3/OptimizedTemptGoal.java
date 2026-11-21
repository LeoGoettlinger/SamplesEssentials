package xyz.lychee.lagfixer.nms.v1_16_R3;

import java.util.EnumSet;
import net.minecraft.server.v1_16_R3.EntityAnimal;
import net.minecraft.server.v1_16_R3.EntityCreature;
import net.minecraft.server.v1_16_R3.EntityHuman;
import net.minecraft.server.v1_16_R3.PathfinderGoal;
import net.minecraft.server.v1_16_R3.PathfinderTargetCondition;
import net.minecraft.server.v1_16_R3.PathfinderGoal.Type;
import org.bukkit.craftbukkit.v1_16_R3.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import xyz.lychee.lagfixer.modules.MobAiReducerModule;

public class OptimizedTemptGoal extends PathfinderGoal {
   private final MobAiReducerModule module;
   private final EntityCreature mob;
   private final PathfinderTargetCondition targeting;
   private int cooldown = 0;

   public OptimizedTemptGoal(MobAiReducerModule module, EntityCreature mob, PathfinderTargetCondition targeting) {
      this.module = module;
      this.mob = mob;
      this.targeting = targeting;
      this.a(EnumSet.of(Type.MOVE));
   }

   public boolean a() {
      return --this.cooldown <= 0;
   }

   public void e() {
      this.cooldown = this.module.getTemptCooldown();
      EntityHuman player = this.mob.getWorld().a(this.targeting, this.mob);
      if (player != null) {
         if (this.module.isTemptEvent()) {
            EntityTargetLivingEntityEvent event = CraftEventFactory.callEntityTargetLivingEvent(this.mob, player, TargetReason.TEMPT);
            if (event.isCancelled()) {
               return;
            }
         }

         if (this.mob.h(player) >= 6.25 || this.module.isTemptTeleport()) {
            if (this.module.isTemptTeleport()) {
               this.mob.enderTeleportTo(player.locX(), player.locY(), player.locZ());
            } else {
               this.mob.getNavigation().a(player, this.mob instanceof EntityAnimal ? this.module.getTemptSpeed() : 0.35);
            }

            return;
         }

         this.mob.getNavigation().o();
      }
   }
}
