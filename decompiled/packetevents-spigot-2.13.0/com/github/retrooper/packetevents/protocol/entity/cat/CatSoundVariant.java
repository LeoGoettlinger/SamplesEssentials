package com.github.retrooper.packetevents.protocol.entity.cat;

import com.github.retrooper.packetevents.protocol.mapper.CopyableEntity;
import com.github.retrooper.packetevents.protocol.mapper.DeepComparableEntity;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.util.NbtCodec;
import com.github.retrooper.packetevents.protocol.util.NbtCodecException;
import com.github.retrooper.packetevents.protocol.util.NbtMapCodec;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface CatSoundVariant extends MappedEntity, CopyableEntity<CatSoundVariant>, DeepComparableEntity {
   NbtCodec<CatSoundVariant> CODEC = (new NbtMapCodec<CatSoundVariant>() {
      public CatSoundVariant decode(NBTCompound tag, PacketWrapper<?> wrapper) throws NbtCodecException {
         CatSoundSet adultSounds = tag.getOrThrow("adult_sounds", CatSoundSet.CODEC, wrapper);
         CatSoundSet babySounds = tag.getOrThrow("baby_sounds", CatSoundSet.CODEC, wrapper);
         return new StaticCatSoundVariant(adultSounds, babySounds);
      }

      public void encode(NBTCompound tag, PacketWrapper<?> wrapper, CatSoundVariant value) throws NbtCodecException {
         tag.set("adult_sounds", value.getAdultSounds(), CatSoundSet.CODEC, wrapper);
         tag.set("baby_sounds", value.getBabySounds(), CatSoundSet.CODEC, wrapper);
      }
   }).codec();

   static CatSoundVariant read(PacketWrapper<?> wrapper) {
      return wrapper.readMappedEntity(CatSoundVariants.getRegistry());
   }

   static void write(PacketWrapper<?> wrapper, CatSoundVariant variant) {
      wrapper.writeMappedEntity(variant);
   }

   CatSoundSet getAdultSounds();

   CatSoundSet getBabySounds();
}
