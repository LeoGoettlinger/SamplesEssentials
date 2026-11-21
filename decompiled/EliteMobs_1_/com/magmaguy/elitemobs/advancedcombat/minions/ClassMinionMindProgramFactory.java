package com.magmaguy.elitemobs.advancedcombat.minions;

import com.magmaguy.elitemobs.magmacore.ai.MindBehavior;
import com.magmaguy.elitemobs.magmacore.ai.MindContext;
import com.magmaguy.elitemobs.magmacore.ai.MindControl;
import com.magmaguy.elitemobs.magmacore.ai.MindProgram;
import com.magmaguy.elitemobs.magmacore.ai.MindStopReason;
import java.util.EnumSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

final class ClassMinionMindProgramFactory {
   private static final double ATTACK_REACH_SQUARED = 7.5625;

   private ClassMinionMindProgramFactory() {
   }

   static MindProgram create(Plugin plugin, ClassMinionTheme theme, ClassMinionControlState state) {
      MindProgram.Builder builder = MindProgram.builder(new NamespacedKey(plugin, "class_minion/" + theme.name().toLowerCase()).toString(), 1L);
      if (!theme.supportRole()) {
         builder.behavior(new ClassMinionMindProgramFactory.CombatBehavior(state));
      }

      return builder.behavior(new ClassMinionMindProgramFactory.FollowOwnerBehavior(state))
         .behavior(new ClassMinionMindProgramFactory.LocalWanderBehavior(state))
         .build();
   }

   private static final class CombatBehavior implements MindBehavior {
      private static final Set<MindControl> CONTROLS = EnumSet.of(MindControl.MOVE, MindControl.LOOK, MindControl.TARGET, MindControl.ATTACK);
      private final ClassMinionControlState state;
      private long nextAttackTick;

      private CombatBehavior(ClassMinionControlState state) {
         this.state = state;
      }

      @Override
      public String identifier() {
         return "elitemobs:class_minion/combat";
      }

      @Override
      public int priority() {
         return 0;
      }

      @Override
      public Set<MindControl> controls() {
         return CONTROLS;
      }

      @Override
      public boolean canStart(MindContext context) {
         return this.state.target(context.entity()).isPresent();
      }

      @Override
      public boolean canContinue(MindContext context) {
         return this.canStart(context);
      }

      @Override
      public void tick(MindContext context) {
         LivingEntity target = this.state.target(context.entity()).orElse(null);
         if (target != null) {
            context.actuator().setTarget(target);
            context.actuator().lookAt(target.getEyeLocation());
            if (context.entity().getLocation().distanceSquared(target.getLocation()) > 7.5625) {
               context.actuator().moveTo(target.getLocation(), 1.15);
            } else {
               context.actuator().stopMoving();
               if (context.gameTick() >= this.nextAttackTick) {
                  context.actuator().attack(target);
                  this.nextAttackTick = context.gameTick() + this.state.attackPeriodTicks();
               }
            }
         }
      }

      @Override
      public void stop(MindContext context, MindStopReason reason) {
         context.actuator().clearTarget();
         context.actuator().stopMoving();
      }
   }

   private static final class FollowOwnerBehavior implements MindBehavior {
      private static final Set<MindControl> CONTROLS = EnumSet.of(MindControl.MOVE, MindControl.LOOK);
      private final ClassMinionControlState state;

      private FollowOwnerBehavior(ClassMinionControlState state) {
         this.state = state;
      }

      @Override
      public String identifier() {
         return "elitemobs:class_minion/follow_owner";
      }

      @Override
      public int priority() {
         return 10;
      }

      @Override
      public Set<MindControl> controls() {
         return CONTROLS;
      }

      @Override
      public boolean canStart(MindContext context) {
         boolean combat = this.state.target(context.entity()).isPresent();
         return MinionBehaviorPolicy.choose(combat, this.state.ownerDistance(context.entity()), false) == MinionBehaviorPolicy.Intent.FOLLOW;
      }

      @Override
      public boolean canContinue(MindContext context) {
         return this.canStart(context);
      }

      @Override
      public void tick(MindContext context) {
         Player owner = this.state.owner().orElse(null);
         if (owner != null) {
            context.actuator().lookAt(owner.getEyeLocation());
            context.actuator().moveTo(owner.getLocation(), 1.1);
         }
      }

      @Override
      public void stop(MindContext context, MindStopReason reason) {
         context.actuator().stopMoving();
      }
   }

   private static final class LocalWanderBehavior implements MindBehavior {
      private static final Set<MindControl> CONTROLS = EnumSet.of(MindControl.MOVE, MindControl.LOOK);
      private final ClassMinionControlState state;
      private long nextWanderTick;
      private long stopAtTick;
      private Location destination;

      private LocalWanderBehavior(ClassMinionControlState state) {
         this.state = state;
      }

      @Override
      public String identifier() {
         return "elitemobs:class_minion/local_wander";
      }

      @Override
      public int priority() {
         return 20;
      }

      @Override
      public Set<MindControl> controls() {
         return CONTROLS;
      }

      @Override
      public boolean canStart(MindContext context) {
         boolean combat = this.state.target(context.entity()).isPresent();
         boolean ready = context.gameTick() >= this.nextWanderTick;
         return this.state.owner().isPresent()
            && MinionBehaviorPolicy.choose(combat, this.state.ownerDistance(context.entity()), ready) == MinionBehaviorPolicy.Intent.WANDER;
      }

      @Override
      public boolean canContinue(MindContext context) {
         if (this.destination == null || context.gameTick() >= this.stopAtTick) {
            return false;
         } else if (this.state.target(context.entity()).isPresent()) {
            return false;
         } else {
            return this.state.ownerDistance(context.entity()) > 6.0 ? false : context.perception().navigation().consecutiveStuckTicks() < 20;
         }
      }

      @Override
      public void start(MindContext context) {
         Player owner = this.state.owner().orElse(null);
         if (owner != null) {
            long seed = context.entity().getUniqueId().getLeastSignificantBits() ^ context.gameTick();
            double angle = Math.floorMod(seed, 360L) * Math.PI / 180.0;
            double radius = 1.5 + Math.floorMod(seed >>> 8, 150L) / 100.0;
            this.destination = owner.getLocation().clone().add(Math.cos(angle) * radius, 0.0, Math.sin(angle) * radius);
            if (!this.destination.getWorld().isChunkLoaded(this.destination.getBlockX() >> 4, this.destination.getBlockZ() >> 4)) {
               this.destination = null;
               this.nextWanderTick = context.gameTick() + 40L;
            } else {
               this.stopAtTick = context.gameTick() + 40L;
               context.actuator().lookAt(this.destination);
               context.actuator().moveTo(this.destination, 0.8);
            }
         }
      }

      @Override
      public void tick(MindContext context) {
         if (this.destination != null) {
            context.actuator().lookAt(this.destination);
         }
      }

      @Override
      public void stop(MindContext context, MindStopReason reason) {
         context.actuator().stopMoving();
         this.destination = null;
         long seed = context.entity().getUniqueId().getMostSignificantBits() ^ context.gameTick();
         this.nextWanderTick = context.gameTick() + 40L + Math.floorMod(seed, 41L);
      }
   }
}
