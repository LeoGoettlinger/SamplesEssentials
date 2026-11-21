package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.pig;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.sound.Sound;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.sound.Sounds;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtCodec;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtCodecException;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtMapCodec;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class PigSoundSet {
   public static final NbtCodec<PigSoundSet> CODEC = (new NbtMapCodec<PigSoundSet>() {
      public PigSoundSet decode(NBTCompound tag, PacketWrapper<?> wrapper) throws NbtCodecException {
         Sound ambientSound = tag.getOrThrow("ambient_sound", Sound.CODEC, wrapper);
         Sound hurtSound = tag.getOrThrow("hurt_sound", Sound.CODEC, wrapper);
         Sound deathSound = tag.getOrThrow("death_sound", Sound.CODEC, wrapper);
         Sound stepSound = tag.getOrThrow("step_sound", Sound.CODEC, wrapper);
         Sound eatSound = tag.getOrThrow("eat_sound", Sound.CODEC, wrapper);
         return new PigSoundSet(ambientSound, hurtSound, deathSound, stepSound, eatSound);
      }

      public void encode(NBTCompound tag, PacketWrapper<?> wrapper, PigSoundSet value) throws NbtCodecException {
         tag.set("ambient_sound", value.ambientSound, Sound.CODEC, wrapper);
         tag.set("hurt_sound", value.hurtSound, Sound.CODEC, wrapper);
         tag.set("death_sound", value.deathSound, Sound.CODEC, wrapper);
         tag.set("step_sound", value.stepSound, Sound.CODEC, wrapper);
         tag.set("eat_sound", value.eatSound, Sound.CODEC, wrapper);
      }
   }).codec();
   private final Sound ambientSound;
   private final Sound hurtSound;
   private final Sound deathSound;
   private final Sound stepSound;
   private final Sound eatSound;

   public PigSoundSet(Sound ambientSound, Sound hurtSound, Sound deathSound, Sound stepSound, Sound eatSound) {
      this.ambientSound = ambientSound;
      this.hurtSound = hurtSound;
      this.deathSound = deathSound;
      this.stepSound = stepSound;
      this.eatSound = eatSound;
   }

   @ApiStatus.Internal
   public static PigSoundSet getOrThrow(String soundPrefix, Sound stepSound) {
      VersionedRegistry<Sound> soundRegistry = Sounds.getRegistry();
      Sound ambientSound = soundRegistry.getByNameOrThrow(soundPrefix + "ambient");
      Sound hurtSound = soundRegistry.getByNameOrThrow(soundPrefix + "hurt");
      Sound deathSound = soundRegistry.getByNameOrThrow(soundPrefix + "death");
      Sound eatSound = soundRegistry.getByNameOrThrow(soundPrefix + "eat");
      return new PigSoundSet(ambientSound, hurtSound, deathSound, stepSound, eatSound);
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

   public Sound getEatSound() {
      return this.eatSound;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj != null && this.getClass() == obj.getClass()) {
         PigSoundSet that = (PigSoundSet)obj;
         if (!this.ambientSound.equals(that.ambientSound)) {
            return false;
         } else if (!this.hurtSound.equals(that.hurtSound)) {
            return false;
         } else if (!this.deathSound.equals(that.deathSound)) {
            return false;
         } else {
            return !this.stepSound.equals(that.stepSound) ? false : this.eatSound.equals(that.eatSound);
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.ambientSound, this.hurtSound, this.deathSound, this.stepSound, this.eatSound);
   }
}
