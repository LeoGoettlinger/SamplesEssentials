package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.cow;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.CopyableEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.DeepComparableEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtCodec;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface CowSoundVariant extends MappedEntity, CopyableEntity<CowSoundVariant>, DeepComparableEntity {
   NbtCodec<CowSoundVariant> CODEC = CowSoundSet.CODEC.apply(StaticCowSoundVariant::new, CowSoundVariant::getSounds);

   static CowSoundVariant read(PacketWrapper<?> wrapper) {
      return wrapper.readMappedEntity(CowSoundVariants.getRegistry());
   }

   static void write(PacketWrapper<?> wrapper, CowSoundVariant variant) {
      wrapper.writeMappedEntity(variant);
   }

   CowSoundSet getSounds();
}
