package com.github.retrooper.packetevents.protocol.entity.cow;

import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.sound.Sound;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.protocol.util.NbtCodec;
import com.github.retrooper.packetevents.protocol.util.NbtCodecException;
import com.github.retrooper.packetevents.protocol.util.NbtMapCodec;
import com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class CowSoundSet {
   public static final NbtCodec<CowSoundSet> CODEC = (new NbtMapCodec<CowSoundSet>() {
      public CowSoundSet decode(NBTCompound tag, PacketWrapper<?> wrapper) throws NbtCodecException {
         Sound ambientSound = tag.getOrThrow("ambient_sound", Sound.CODEC, wrapper);
         Sound hurtSound = tag.getOrThrow("hurt_sound", Sound.CODEC, wrapper);
         Sound deathSound = tag.getOrThrow("death_sound", Sound.CODEC, wrapper);
         Sound stepSound = tag.getOrThrow("step_sound", Sound.CODEC, wrapper);
         return new CowSoundSet(ambientSound, hurtSound, deathSound, stepSound);
      }

      public void encode(NBTCompound tag, PacketWrapper<?> wrapper, CowSoundSet value) throws NbtCodecException {
         tag.set("ambient_sound", value.ambientSound, Sound.CODEC, wrapper);
         tag.set("hurt_sound", value.hurtSound, Sound.CODEC, wrapper);
         tag.set("death_sound", value.deathSound, Sound.CODEC, wrapper);
         tag.set("step_sound", value.stepSound, Sound.CODEC, wrapper);
      }
   }).codec();
   private final Sound ambientSound;
   private final Sound hurtSound;
   private final Sound deathSound;
   private final Sound stepSound;

   public CowSoundSet(Sound ambientSound, Sound hurtSound, Sound deathSound, Sound stepSound) {
      this.ambientSound = ambientSound;
      this.hurtSound = hurtSound;
      this.deathSound = deathSound;
      this.stepSound = stepSound;
   }

   @Internal
   public static CowSoundSet getOrThrow(String soundPrefix) {
      VersionedRegistry<Sound> soundRegistry = Sounds.getRegistry();
      Sound ambientSound = soundRegistry.getByNameOrThrow(soundPrefix + "ambient");
      Sound hurtSound = soundRegistry.getByNameOrThrow(soundPrefix + "hurt");
      Sound deathSound = soundRegistry.getByNameOrThrow(soundPrefix + "death");
      Sound stepSound = soundRegistry.getByNameOrThrow(soundPrefix + "step");
      return new CowSoundSet(ambientSound, hurtSound, deathSound, stepSound);
   }

   public Sound getAmbientSound() {
      return this.ambientSound;
   }

   public Sound getHurtSound() {
      return this.hurtSound;
   }

   public Sound getDeathSound() {
      return this.deathSound;
   }

   public Sound getStepSound() {
      return this.stepSound;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof CowSoundSet)) {
         return false;
      } else {
         CowSoundSet that = (CowSoundSet)obj;
         if (!this.ambientSound.equals(that.ambientSound)) {
            return false;
         } else if (!this.hurtSound.equals(that.hurtSound)) {
            return false;
         } else {
            return !this.deathSound.equals(that.deathSound) ? false : this.stepSound.equals(that.stepSound);
         }
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.ambientSound, this.hurtSound, this.deathSound, this.stepSound);
   }
}
