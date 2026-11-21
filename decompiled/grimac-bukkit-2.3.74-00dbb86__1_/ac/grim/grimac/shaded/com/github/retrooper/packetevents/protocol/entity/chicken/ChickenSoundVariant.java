package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.chicken;

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
public interface ChickenSoundVariant extends MappedEntity, CopyableEntity<ChickenSoundVariant>, DeepComparableEntity {
   NbtCodec<ChickenSoundVariant> CODEC = (new NbtMapCodec<ChickenSoundVariant>() {
      public ChickenSoundVariant decode(NBTCompound tag, PacketWrapper<?> wrapper) throws NbtCodecException {
         ChickenSoundSet adultSounds = tag.getOrThrow("adult_sounds", ChickenSoundSet.CODEC, wrapper);
         ChickenSoundSet babySounds = tag.getOrThrow("baby_sounds", ChickenSoundSet.CODEC, wrapper);
         return new StaticChickenSoundVariant(adultSounds, babySounds);
      }

      public void encode(NBTCompound tag, PacketWrapper<?> wrapper, ChickenSoundVariant value) throws NbtCodecException {
         tag.set("adult_sounds", value.getAdultSounds(), ChickenSoundSet.CODEC, wrapper);
         tag.set("baby_sounds", value.getBabySounds(), ChickenSoundSet.CODEC, wrapper);
      }
   }).codec();

   static ChickenSoundVariant read(PacketWrapper<?> wrapper) {
      return wrapper.readMappedEntity(ChickenSoundVariants.getRegistry());
   }

   static void write(PacketWrapper<?> wrapper, ChickenSoundVariant variant) {
      wrapper.writeMappedEntity(variant);
   }

   ChickenSoundSet getAdultSounds();

   ChickenSoundSet getBabySounds();
}
