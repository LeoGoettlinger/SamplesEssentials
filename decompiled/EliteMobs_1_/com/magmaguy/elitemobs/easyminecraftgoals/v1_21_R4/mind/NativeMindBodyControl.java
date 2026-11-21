package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R4.mind;

import com.magmaguy.elitemobs.magmacore.ai.MindBodyLocomotion;
import com.magmaguy.elitemobs.magmacore.ai.MindBodyProfile;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EnumMoveType;
import net.minecraft.world.entity.ai.attributes.AttributeBase;
import net.minecraft.world.entity.ai.attributes.AttributeMapBase;
import net.minecraft.world.entity.ai.attributes.AttributeModifiable;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.control.ControllerLook;
import net.minecraft.world.entity.ai.control.ControllerMove;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.PathfinderGoalSelector;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.Navigation;
import net.minecraft.world.entity.ai.navigation.NavigationAbstract;
import net.minecraft.world.entity.ai.navigation.NavigationFlying;
import net.minecraft.world.entity.ai.navigation.NavigationGuardian;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Squid;

final class NativeMindBodyControl {
   private static final Field NAVIGATION = field(NavigationAbstract.class);
   private static final Field MOVE_CONTROL = field(ControllerMove.class);
   private static final Field LOOK_CONTROL = field(ControllerLook.class);
   private static final Field ATTRIBUTE_INSTANCES = attributeInstances();
   private final EntityInsentient mob;
   private final MindBodyProfile profile;
   private final Field squidMovementVector;
   private Vec3D flightVelocity;

   NativeMindBodyControl(EntityInsentient mob, MindBodyProfile profile) {
      this.mob = mob;
      this.profile = profile;
      this.squidMovementVector = squidMovementVector(mob, profile);
      this.applyProfile();
   }

   void beforeMindTick() {
      this.flightVelocity = null;
      this.mob.t(true);
      this.mob.O().a();
      if (this.profile.locomotion() == MindBodyLocomotion.AQUATIC || this.profile.locomotion() == MindBodyLocomotion.AMPHIBIOUS) {
         this.mob.j(this.mob.cr());
      }

      if (this.profile.locomotion() == MindBodyLocomotion.FLYING) {
         this.mob.f(true);
      }

      if (this.squidMovementVector != null && !this.mob.bi()) {
         this.setSquidMovement(Vec3D.c);
      }

      if (this.profile.locomotion() == MindBodyLocomotion.STATIONARY) {
         this.stopMovement();
      }
   }

   void afterMindTick() {
      if (this.flightVelocity != null) {
         this.mob.N().m();
         NativeMindVersion.stopControl(this.mob.K());
         this.mob.f(true);
         this.mob.i(this.flightVelocity);
         this.mob.a(EnumMoveType.a, this.flightVelocity);
         this.mob.Z = 0.0;
         double horizontal = this.flightVelocity.i();
         if (horizontal > 1.0E-5) {
            float desired = (float)Math.toDegrees(Math.atan2(-this.flightVelocity.d, this.flightVelocity.f));
            float difference = MathHelper.h(desired - this.mob.dL());
            this.mob.w(this.mob.dL() + MathHelper.a(difference, -8.0F, 8.0F));
            this.mob.s(this.mob.dL());
            this.mob.r(this.mob.dL());
         }
      } else if (this.profile.locomotion() == MindBodyLocomotion.STATIONARY) {
         this.stopMovement();
      } else {
         this.mob.N().c();
         this.mob.K().a();
         this.mob.I().a();
         this.mob.M().b();
         if (this.squidMovementVector != null && this.mob.bi()) {
            float yaw = MathHelper.h(this.mob.dL());
            this.mob.w(yaw);
            this.mob.s(yaw);
            Vec3D movement = new Vec3D(this.mob.bg, this.mob.bh, this.mob.bi)
               .b((float)Math.toRadians(-yaw))
               .c(0.2 / ((AttributeBase)GenericAttributes.v.a()).a());
            this.setSquidMovement(movement);
         }

         NativeMindVersion.advancePhysics(this.mob, new Vec3D(this.mob.bg, this.mob.bh, this.mob.bi));
      }
   }

   void stopMovement() {
      this.flightVelocity = null;
      this.mob.N().m();
      NativeMindVersion.stopControl(this.mob.K());
      this.mob.V();
      if (this.squidMovementVector != null) {
         this.setSquidMovement(Vec3D.c);
      }
   }

   void steerFlight(Vec3D velocity) {
      this.flightVelocity = velocity;
   }

   void tickPausedPhysics() {
      this.beforeMindTick();
      this.mob.N().m();
      NativeMindVersion.stopControl(this.mob.K());
      this.mob.I(0.0F);
      this.mob.H(0.0F);
      this.mob.G(0.0F);
      if (this.squidMovementVector != null) {
         this.setSquidMovement(Vec3D.c);
      }

      if (this.profile.locomotion() != MindBodyLocomotion.STATIONARY) {
         NativeMindVersion.advancePhysics(this.mob, Vec3D.c);
      }
   }

