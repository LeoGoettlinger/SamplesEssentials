package xyz.lychee.lagfixer.nms.v26_1;

import java.util.EnumSet;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import xyz.lychee.lagfixer.modules.MobAiReducerModule;

public class OptimizedTemptGoal extends Goal {
   private final MobAiReducerModule module;
   private final PathfinderMob mob;
   private final TargetingConditions targeting;
   private int cooldown = 0;
   private Player targetPlayer;

   public OptimizedTemptGoal(MobAiReducerModule module, PathfinderMob mob, TargetingConditions targeting) {
      this.module = module;
      this.mob = mob;
      this.targeting = targeting;
      this.setFlags(EnumSet.of(Flag.MOVE));
   }

   public boolean canUse() {
      if (this.cooldown > 0) {
         this.cooldown--;
         return false;
      } else {
         this.targetPlayer = getServerLevel(this.mob).getNearestPlayer(this.targeting, this.mob);
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

   public void tick() {
      if (!(this.mob.distanceToSqr(this.targetPlayer) >= 6.25) && !this.module.isTemptTeleport()) {
         this.mob.getNavigation().stop();
      } else if (this.module.isTemptTeleport()) {
         this.mob.teleportTo(this.targetPlayer.getX(), this.targetPlayer.getY(), this.targetPlayer.getZ());
      } else {
         this.mob.getNavigation().moveTo(this.targetPlayer, this.mob instanceof Animal ? this.module.getTemptSpeed() : 0.35);
      }

      this.cooldown = this.module.getTemptCooldown();
   }
}
