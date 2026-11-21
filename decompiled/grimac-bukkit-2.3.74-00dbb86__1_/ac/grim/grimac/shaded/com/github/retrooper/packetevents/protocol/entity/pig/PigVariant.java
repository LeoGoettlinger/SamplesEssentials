package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.pig;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.CopyableEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.DeepComparableEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBT;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.CodecNameable;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtCodec;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtCodecException;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtCodecs;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtMapCodec;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.resources.ResourceLocation;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.adventure.AdventureIndexUtil;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import ac.grim.grimac.shaded.kyori.adventure.util.Index;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface PigVariant extends MappedEntity, CopyableEntity<PigVariant>, DeepComparableEntity {
   NbtCodec<PigVariant> CODEC = (new NbtMapCodec<PigVariant>() {
         public PigVariant decode(NBTCompound tag, PacketWrapper<?> wrapper) throws NbtCodecException {
            PigVariant.ModelType modelType = tag.getOr("model", PigVariant.ModelType.CODEC, PigVariant.ModelType.NORMAL, wrapper);
            ResourceLocation assetId = tag.getOrThrow("asset_id", ResourceLocation.CODEC, wrapper);
            ResourceLocation babyAssetId = wrapper.getServerVersion().isOlderThan(ServerVersion.V_26_1)
               ? assetId
               : tag.getOrThrow("baby_asset_id", ResourceLocation.CODEC, wrapper);
            return new StaticPigVariant(modelType, assetId, babyAssetId);
         }

         public void encode(NBTCompound tag, PacketWrapper<?> wrapper, PigVariant value) throws NbtCodecException {
            tag.set("model", value.getModelType(), PigVariant.ModelType.CODEC, wrapper);
            tag.set("asset_id", value.getAssetId(), ResourceLocation.CODEC, wrapper);
            if (wrapper.getServerVersion().isNewerThan(ServerVersion.V_26_1)) {
               tag.set("baby_asset_id", value.getBabyAssetId(), ResourceLocation.CODEC, wrapper);
            }
         }
      })
      .codec();

   PigVariant.ModelType getModelType();

   ResourceLocation getAssetId();

   ResourceLocation getBabyAssetId();

   static PigVariant read(PacketWrapper<?> wrapper) {
      return wrapper.readMappedEntity(PigVariants.getRegistry());
   }

   static void write(PacketWrapper<?> wrapper, PigVariant variant) {
      wrapper.writeMappedEntity(variant);
   }

   static PigVariant decode(NBT tag, ClientVersion version, @Nullable TypesBuilderData data) {
      return CODEC.decode(tag, PacketWrapper.createDummyWrapper(version)).copy(data);
   }

   static NBT encode(PigVariant variant, ClientVersion version) {
      return CODEC.encode(PacketWrapper.createDummyWrapper(version), variant);
   }

   public static enum ModelType implements CodecNameable {
      NORMAL("normal"),
      COLD("cold");

      private static final Index<String, PigVariant.ModelType> NAME_INDEX = Index.create(PigVariant.ModelType.class, PigVariant.ModelType::getName);
      public static final NbtCodec<PigVariant.ModelType> CODEC = NbtCodecs.forEnum(values());
      private final String name;

      private ModelType(String name) {
         this.name = name;
      }

      public static PigVariant.ModelType getByName(String name) {
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