   private void applyProfile() {
      this.mob.gp();

      for (Field field : EntityInsentient.class.getDeclaredFields()) {
         if (field.getType() == PathfinderGoalSelector.class) {
            try {
               field.setAccessible(true);
               ((PathfinderGoalSelector)field.get(this.mob)).a(goal -> true);
            } catch (IllegalAccessException var6) {
               throw new IllegalStateException("Could not clear native target goals", var6);
            }
         }
      }

      this.mob.t(true);
      if (this.mob.getBukkitEntity() instanceof Bat bat) {
         bat.setAwake(true);
      }

      this.set(NAVIGATION, this.navigation());
      this.set(MOVE_CONTROL, this.moveControl());
      if (this.profile.locomotion() == MindBodyLocomotion.AQUATIC) {
         this.set(LOOK_CONTROL, new SmoothSwimmingLookControl(this.mob, 10));
      }

      this.mob.f(this.profile.locomotion() == MindBodyLocomotion.FLYING);
      this.applyUniformScale();
      this.ensureAttribute(GenericAttributes.c, 2.0);
      this.ensureAttribute(GenericAttributes.d, 0.0);
      if (this.profile.locomotion() == MindBodyLocomotion.STATIONARY) {
         this.stopMovement();
      }
   }

   private void setSquidMovement(Vec3D movement) {
      this.set(this.squidMovementVector, movement);
      if (this.mob.bi()) {
         this.mob.i(movement);
      }
   }

   private static Field squidMovementVector(EntityInsentient mob, MindBodyProfile profile) {
      if (profile.locomotion() == MindBodyLocomotion.AQUATIC && mob.getBukkitEntity() instanceof Squid) {
         Field movement = null;

         for (Class<?> type = mob.getClass(); type != EntityInsentient.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
               if (field.getType() == Vec3D.class && !Modifier.isStatic(field.getModifiers())) {
                  if (movement != null) {
                     throw new IllegalStateException("Native squid swim vector is ambiguous");
                  }

                  movement = field;
               }
            }
         }

         if (movement == null) {
            throw new IllegalStateException("Native squid swim vector is unavailable");
         } else {
            movement.setAccessible(true);
            return movement;
         }
      } else {
         return null;
      }
   }

   private NavigationAbstract navigation() {
      WorldServer level = (WorldServer)this.mob.dV();

      return (NavigationAbstract)(switch (this.profile.locomotion()) {
         case GROUNDED, STATIONARY -> new Navigation(this.mob, level);
         case FLYING -> new NavigationFlying(this.mob, level);
         case AQUATIC -> {
            NavigationGuardian navigation = new NavigationGuardian(this.mob, level);
            navigation.a(true);
            yield navigation;
         }
         case AMPHIBIOUS -> {
            AmphibiousPathNavigation navigation = new AmphibiousPathNavigation(this.mob, level);
            navigation.a(true);
            yield navigation;
         }
      });
   }

   private ControllerMove moveControl() {
      return (ControllerMove)(switch (this.profile.locomotion()) {
         case GROUNDED, STATIONARY, AMPHIBIOUS -> new ControllerMove(this.mob);
         case FLYING -> new NativeMindFlyingMoveControl(this.mob, 20, true);
         case AQUATIC -> new SmoothSwimmingMoveControl(this.mob, 85, 10, 0.02F, 0.1F, false);
      });
   }

   private void applyUniformScale() {
      AttributeModifiable scale = this.mob.g(GenericAttributes.y);
      if (scale == null) {
         throw new IllegalStateException("Native Mind carrier is missing Minecraft's scale attribute");
      } else {
         scale.a(this.profile.uniformScale());
      }
   }

   private void ensureAttribute(Holder<AttributeBase> attribute, double baseline) {
      AttributeMapBase attributes = this.mob.fa();
      if (!attributes.b(attribute)) {
         AttributeModifiable instance = new AttributeModifiable(attribute, changed -> {
            if (((AttributeBase)attribute.a()).b()) {
               attributes.a().add(changed);
            }

            attributes.b().add(changed);
         });

         try {
            ((Map)ATTRIBUTE_INSTANCES.get(attributes)).put(attribute, instance);
            instance.a(baseline);
         } catch (IllegalAccessException var7) {
            throw new IllegalStateException("Could not install native melee attributes", var7);
         }
      }
   }

   private static Field attributeInstances() {
      for (Field field : AttributeMapBase.class.getDeclaredFields()) {
         if (field.getType() == Map.class) {
            field.setAccessible(true);
            return field;
         }
      }

      throw new ExceptionInInitializerError("Native attribute instance map is unavailable");
   }

   private void set(Field field, Object value) {
      try {
         field.set(this.mob, value);
      } catch (IllegalAccessException var4) {
         throw new IllegalStateException("Could not install native Mind body controls", var4);
      }
   }

   private static Field field(Class<?> type) {
      try {
         for (Field field : EntityInsentient.class.getDeclaredFields()) {
            if (field.getType() == type) {
               field.setAccessible(true);
               return field;
            }
         }

         throw new NoSuchFieldException(type.getName());
      } catch (RuntimeException | ReflectiveOperationException var5) {
         throw new ExceptionInInitializerError(var5);
      }
   }
}
