package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record KineticWeapon(
   int contactCooldownTicks,
   int delayTicks,
   @Nullable KineticWeapon.Condition dismountConditions,
   @Nullable KineticWeapon.Condition knockbackConditions,
   @Nullable KineticWeapon.Condition damageConditions,
   float forwardMovement,
   float damageMultiplier,
   @Nullable Holder<SoundEvent> sound,
   @Nullable Holder<SoundEvent> hitSound
) {
   public static final Type<KineticWeapon> TYPE = new Type<KineticWeapon>(KineticWeapon.class) {
      public KineticWeapon read(ByteBuf buffer) {
         int contactCooldownTicks = Types.VAR_INT.readPrimitive(buffer);
         int delayTicks = Types.VAR_INT.readPrimitive(buffer);
         KineticWeapon.Condition dismountConditions = KineticWeapon.Condition.OPTIONAL_TYPE.read(buffer);
         KineticWeapon.Condition knockbackConditions = KineticWeapon.Condition.OPTIONAL_TYPE.read(buffer);
         KineticWeapon.Condition damageConditions = KineticWeapon.Condition.OPTIONAL_TYPE.read(buffer);
         float forwardMovement = Types.FLOAT.readPrimitive(buffer);
         float damageMultiplier = Types.FLOAT.readPrimitive(buffer);
         Holder<SoundEvent> sound = Types.OPTIONAL_SOUND_EVENT.read(buffer);
         Holder<SoundEvent> hitSound = Types.OPTIONAL_SOUND_EVENT.read(buffer);
         return new KineticWeapon(
            contactCooldownTicks, delayTicks, dismountConditions, knockbackConditions, damageConditions, forwardMovement, damageMultiplier, sound, hitSound
         );
      }

      public void write(ByteBuf buffer, KineticWeapon value) {
         Types.VAR_INT.writePrimitive(buffer, value.contactCooldownTicks);
         Types.VAR_INT.writePrimitive(buffer, value.delayTicks);
         KineticWeapon.Condition.OPTIONAL_TYPE.write(buffer, value.dismountConditions);
         KineticWeapon.Condition.OPTIONAL_TYPE.write(buffer, value.knockbackConditions);
         KineticWeapon.Condition.OPTIONAL_TYPE.write(buffer, value.damageConditions);
         Types.FLOAT.writePrimitive(buffer, value.forwardMovement);
         Types.FLOAT.writePrimitive(buffer, value.damageMultiplier);
         Types.OPTIONAL_SOUND_EVENT.write(buffer, value.sound);
         Types.OPTIONAL_SOUND_EVENT.write(buffer, value.hitSound);
      }

      public void write(Ops ops, KineticWeapon value) {
         ops.writeMap(
            map -> map.writeOptional("contact_cooldown_ticks", Types.INT, value.contactCooldownTicks, 10)
               .writeOptional("delay_ticks", Types.INT, value.delayTicks, 0)
               .writeOptional("dismount_conditions", KineticWeapon.Condition.TYPE, value.dismountConditions)
               .writeOptional("knockback_conditions", KineticWeapon.Condition.TYPE, value.knockbackConditions)
               .writeOptional("damage_conditions", KineticWeapon.Condition.TYPE, value.damageConditions)
               .writeOptional("forward_movement", Types.FLOAT, value.forwardMovement, 0.0F)
               .writeOptional("damage_multiplier", Types.FLOAT, value.damageMultiplier, 1.0F)
               .writeOptional("sound", Types.SOUND_EVENT, value.sound)
               .writeOptional("hit_sound", Types.SOUND_EVENT, value.hitSound)
         );
      }
   };

   public record Condition(int maxDurationTicks, float minSpeed, float minRelativeSpeed) {
      public static final Type<KineticWeapon.Condition> TYPE = new Type<KineticWeapon.Condition>(KineticWeapon.Condition.class) {
         public KineticWeapon.Condition read(ByteBuf buffer) {
            int maxDurationTicks = Types.VAR_INT.readPrimitive(buffer);
            float minSpeed = Types.FLOAT.readPrimitive(buffer);
            float minRelativeSpeed = Types.FLOAT.readPrimitive(buffer);
            return new KineticWeapon.Condition(maxDurationTicks, minSpeed, minRelativeSpeed);
         }

         public void write(ByteBuf buffer, KineticWeapon.Condition value) {
            Types.VAR_INT.writePrimitive(buffer, value.maxDurationTicks);
            Types.FLOAT.writePrimitive(buffer, value.minSpeed);
            Types.FLOAT.writePrimitive(buffer, value.minRelativeSpeed);
         }

         public void write(Ops ops, KineticWeapon.Condition value) {
            ops.writeMap(
               map -> map.write("max_duration_ticks", Types.INT, value.maxDurationTicks)
                  .writeOptional("min_speed", Types.FLOAT, value.minSpeed, 0.0F)
                  .writeOptional("min_relative_speed", Types.FLOAT, value.minRelativeSpeed, 0.0F)
            );
         }
      };
      public static final Type<KineticWeapon.Condition> OPTIONAL_TYPE = new OptionalType<KineticWeapon.Condition>(TYPE) {};
   }
}
