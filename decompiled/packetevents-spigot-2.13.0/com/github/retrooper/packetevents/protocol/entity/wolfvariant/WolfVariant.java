package com.github.retrooper.packetevents.protocol.entity.wolfvariant;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.mapper.CopyableEntity;
import com.github.retrooper.packetevents.protocol.mapper.DeepComparableEntity;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntitySet;
import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.util.NbtCodec;
import com.github.retrooper.packetevents.protocol.util.NbtCodecException;
import com.github.retrooper.packetevents.protocol.util.NbtMapCodec;
import com.github.retrooper.packetevents.protocol.world.biome.Biome;
import com.github.retrooper.packetevents.protocol.world.biome.Biomes;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.ApiStatus.Obsolete;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface WolfVariant extends MappedEntity, CopyableEntity<WolfVariant>, DeepComparableEntity {
   NbtCodec<WolfVariant> CODEC = (new NbtMapCodec<WolfVariant>() {
         public WolfVariant decode(NBTCompound tag, PacketWrapper<?> wrapper) throws NbtCodecException {
            if (wrapper.getServerVersion().isOlderThan(ServerVersion.V_1_21_5)) {
               WolfAssetSet assets = WolfAssetSet.MAP_CODEC.decode(tag, wrapper);
               MappedEntitySet<Biome> biomes = tag.getOrThrow("biomes", (itag, ew) -> MappedEntitySet.decode(itag, ew, Biomes.getRegistry()), wrapper);
               return new StaticWolfVariant(assets, biomes);
            } else {
               WolfAssetSet assets = tag.getOrThrow("assets", WolfAssetSet.CODEC, wrapper);
               WolfAssetSet babyAssets = wrapper.getServerVersion().isOlderThan(ServerVersion.V_26_1)
                  ? assets
                  : tag.getOrThrow("baby_assets", WolfAssetSet.CODEC, wrapper);
               return new StaticWolfVariant(assets, babyAssets);
            }
         }

         public void encode(NBTCompound tag, PacketWrapper<?> wrapper, WolfVariant value) throws NbtCodecException {
            if (wrapper.getServerVersion().isOlderThan(ServerVersion.V_1_21_5)) {
               WolfAssetSet.MAP_CODEC.encode(tag, wrapper, value.getAssets());
               tag.set("biomes", value.getBiomes(), MappedEntitySet::encode, wrapper);
            } else {
               tag.set("assets", value.getAssets(), WolfAssetSet.CODEC, wrapper);
               if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_26_1)) {
                  tag.set("baby_assets", value.getBabyAssets(), WolfAssetSet.CODEC, wrapper);
               }
            }
         }
      })
      .codec();

   WolfAssetSet getAssets();

   WolfAssetSet getBabyAssets();

   @Obsolete
   MappedEntitySet<Biome> getBiomes();

   @Deprecated
   default ResourceLocation getWildTexture() {
      return this.getAssets().getWildId();
   }

   @Deprecated
   default ResourceLocation getTameTexture() {
      return this.getAssets().getTameId();
   }

   @Deprecated
   default ResourceLocation getAngryTexture() {
      return this.getAssets().getAngryId();
   }

   static WolfVariant read(PacketWrapper<?> wrapper) {
      return !wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_5) && !wrapper.getServerVersion().isOlderThan(ServerVersion.V_1_21)
         ? wrapper.readMappedEntityOrDirect(WolfVariants.getRegistry(), WolfVariant::readDirect)
         : wrapper.readMappedEntity(WolfVariants.getRegistry());
   }

   static void write(PacketWrapper<?> wrapper, WolfVariant variant) {
      if (!wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_5) && !wrapper.getServerVersion().isOlderThan(ServerVersion.V_1_21)) {
         wrapper.writeMappedEntityOrDirect(variant, WolfVariant::writeDirect);
      } else {
         wrapper.writeMappedEntity(variant);
      }
   }

   @Deprecated
   static WolfVariant readDirect(PacketWrapper<?> wrapper) {
      ResourceLocation wildTexture = wrapper.readIdentifier();
      ResourceLocation tameTexture = wrapper.readIdentifier();
      ResourceLocation angryTexture = wrapper.readIdentifier();
      MappedEntitySet<Biome> biomes = MappedEntitySet.read(wrapper, Biomes.getRegistry());
      return new StaticWolfVariant(wildTexture, tameTexture, angryTexture, biomes);
   }

   @Deprecated
   static void writeDirect(PacketWrapper<?> wrapper, WolfVariant variant) {
      wrapper.writeIdentifier(variant.getWildTexture());
      wrapper.writeIdentifier(variant.getTameTexture());
      wrapper.writeIdentifier(variant.getAngryTexture());
      MappedEntitySet.write(wrapper, variant.getBiomes());
   }

   @Deprecated
   static WolfVariant decode(NBT tag, ClientVersion version, @Nullable TypesBuilderData data) {
      return decode(tag, PacketWrapper.createDummyWrapper(version), data);
   }

   @Deprecated
   static WolfVariant decode(NBT tag, PacketWrapper<?> wrapper, @Nullable TypesBuilderData data) {
      return CODEC.decode(tag, wrapper).copy(data);
   }

   @Deprecated
   static NBT encode(WolfVariant variant, ClientVersion version) {
      return encode(PacketWrapper.createDummyWrapper(version), variant);
   }

   @Deprecated
   static NBT encode(PacketWrapper<?> wrapper, WolfVariant variant) {
      return CODEC.encode(wrapper, variant);
   }
}
