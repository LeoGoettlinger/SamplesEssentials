package com.github.retrooper.packetevents.protocol.entity.nautilus;

import com.github.retrooper.packetevents.protocol.mapper.CopyableEntity;
import com.github.retrooper.packetevents.protocol.mapper.DeepComparableEntity;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.adventure.AdventureIndexUtil;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import net.kyori.adventure.util.Index;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface ZombieNautilusVariant extends MappedEntity, CopyableEntity<ZombieNautilusVariant>, DeepComparableEntity {
   ZombieNautilusVariant.ModelType getModelType();

   ResourceLocation getAssetId();

   static ZombieNautilusVariant read(PacketWrapper<?> wrapper) {
      return wrapper.readMappedEntity(ZombieNautilusVariants.getRegistry());
   }

   static void write(PacketWrapper<?> wrapper, ZombieNautilusVariant variant) {
      wrapper.writeMappedEntity(variant);
   }

   static ZombieNautilusVariant decode(NBT nbt, ClientVersion version, @Nullable TypesBuilderData data) {
      NBTCompound compound = (NBTCompound)nbt;
      String modelTypeString = compound.getStringTagValueOrNull("model");
      ZombieNautilusVariant.ModelType modelType = modelTypeString != null
         ? ZombieNautilusVariant.ModelType.getByName(modelTypeString)
         : ZombieNautilusVariant.ModelType.NORMAL;
      ResourceLocation assetId = new ResourceLocation(compound.getStringTagValueOrThrow("asset_id"));
      return new StaticZombieNautilusVariant(data, modelType, assetId);
   }

   static NBT encode(ZombieNautilusVariant variant, ClientVersion version) {
      NBTCompound compound = new NBTCompound();
      compound.setTag("model", new NBTString(variant.getModelType().getName()));
      compound.setTag("asset_id", new NBTString(variant.getAssetId().toString()));
      return compound;
   }

   public static enum ModelType {
      NORMAL("normal"),
      WARM("warm");

      private static final Index<String, ZombieNautilusVariant.ModelType> NAME_INDEX = Index.create(
         ZombieNautilusVariant.ModelType.class, ZombieNautilusVariant.ModelType::getName
      );
      private final String name;

      private ModelType(String name) {
         this.name = name;
      }

      public static ZombieNautilusVariant.ModelType getByName(String name) {
         return AdventureIndexUtil.indexValueOrThrow(NAME_INDEX, name);
      }

      public String getName() {
         return this.name;
      }
   }
}
