package com.github.retrooper.packetevents.protocol.item.banner;

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
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface BannerPattern extends MappedEntity, CopyableEntity<BannerPattern>, DeepComparableEntity {
   ResourceLocation getAssetId();

   String getTranslationKey();

   static BannerPattern read(PacketWrapper<?> wrapper) {
      return wrapper.readMappedEntityOrDirect(BannerPatterns.getRegistry(), BannerPattern::readDirect);
   }

   static void write(PacketWrapper<?> wrapper, BannerPattern pattern) {
      wrapper.writeMappedEntityOrDirect(pattern, BannerPattern::writeDirect);
   }

   static BannerPattern readDirect(PacketWrapper<?> wrapper) {
      ResourceLocation assetId = wrapper.readIdentifier();
      String translationKey = wrapper.readString();
      return new StaticBannerPattern(assetId, translationKey);
   }

   static void writeDirect(PacketWrapper<?> wrapper, BannerPattern pattern) {
      wrapper.writeIdentifier(pattern.getAssetId());
      wrapper.writeString(pattern.getTranslationKey());
   }

   static BannerPattern decode(NBT nbt, ClientVersion version, @Nullable TypesBuilderData data) {
      NBTCompound compound = (NBTCompound)nbt;
      ResourceLocation assetId = new ResourceLocation(compound.getStringTagValueOrThrow("asset_id"));
      String translationKey = compound.getStringTagValueOrThrow("translation_key");
      return new StaticBannerPattern(data, assetId, translationKey);
   }

   static NBT encode(BannerPattern bannerPattern, ClientVersion version) {
      NBTCompound compound = new NBTCompound();
      compound.setTag("asset_id", new NBTString(bannerPattern.getAssetId().toString()));
      compound.setTag("translation_key", new NBTString(bannerPattern.getTranslationKey()));
      return compound;
   }
}
