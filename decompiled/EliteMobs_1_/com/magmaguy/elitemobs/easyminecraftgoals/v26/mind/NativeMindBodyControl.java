package com.magmaguy.elitemobs.easyminecraftgoals.v26.mind;

import com.magmaguy.elitemobs.magmacore.ai.MindBodyLocomotion;
import com.magmaguy.elitemobs.magmacore.ai.MindBodyProfile;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.phys.Vec3;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Squid;

final class NativeMindBodyControl {
   private static final Field NAVIGATION = field(PathNavigation.class);
   private static final Field MOVE_CONTROL = field(MoveControl.class);
   private static final Field LOOK_CONTROL = field(LookControl.class);
   private static final Field ATTRIBUTE_INSTANCES = attributeInstances();
   private final Mob mob;
   private final MindBodyProfile profile;
   private final Field squidMovementVector;
   private Vec3 flightVelocity;

   NativeMindBodyControl(Mob mob, MindBodyProfile profile) {
      this.mob = mob;
      this.profile = profile;
      this.squidMovementVector = squidMovementVector(mob, profile);
      this.applyProfile();
   }

   void beforeMindTick() {
      this.flightVelocity = null;
      this.mob.setNoAi(true);
      this.mob.getSensing().tick();
      if (this.profile.locomotion() == MindBodyLocomotion.AQUATIC || this.profile.locomotion() == MindBodyLocomotion.AMPHIBIOUS) {
         this.mob.setAirSupply(this.mob.getMaxAirSupply());
      }

      if (this.profile.locomotion() == MindBodyLocomotion.FLYING) {
         this.mob.setNoGravity(true);
      }

      if (this.squidMovementVector != null && !this.mob.isInWater()) {
         this.setSquidMovement(Vec3.ZERO);
      }

      if (this.profile.locomotion() == MindBodyLocomotion.STATIONARY) {
         this.stopMovement();
      }
   }

   void afterMindTick() {
      if (this.flightVelocity != null) {
         this.mob.getNavigation().stop();
         NativeMindVersion.stopControl(this.mob.getMoveControl());
         this.mob.setNoGravity(true);
         this.mob.setDeltaMovement(this.flightVelocity);
         this.mob.move(MoverType.SELF, this.flightVelocity);
         this.mob.fallDistance = 0.0;
         double horizontal = this.flightVelocity.horizontalDistance();
         if (horizontal > 1.0E-5) {
            float desired = (float)Math.toDegrees(Math.atan2(-this.flightVelocity.x, this.flightVelocity.z));
            float difference = Mth.wrapDegrees(desired - this.mob.getYRot());
            this.mob.setYRot(this.mob.getYRot() + Mth.clamp(difference, -8.0F, 8.0F));
            this.mob.setYBodyRot(this.mob.getYRot());
            this.mob.setYHeadRot(this.mob.getYRot());
         }
      } else if (this.profile.locomotion() == MindBodyLocomotion.STATIONARY) {
         this.stopMovement();
      } else {
         this.mob.getNavigation().tick();
         this.mob.getMoveControl().tick();
         this.mob.getLookControl().tick();
         this.mob.getJumpControl().tick();
         if (this.squidMovementVector != null && this.mob.isInWater()) {
            float yaw = Mth.wrapDegrees(this.mob.getYRot());
            this.mob.setYRot(yaw);
            this.mob.setYBodyRot(yaw);
            Vec3 movement = new Vec3(this.mob.xxa, this.mob.yya, this.mob.zza)
               .yRot((float)Math.toRadians(-yaw))
               .scale(0.2 / ((Attribute)Attributes.MOVEMENT_SPEED.value()).getDefaultValue());
            this.setSquidMovement(movement);
         }

         NativeMindVersion.advancePhysics(this.mob, new Vec3(this.mob.xxa, this.mob.yya, this.mob.zza));
      }
   }

   void stopMovement() {
      this.flightVelocity = null;
      this.mob.getNavigation().stop();
      NativeMindVersion.stopControl(this.mob.getMoveControl());
      this.mob.stopInPlace();
      if (this.squidMovementVector != null) {
         this.setSquidMovement(Vec3.ZERO);
      }
   }

   void steerFlight(Vec3 velocity) {
      this.flightVelocity = velocity;
   }

   void tickPausedPhysics() {
      this.beforeMindTick();
      this.mob.getNavigation().stop();
      NativeMindVersion.stopControl(this.mob.getMoveControl());
      this.mob.setXxa(0.0F);
      this.mob.setYya(0.0F);
      this.mob.setZza(0.0F);
      if (this.squidMovementVector != null) {
         this.setSquidMovement(Vec3.ZERO);
      }

      if (this.profile.locomotion() != MindBodyLocomotion.STATIONARY) {
         NativeMindVersion.advancePhysics(this.mob, Vec3.ZERO);
      }
   }

