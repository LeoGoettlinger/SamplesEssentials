package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.valueproviders.floats;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtMapCodec;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@ApiStatus.Internal
final class StaticFloatProviderType<T extends FloatProvider> extends AbstractMappedEntity implements FloatProviderType<T> {
   private final NbtMapCodec<T> codec;

   public StaticFloatProviderType(@Nullable TypesBuilderData data, NbtMapCodec<T> codec) {
      super(data);
      this.codec = codec;
   }

   @Override
   public NbtMapCodec<T> getCodec() {
      return this.codec;
   }
}
