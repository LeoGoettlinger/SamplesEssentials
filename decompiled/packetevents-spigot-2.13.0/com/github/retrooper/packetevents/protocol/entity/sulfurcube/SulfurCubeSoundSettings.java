package com.github.retrooper.packetevents.protocol.entity.sulfurcube;

import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTFloat;
import com.github.retrooper.packetevents.protocol.sound.Sound;
import com.github.retrooper.packetevents.protocol.util.NbtCodec;
import com.github.retrooper.packetevents.protocol.util.NbtCodecException;
import com.github.retrooper.packetevents.protocol.util.NbtMapCodec;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class SulfurCubeSoundSettings {
   public static final NbtCodec<SulfurCubeSoundSettings> CODEC = (new NbtMapCodec<SulfurCubeSoundSettings>() {
      public SulfurCubeSoundSettings decode(NBTCompound tag, PacketWrapper<?> wrapper) throws NbtCodecException {
         Sound hitSound = tag.getOrThrow("hit_sound", Sound.CODEC, wrapper);
         Sound pushSound = tag.getOrThrow("push_sound", Sound.CODEC, wrapper);
         float pushSoundImpulseThreshold = tag.getNumberTagValueOrThrow("push_sound_impulse_threshold").floatValue();
         float pushSoundCooldown = tag.getNumberTagValueOrThrow("push_sound_cooldown").floatValue();
         return new SulfurCubeSoundSettings(hitSound, pushSound, pushSoundImpulseThreshold, pushSoundCooldown);
      }

      public void encode(NBTCompound tag, PacketWrapper<?> wrapper, SulfurCubeSoundSettings value) throws NbtCodecException {
         tag.set("hit_sound", value.hitSound, Sound.CODEC, wrapper);
         tag.set("push_sound", value.pushSound, Sound.CODEC, wrapper);
         tag.setTag("push_sound_impulse_threshold", new NBTFloat(value.pushSoundImpulseThreshold));
         tag.setTag("push_sound_cooldown", new NBTFloat(value.pushSoundCooldown));
      }
   }).codec();
   private final Sound hitSound;
   private final Sound pushSound;
   private final float pushSoundImpulseThreshold;
   private final float pushSoundCooldown;

   public SulfurCubeSoundSettings(Sound hitSound, Sound pushSound, float pushSoundImpulseThreshold, float pushSoundCooldown) {
      this.hitSound = hitSound;
      this.pushSound = pushSound;
      this.pushSoundImpulseThreshold = pushSoundImpulseThreshold;
      this.pushSoundCooldown = pushSoundCooldown;
   }

   public Sound getHitSound() {
      return this.hitSound;
   }

   public Sound getPushSound() {
      return this.pushSound;
   }

   public float getPushSoundImpulseThreshold() {
      return this.pushSoundImpulseThreshold;
   }

   public float getPushSoundCooldown() {
      return this.pushSoundCooldown;
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (obj != null && this.getClass() == obj.getClass()) {
         SulfurCubeSoundSettings that = (SulfurCubeSoundSettings)obj;
         if (Float.compare(that.pushSoundImpulseThreshold, this.pushSoundImpulseThreshold) != 0) {
            return false;
         } else if (Float.compare(that.pushSoundCooldown, this.pushSoundCooldown) != 0) {
            return false;
         } else {
            return !this.hitSound.equals(that.hitSound) ? false : this.pushSound.equals(that.pushSound);
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.hitSound, this.pushSound, this.pushSoundImpulseThreshold, this.pushSoundCooldown);
   }
}
