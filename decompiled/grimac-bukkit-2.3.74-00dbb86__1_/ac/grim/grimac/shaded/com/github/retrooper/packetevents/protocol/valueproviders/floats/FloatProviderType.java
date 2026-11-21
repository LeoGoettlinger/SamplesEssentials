package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.valueproviders.floats;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtCodec;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtCodecs;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtMapCodec;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface FloatProviderType<T extends FloatProvider> extends MappedEntity {
   NbtCodec<FloatProviderType<?>> CODEC = NbtCodecs.forRegistry(FloatProviderTypes.getRegistry());

   NbtMapCodec<T> getCodec();
}
