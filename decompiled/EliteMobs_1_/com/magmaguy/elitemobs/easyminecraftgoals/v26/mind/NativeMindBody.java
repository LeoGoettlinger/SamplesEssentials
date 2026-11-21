package com.magmaguy.elitemobs.easyminecraftgoals.v26.mind;

import com.magmaguy.elitemobs.magmacore.ai.MindBodyCapabilities;
import com.magmaguy.elitemobs.magmacore.ai.MindBodyProfile;
import com.magmaguy.elitemobs.magmacore.ai.MobBody;
import java.util.Objects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.phys.Vec3;
import org.bukkit.entity.LivingEntity;

final class NativeMindBody implements MobBody {
   private final NativeMindHost host;
   private final Mob mob;
   private final MindBodyProfile profile;
   private final NativeMindBodyControl control;
   private NativeMindSession session;
   private boolean preparing;

   NativeMindBody(NativeMindHost host, Mob mob, MindBodyProfile profile) {
      this.host = Objects.requireNonNull(host, "host");
      this.mob = Objects.requireNonNull(mob, "mob");
      this.profile = Objects.requireNonNull(profile, "profile");
      this.control = new NativeMindBodyControl(mob, profile);
   }

   NativeMindHost host() {
      return this.host;
   }

   Mob mob() {
      return this.mob;
   }

   ServerLevel level() {
      return (ServerLevel)this.mob.level();
   }

   void attach(NativeMindSession next) {
      if (this.session != null) {
         throw new IllegalStateException("Native mind body is already attached");
      } else {
         this.session = Objects.requireNonNull(next, "next");
      }
   }

   void detach(NativeMindSession expected) {
      if (this.session == expected) {
         this.session = null;
      }
   }

   boolean attached() {
      return this.session != null;
   }

   void beginPreparation() {
      this.preparing = true;
   }

   void endPreparation() {
      this.preparing = false;
   }

   boolean canAttach() {
      return this.isValid() || this.preparing && !this.mob.isRemoved() && this.mob.isAlive();
   }

   void abortPreparation() {
      this.preparing = false;
      if (this.session != null) {
         this.session.close();
      }
   }

   void beforeMindTick() {
      this.control.beforeMindTick();
   }

   void afterMindTick() {
      this.control.afterMindTick();
   }

   void stopMovement() {
      this.control.stopMovement();
   }

   void tickPausedPhysics() {
      this.control.tickPausedPhysics();
   }

   void steerFlight(Vec3 velocity) {
      this.control.steerFlight(velocity);
   }

   RemovalReason removalReason() {
      return this.mob.getRemovalReason();
   }

   @Override
   public LivingEntity entity() {
      return (LivingEntity)this.mob.getBukkitEntity();
   }

   @Override
   public MindBodyProfile profile() {
      return this.profile;
   }

   @Override
   public MindBodyCapabilities capabilities() {
      return this.host.bodyCapabilities();
   }

   @Override
   public boolean isValid() {
      LivingEntity entity = this.entity();
      return !this.mob.isRemoved() && entity.isValid() && !entity.isDead();
   }
}
