package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.cow;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class StaticCowSoundVariant extends AbstractMappedEntity implements CowSoundVariant {
   private final CowSoundSet sounds;

   public StaticCowSoundVariant(CowSoundSet sounds) {
      this(null, sounds);
   }

   @ApiStatus.Internal
   public StaticCowSoundVariant(@Nullable TypesBuilderData data, CowSoundSet sounds) {
      super(data);
      this.sounds = sounds;
   }

   @Override
   public CowSoundSet getSounds() {
      return this.sounds;
   }

   public CowSoundVariant copy(@Nullable TypesBuilderData newData) {
      return new StaticCowSoundVariant(newData, this.sounds);
   }

   @Override
   public boolean deepEquals(@Nullable Object obj) {
      if (!(obj instanceof StaticCowSoundVariant)) {
         return false;
      } else if (!super.equals(obj)) {
         return false;
      } else {
         StaticCowSoundVariant that = (StaticCowSoundVariant)obj;
         return this.sounds.equals(that.sounds);
      }
   }

   @Override
   public int deepHashCode() {
      return Objects.hash(super.hashCode(), this.sounds);
   }
}
