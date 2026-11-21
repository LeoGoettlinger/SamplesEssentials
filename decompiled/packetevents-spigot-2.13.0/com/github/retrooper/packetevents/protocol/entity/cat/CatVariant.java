package com.github.retrooper.packetevents.protocol.entity.cat;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.mapper.CopyableEntity;
import com.github.retrooper.packetevents.protocol.mapper.DeepComparableEntity;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.util.NbtCodec;
import com.github.retrooper.packetevents.protocol.util.NbtCodecException;
import com.github.retrooper.packetevents.protocol.util.NbtMapCodec;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface CatVariant extends MappedEntity, CopyableEntity<CatVariant>, DeepComparableEntity {
   NbtCodec<CatVariant> CODEC = (new NbtMapCodec<CatVariant>() {
         public CatVariant decode(NBTCompound tag, PacketWrapper<?> wrapper) throws NbtCodecException {
            ResourceLocation assetId = tag.getOrThrow("asset_id", ResourceLocation.CODEC, wrapper);
            ResourceLocation babyAssetId = wrapper.getServerVersion().isOlderThan(ServerVersion.V_26_1)
               ? assetId
               : tag.getOrThrow("baby_asset_id", ResourceLocation.CODEC, wrapper);
            return new StaticCatVariant(assetId, babyAssetId);
         }

         public void encode(NBTCompound tag, PacketWrapper<?> wrapper, CatVariant value) throws NbtCodecException {
            tag.set("asset_id", value.getAssetId(), ResourceLocation.CODEC, wrapper);
            if (wrapper.getServerVersion().isNewerThan(ServerVersion.V_26_1)) {
               tag.set("baby_asset_id", value.getBabyAssetId(), ResourceLocation.CODEC, wrapper);
            }
         }
      })
      .codec();

   ResourceLocation getAssetId();

   ResourceLocation getBabyAssetId();

   static CatVariant read(PacketWrapper<?> wrapper) {
      return wrapper.readMappedEntity(CatVariants.getRegistry());
   }

   static void write(PacketWrapper<?> wrapper, CatVariant variant) {
      wrapper.writeMappedEntity(variant);
   }

   @Deprecated
   static CatVariant decode(NBT tag, ClientVersion version, @Nullable TypesBuilderData data) {
      return CODEC.decode(tag, PacketWrapper.createDummyWrapper(version)).copy(data);
   }

   @Deprecated
   static NBT encode(CatVariant variant, ClientVersion version) {
      return CODEC.encode(PacketWrapper.createDummyWrapper(version), variant);
   }
}
