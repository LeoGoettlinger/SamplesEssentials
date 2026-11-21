package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface MappedEntityBuilder<T extends MappedEntity> {
   default T build() {
      return this.build(null);
   }

   @ApiStatus.Internal
   T build(@Nullable TypesBuilderData data);
}
