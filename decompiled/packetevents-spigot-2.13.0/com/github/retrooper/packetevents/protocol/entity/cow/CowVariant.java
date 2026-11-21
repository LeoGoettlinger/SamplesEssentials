package com.github.retrooper.packetevents.protocol.entity.cow;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.mapper.CopyableEntity;
import com.github.retrooper.packetevents.protocol.mapper.DeepComparableEntity;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.util.CodecNameable;
import com.github.retrooper.packetevents.protocol.util.NbtCodec;
import com.github.retrooper.packetevents.protocol.util.NbtCodecException;
import com.github.retrooper.packetevents.protocol.util.NbtCodecs;
import com.github.retrooper.packetevents.protocol.util.NbtMapCodec;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.adventure.AdventureIndexUtil;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import net.kyori.adventure.util.Index;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface CowVariant extends MappedEntity, CopyableEntity<CowVariant>, DeepComparableEntity {
   NbtCodec<CowVariant> CODEC = (new NbtMapCodec<CowVariant>() {
         public CowVariant decode(NBTCompound tag, PacketWrapper<?> wrapper) throws NbtCodecException {
            CowVariant.ModelType modelType = tag.getOr("model", CowVariant.ModelType.CODEC, CowVariant.ModelType.NORMAL, wrapper);
            ResourceLocation assetId = tag.getOrThrow("asset_id", ResourceLocation.CODEC, wrapper);
            ResourceLocation babyAssetId = wrapper.getServerVersion().isOlderThan(ServerVersion.V_26_1)
               ? assetId
               : tag.getOrThrow("baby_asset_id", ResourceLocation.CODEC, wrapper);
            return new StaticCowVariant(modelType, assetId, babyAssetId);
         }

         public void encode(NBTCompound tag, PacketWrapper<?> wrapper, CowVariant value) throws NbtCodecException {
            tag.set("model", value.getModelType(), CowVariant.ModelType.CODEC, wrapper);
            tag.set("asset_id", value.getAssetId(), ResourceLocation.CODEC, wrapper);
            if (wrapper.getServerVersion().isNewerThan(ServerVersion.V_26_1)) {
               tag.set("baby_asset_id", value.getBabyAssetId(), ResourceLocation.CODEC, wrapper);
            }
         }
      })
      .codec();

   CowVariant.ModelType getModelType();

   ResourceLocation getAssetId();

   ResourceLocation getBabyAssetId();

   static CowVariant read(PacketWrapper<?> wrapper) {
      return wrapper.readMappedEntity(CowVariants.getRegistry());
   }

   static void write(PacketWrapper<?> wrapper, CowVariant variant) {
      wrapper.writeMappedEntity(variant);
   }

   @Deprecated
   static CowVariant decode(NBT tag, ClientVersion version, @Nullable TypesBuilderData data) {
      return CODEC.decode(tag, PacketWrapper.createDummyWrapper(version)).copy(data);
   }

   @Deprecated
   static NBT encode(CowVariant variant, ClientVersion version) {
      return CODEC.encode(PacketWrapper.createDummyWrapper(version), variant);
   }

   public static enum ModelType implements CodecNameable {
      NORMAL("normal"),
      COLD("cold"),
      WARM("warm");

      private static final Index<String, CowVariant.ModelType> NAME_INDEX = Index.create(CowVariant.ModelType.class, CowVariant.ModelType::getName);
      public static final NbtCodec<CowVariant.ModelType> CODEC = NbtCodecs.forEnum(values());
      private final String name;

      private ModelType(String name) {
         this.name = name;
      }

      public static CowVariant.ModelType getByName(String name) {
         return AdventureIndexUtil.indexValueOrThrow(NAME_INDEX, name);
      }

      public String getName() {
         return this.name;
      }

      @Override
      public String getCodecName() {
         return this.name;
      }
   }
}
