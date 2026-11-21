package com.github.retrooper.packetevents.protocol.entity.frog;

import com.github.retrooper.packetevents.protocol.mapper.CopyableEntity;
import com.github.retrooper.packetevents.protocol.mapper.DeepComparableEntity;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface FrogVariant extends MappedEntity, CopyableEntity<FrogVariant>, DeepComparableEntity {
   ResourceLocation getAssetId();

   static FrogVariant read(PacketWrapper<?> wrapper) {
      return wrapper.readMappedEntity(FrogVariants.getRegistry());
   }

   static void write(PacketWrapper<?> wrapper, FrogVariant variant) {
      wrapper.writeMappedEntity(variant);
   }

   static FrogVariant decode(NBT nbt, ClientVersion version, @Nullable TypesBuilderData data) {
      NBTCompound compound = (NBTCompound)nbt;
      ResourceLocation assetId = new ResourceLocation(compound.getStringTagValueOrThrow("asset_id"));
      return new StaticFrogVariant(data, assetId);
   }

   static NBT encode(FrogVariant variant, ClientVersion version) {
      NBTCompound compound = new NBTCompound();
      compound.setTag("asset_id", new NBTString(variant.getAssetId().toString()));
      return compound;
   }
}
