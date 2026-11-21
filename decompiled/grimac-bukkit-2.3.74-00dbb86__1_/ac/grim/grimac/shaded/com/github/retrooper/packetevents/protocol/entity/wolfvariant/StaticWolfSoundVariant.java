package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.wolfvariant;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.sound.Sound;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.sound.Sounds;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class StaticWolfSoundVariant extends AbstractMappedEntity implements WolfSoundVariant {
   private final WolfSoundSet adultSounds;
   private final WolfSoundSet babySounds;

   @Deprecated
   public StaticWolfSoundVariant(Sound ambientSound, Sound deathSound, Sound growlSound, Sound hurtSound, Sound pantSound, Sound whineSound) {
      this(new WolfSoundSet(ambientSound, deathSound, growlSound, hurtSound, pantSound, whineSound, Sounds.ENTITY_WOLF_STEP));
   }

   @ApiStatus.Obsolete
   public StaticWolfSoundVariant(WolfSoundSet sounds) {
      this(null, sounds, sounds);
   }

   public StaticWolfSoundVariant(WolfSoundSet adultSounds, WolfSoundSet babySounds) {
      this(null, adultSounds, babySounds);
   }

   @ApiStatus.Internal
   public StaticWolfSoundVariant(@Nullable TypesBuilderData data, WolfSoundSet adultSounds, WolfSoundSet babySounds) {
      super(data);
      this.adultSounds = adultSounds;
      this.babySounds = babySounds;
   }

   public WolfSoundVariant copy(@Nullable TypesBuilderData newData) {
      return new StaticWolfSoundVariant(newData, this.adultSounds, this.babySounds);
   }

   @Override
   public WolfSoundSet getAdultSounds() {
      return this.adultSounds;
   }

   @Override
   public WolfSoundSet getBabySounds() {
      return this.babySounds;
   }

   @Override
   public boolean deepEquals(@Nullable Object obj) {
      if (!(obj instanceof StaticWolfSoundVariant)) {
         return false;
      } else {
         StaticWolfSoundVariant that = (StaticWolfSoundVariant)obj;
         return !this.adultSounds.equals(that.adultSounds) ? false : this.babySounds.equals(that.babySounds);
      }
   }

   @Override
   public int deepHashCode() {
      return Objects.hash(this.adultSounds, this.babySounds);
   }
}
