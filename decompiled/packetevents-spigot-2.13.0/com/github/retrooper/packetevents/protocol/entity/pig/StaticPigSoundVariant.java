package com.github.retrooper.packetevents.protocol.entity.pig;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import java.util.Objects;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class StaticPigSoundVariant extends AbstractMappedEntity implements PigSoundVariant {
   private final PigSoundSet adultSounds;
   private final PigSoundSet babySounds;

   public StaticPigSoundVariant(PigSoundSet adultSounds, PigSoundSet babySounds) {
      this(null, adultSounds, babySounds);
   }

   @Internal
   public StaticPigSoundVariant(@Nullable TypesBuilderData data, PigSoundSet adultSounds, PigSoundSet babySounds) {
      super(data);
      this.adultSounds = adultSounds;
      this.babySounds = babySounds;
   }

   public PigSoundVariant copy(@Nullable TypesBuilderData newData) {
      return new StaticPigSoundVariant(newData, this.adultSounds, this.babySounds);
   }

   @Override
   public PigSoundSet getAdultSounds() {
      return this.adultSounds;
   }

   @Override
   public PigSoundSet getBabySounds() {
      return this.babySounds;
   }

   @Override
   public boolean deepEquals(@Nullable Object obj) {
      if (!(obj instanceof StaticPigSoundVariant)) {
         return false;
      } else if (!super.equals(obj)) {
         return false;
      } else {
         StaticPigSoundVariant that = (StaticPigSoundVariant)obj;
         return !this.adultSounds.equals(that.adultSounds) ? false : this.babySounds.equals(that.babySounds);
      }
   }

   @Override
   public int deepHashCode() {
      return Objects.hash(super.hashCode(), this.adultSounds, this.babySounds);
   }
}
