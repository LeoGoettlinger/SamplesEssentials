package com.github.retrooper.packetevents.protocol.entity.cat;

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
public final class CatSoundSet {
   public static final NbtCodec<CatSoundSet> CODEC = (new NbtMapCodec<CatSoundSet>() {
      public CatSoundSet decode(NBTCompound tag, PacketWrapper<?> wrapper) throws NbtCodecException {
         Sound ambientSound = tag.getOrThrow("ambient_sound", Sound.CODEC, wrapper);
         Sound strayAmbientSound = tag.getOrThrow("stray_ambient_sound", Sound.CODEC, wrapper);
         Sound hissSound = tag.getOrThrow("hiss_sound", Sound.CODEC, wrapper);
         Sound hurtSound = tag.getOrThrow("hurt_sound", Sound.CODEC, wrapper);
         Sound deathSound = tag.getOrThrow("death_sound", Sound.CODEC, wrapper);
         Sound eatSound = tag.getOrThrow("eat_sound", Sound.CODEC, wrapper);
         Sound begForFoodSound = tag.getOrThrow("beg_for_food_sound", Sound.CODEC, wrapper);
         Sound purrSound = tag.getOrThrow("purr_sound", Sound.CODEC, wrapper);
         Sound purreowSound = tag.getOrThrow("purreow_sound", Sound.CODEC, wrapper);
         return new CatSoundSet(ambientSound, strayAmbientSound, hissSound, hurtSound, deathSound, eatSound, begForFoodSound, purrSound, purreowSound);
      }

      public void encode(NBTCompound tag, PacketWrapper<?> wrapper, CatSoundSet value) throws NbtCodecException {
         tag.set("ambient_sound", value.ambientSound, Sound.CODEC, wrapper);
         tag.set("stray_ambient_sound", value.strayAmbientSound, Sound.CODEC, wrapper);
         tag.set("hiss_sound", value.hissSound, Sound.CODEC, wrapper);
         tag.set("hurt_sound", value.hurtSound, Sound.CODEC, wrapper);
         tag.set("death_sound", value.deathSound, Sound.CODEC, wrapper);
         tag.set("eat_sound", value.eatSound, Sound.CODEC, wrapper);
         tag.set("beg_for_food_sound", value.begForFoodSound, Sound.CODEC, wrapper);
         tag.set("purr_sound", value.purrSound, Sound.CODEC, wrapper);
         tag.set("purreow_sound", value.purreowSound, Sound.CODEC, wrapper);
      }
   }).codec();
   private final Sound ambientSound;
   private final Sound strayAmbientSound;
   private final Sound hissSound;
   private final Sound hurtSound;
   private final Sound deathSound;
   private final Sound eatSound;
   private final Sound begForFoodSound;
   private final Sound purrSound;
   private final Sound purreowSound;

   public CatSoundSet(
      Sound ambientSound,
      Sound strayAmbientSound,
      Sound hissSound,
      Sound hurtSound,
      Sound deathSound,
      Sound eatSound,
      Sound begForFoodSound,
      Sound purrSound,
      Sound purreowSound
   ) {
      this.ambientSound = ambientSound;
      this.strayAmbientSound = strayAmbientSound;
      this.hissSound = hissSound;
      this.hurtSound = hurtSound;
      this.deathSound = deathSound;
      this.eatSound = eatSound;
      this.begForFoodSound = begForFoodSound;
      this.purrSound = purrSound;
      this.purreowSound = purreowSound;
   }

   @Internal
   public static CatSoundSet getOrThrow(String soundPrefix) {
      VersionedRegistry<Sound> soundRegistry = Sounds.getRegistry();
      Sound ambientSound = soundRegistry.getByNameOrThrow(soundPrefix + "ambient");
      Sound strayAmbientSound = soundRegistry.getByNameOrThrow(soundPrefix + "stray_ambient");
      Sound hissSound = soundRegistry.getByNameOrThrow(soundPrefix + "hiss");
      Sound hurtSound = soundRegistry.getByNameOrThrow(soundPrefix + "hurt");
      Sound deathSound = soundRegistry.getByNameOrThrow(soundPrefix + "death");
      Sound eatSound = soundRegistry.getByNameOrThrow(soundPrefix + "eat");
      Sound begForFoodSound = soundRegistry.getByNameOrThrow(soundPrefix + "beg_for_food");
      Sound purrSound = soundRegistry.getByNameOrThrow(soundPrefix + "purr");
      Sound purreowSound = soundRegistry.getByNameOrThrow(soundPrefix + "purreow");
      return new CatSoundSet(ambientSound, strayAmbientSound, hissSound, hurtSound, deathSound, eatSound, begForFoodSound, purrSound, purreowSound);
   }

   public Sound getAmbientSound() {
      return this.ambientSound;
   }

   public Sound getStrayAmbientSound() {
      return this.strayAmbientSound;
   }

   public Sound getHissSound() {
      return this.hissSound;
   }

   public Sound getHurtSound() {
      return this.hurtSound;
   }

   public Sound getDeathSound() {
      return this.deathSound;
   }

   public Sound getEatSound() {
      return this.eatSound;
   }

   public Sound getBegForFoodSound() {
      return this.begForFoodSound;
   }

   public Sound getPurrSound() {
      return this.purrSound;
   }

   public Sound getPurreowSound() {
      return this.purreowSound;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof CatSoundSet)) {
         return false;
      } else {
         CatSoundSet that = (CatSoundSet)obj;
         if (!this.ambientSound.equals(that.ambientSound)) {
            return false;
         } else if (!this.strayAmbientSound.equals(that.strayAmbientSound)) {
            return false;
         } else if (!this.hissSound.equals(that.hissSound)) {
            return false;
         } else if (!this.hurtSound.equals(that.hurtSound)) {
            return false;
         } else if (!this.deathSound.equals(that.deathSound)) {
            return false;
         } else if (!this.eatSound.equals(that.eatSound)) {
            return false;
         } else if (!this.begForFoodSound.equals(that.begForFoodSound)) {
            return false;
         } else {
            return !this.purrSound.equals(that.purrSound) ? false : this.purreowSound.equals(that.purreowSound);
         }
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.ambientSound,
         this.strayAmbientSound,
         this.hissSound,
         this.hurtSound,
         this.deathSound,
         this.eatSound,
         this.begForFoodSound,
         this.purrSound,
         this.purreowSound
      );
   }
}
