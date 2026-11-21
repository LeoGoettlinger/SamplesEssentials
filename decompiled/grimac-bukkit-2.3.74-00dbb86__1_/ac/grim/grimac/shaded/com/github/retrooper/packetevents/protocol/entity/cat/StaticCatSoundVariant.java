package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.cat;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class StaticCatSoundVariant extends AbstractMappedEntity implements CatSoundVariant {
   private final CatSoundSet adultSounds;
   private final CatSoundSet babySounds;

   public StaticCatSoundVariant(CatSoundSet adultSounds, CatSoundSet babySounds) {
      this(null, adultSounds, babySounds);
   }

   @ApiStatus.Internal
   public StaticCatSoundVariant(@Nullable TypesBuilderData data, CatSoundSet adultSounds, CatSoundSet babySounds) {
      super(data);
      this.adultSounds = adultSounds;
      this.babySounds = babySounds;
   }

   public CatSoundVariant copy(@Nullable TypesBuilderData newData) {
      return new StaticCatSoundVariant(newData, this.adultSounds, this.babySounds);
   }

   @Override
   public CatSoundSet getAdultSounds() {
      return this.adultSounds;
   }

   @Override
   public CatSoundSet getBabySounds() {
      return this.babySounds;
   }

   @Override
   public boolean deepEquals(@Nullable Object obj) {
      if (!(obj instanceof StaticCatSoundVariant)) {
         return false;
      } else if (!super.equals(obj)) {
         return false;
      } else {
         StaticCatSoundVariant that = (StaticCatSoundVariant)obj;
         return !this.adultSounds.equals(that.adultSounds) ? false : this.babySounds.equals(that.babySounds);
      }
   }

   @Override
   public int deepHashCode() {
      return Objects.hash(super.hashCode(), this.adultSounds, this.babySounds);
   }
}
