package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot.mind;

import com.magmaguy.elitemobs.magmacore.ai.MindBodyCapabilities;
import com.magmaguy.elitemobs.magmacore.ai.MindBodyProfile;
import com.magmaguy.elitemobs.magmacore.ai.MobBody;
import java.util.Objects;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.entity.LivingEntity;

final class NativeMindBody implements MobBody {
   private final NativeMindHost host;
   private final EntityInsentient mob;
   private final MindBodyProfile profile;
   private final NativeMindBodyControl control;
   private NativeMindSession session;
   private boolean preparing;

   NativeMindBody(NativeMindHost host, EntityInsentient mob, MindBodyProfile profile) {
      this.host = Objects.requireNonNull(host, "host");
      this.mob = Objects.requireNonNull(mob, "mob");
      this.profile = Objects.requireNonNull(profile, "profile");
      this.control = new NativeMindBodyControl(mob, profile);
   }

   NativeMindHost host() {
      return this.host;
   }

   EntityInsentient mob() {
      return this.mob;
   }

   WorldServer level() {
      return (WorldServer)this.mob.ao();
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
      return this.isValid() || this.preparing && !this.mob.eh() && this.mob.cb();
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

   void steerFlight(Vec3D velocity) {
      this.control.steerFlight(velocity);
   }

   RemovalReason removalReason() {
      return this.mob.ei();
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
      return !this.mob.eh() && entity.isValid() && !entity.isDead();
   }
}
