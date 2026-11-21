package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.pig;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.CopyableEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.DeepComparableEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtCodec;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtCodecException;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtMapCodec;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface PigSoundVariant extends MappedEntity, CopyableEntity<PigSoundVariant>, DeepComparableEntity {
   NbtCodec<PigSoundVariant> CODEC = (new NbtMapCodec<PigSoundVariant>() {
      public PigSoundVariant decode(NBTCompound tag, PacketWrapper<?> wrapper) throws NbtCodecException {
         PigSoundSet adultSounds = tag.getOrThrow("adult_sounds", PigSoundSet.CODEC, wrapper);
         PigSoundSet babySounds = tag.getOrThrow("baby_sounds", PigSoundSet.CODEC, wrapper);
         return new StaticPigSoundVariant(adultSounds, babySounds);
      }

      public void encode(NBTCompound tag, PacketWrapper<?> wrapper, PigSoundVariant value) throws NbtCodecException {
         tag.set("adult_sounds", value.getAdultSounds(), PigSoundSet.CODEC, wrapper);
         tag.set("baby_sounds", value.getBabySounds(), PigSoundSet.CODEC, wrapper);
      }
   }).codec();

   static PigSoundVariant read(PacketWrapper<?> wrapper) {
      return wrapper.readMappedEntity(PigSoundVariants.getRegistry());
   }

   static void write(PacketWrapper<?> wrapper, PigSoundVariant variant) {
      wrapper.writeMappedEntity(variant);
   }

   PigSoundSet getAdultSounds();

   PigSoundSet getBabySounds();
}
