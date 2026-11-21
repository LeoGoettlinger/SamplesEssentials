package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record PiercingWeapon(boolean dealsKnockback, boolean dismounts, @Nullable Holder<SoundEvent> sound, @Nullable Holder<SoundEvent> hitSound) {
   public static final Type<PiercingWeapon> TYPE = new Type<PiercingWeapon>(PiercingWeapon.class) {
      public PiercingWeapon read(ByteBuf buffer) {
         boolean dealsKnockback = Types.BOOLEAN.read(buffer);
         boolean dismounts = Types.BOOLEAN.read(buffer);
         Holder<SoundEvent> sound = Types.OPTIONAL_SOUND_EVENT.read(buffer);
         Holder<SoundEvent> hitSound = Types.OPTIONAL_SOUND_EVENT.read(buffer);
         return new PiercingWeapon(dealsKnockback, dismounts, sound, hitSound);
      }

      public void write(ByteBuf buffer, PiercingWeapon value) {
         Types.BOOLEAN.write(buffer, value.dealsKnockback);
         Types.BOOLEAN.write(buffer, value.dismounts);
         Types.OPTIONAL_SOUND_EVENT.write(buffer, value.sound);
         Types.OPTIONAL_SOUND_EVENT.write(buffer, value.hitSound);
      }

      public void write(Ops ops, PiercingWeapon value) {
         ops.writeMap(
            map -> map.writeOptional("deals_knockback", Types.BOOLEAN, value.dealsKnockback, true)
               .writeOptional("dismounts", Types.BOOLEAN, value.dismounts, false)
               .writeOptional("sound", Types.SOUND_EVENT, value.sound)
               .writeOptional("hit_sound", Types.SOUND_EVENT, value.hitSound)
         );
      }
   };
}