   private void applyProfile() {
      this.mob.removeFreeWill();

      for (Field field : Mob.class.getDeclaredFields()) {
         if (field.getType() == GoalSelector.class) {
            try {
               field.setAccessible(true);
               ((GoalSelector)field.get(this.mob)).removeAllGoals(goal -> true);
            } catch (IllegalAccessException var6) {
               throw new IllegalStateException("Could not clear native target goals", var6);
            }
         }
      }

      this.mob.setNoAi(true);
      if (this.mob.getBukkitEntity() instanceof Bat bat) {
         bat.setAwake(true);
      }

      this.set(NAVIGATION, this.navigation());
      this.set(MOVE_CONTROL, this.moveControl());
      if (this.profile.locomotion() == MindBodyLocomotion.AQUATIC) {
         this.set(LOOK_CONTROL, new SmoothSwimmingLookControl(this.mob, 10));
      }

      this.mob.setNoGravity(this.profile.locomotion() == MindBodyLocomotion.FLYING);
      this.applyUniformScale();
      this.ensureAttribute(Attributes.ATTACK_DAMAGE, 2.0);
      this.ensureAttribute(Attributes.ATTACK_KNOCKBACK, 0.0);
      if (this.profile.locomotion() == MindBodyLocomotion.STATIONARY) {
         this.stopMovement();
      }
   }

   private void setSquidMovement(Vec3 movement) {
      this.set(this.squidMovementVector, movement);
      if (this.mob.isInWater()) {
         this.mob.setDeltaMovement(movement);
      }
   }

   private static Field squidMovementVector(Mob mob, MindBodyProfile profile) {
      if (profile.locomotion() == MindBodyLocomotion.AQUATIC && mob.getBukkitEntity() instanceof Squid) {
         Field movement = null;

         for (Class<?> type = mob.getClass(); type != Mob.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
               if (field.getType() == Vec3.class && !Modifier.isStatic(field.getModifiers())) {
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

   private PathNavigation navigation() {
      ServerLevel level = (ServerLevel)this.mob.level();

      return (PathNavigation)(switch (this.profile.locomotion()) {
         case GROUNDED, STATIONARY -> new GroundPathNavigation(this.mob, level);
         case FLYING -> new FlyingPathNavigation(this.mob, level);
         case AQUATIC -> {
            WaterBoundPathNavigation navigation = new WaterBoundPathNavigation(this.mob, level);
            navigation.setCanFloat(true);
            yield navigation;
         }
         case AMPHIBIOUS -> {
            AmphibiousPathNavigation navigation = new AmphibiousPathNavigation(this.mob, level);
            navigation.setCanFloat(true);
            yield navigation;
         }
      });
   }

   private MoveControl moveControl() {
      return (MoveControl)(switch (this.profile.locomotion()) {
         case GROUNDED, STATIONARY, AMPHIBIOUS -> new MoveControl(this.mob);
         case FLYING -> new NativeMindFlyingMoveControl(this.mob, 20, true);
         case AQUATIC -> new SmoothSwimmingMoveControl(this.mob, 85, 10, 0.02F, 0.1F, false);
      });
   }

   private void applyUniformScale() {
      AttributeInstance scale = this.mob.getAttribute(Attributes.SCALE);
      if (scale == null) {
         throw new IllegalStateException("Native Mind carrier is missing Minecraft's scale attribute");
      } else {
         scale.setBaseValue(this.profile.uniformScale());
      }
   }

   private void ensureAttribute(Holder<Attribute> attribute, double baseline) {
      AttributeMap attributes = this.mob.getAttributes();
      if (!attributes.hasAttribute(attribute)) {
         AttributeInstance instance = new AttributeInstance(attribute, changed -> {
            if (((Attribute)attribute.value()).isClientSyncable()) {
               attributes.getAttributesToSync().add(changed);
            }

            attributes.getAttributesToUpdate().add(changed);
         });

         try {
            ((Map)ATTRIBUTE_INSTANCES.get(attributes)).put(attribute, instance);
            instance.setBaseValue(baseline);
         } catch (IllegalAccessException var7) {
            throw new IllegalStateException("Could not install native melee attributes", var7);
         }
      }
   }

   private static Field attributeInstances() {
      for (Field field : AttributeMap.class.getDeclaredFields()) {
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
         for (Field field : Mob.class.getDeclaredFields()) {
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
