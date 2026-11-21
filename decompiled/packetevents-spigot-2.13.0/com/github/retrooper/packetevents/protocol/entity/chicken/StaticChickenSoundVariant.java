package com.github.retrooper.packetevents.protocol.entity.chicken;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import java.util.Objects;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class StaticChickenSoundVariant extends AbstractMappedEntity implements ChickenSoundVariant {
   private final ChickenSoundSet adultSounds;
   private final ChickenSoundSet babySounds;

   public StaticChickenSoundVariant(ChickenSoundSet adultSounds, ChickenSoundSet babySounds) {
      this(null, adultSounds, babySounds);
   }

   @Internal
   public StaticChickenSoundVariant(@Nullable TypesBuilderData data, ChickenSoundSet adultSounds, ChickenSoundSet babySounds) {
      super(data);
      this.adultSounds = adultSounds;
      this.babySounds = babySounds;
   }

   public ChickenSoundVariant copy(@Nullable TypesBuilderData newData) {
      return new StaticChickenSoundVariant(newData, this.adultSounds, this.babySounds);
   }

   @Override
   public ChickenSoundSet getAdultSounds() {
      return this.adultSounds;
   }

   @Override
   public ChickenSoundSet getBabySounds() {
      return this.babySounds;
   }

   @Override
   public boolean deepEquals(@Nullable Object obj) {
      if (!(obj instanceof StaticChickenSoundVariant)) {
         return false;
      } else if (!super.equals(obj)) {
         return false;
      } else {
         StaticChickenSoundVariant that = (StaticChickenSoundVariant)obj;
         return !this.adultSounds.equals(that.adultSounds) ? false : this.babySounds.equals(that.babySounds);
      }
   }

   @Override
   public int deepHashCode() {
      return Objects.hash(super.hashCode(), this.adultSounds, this.babySounds);
   }
}
