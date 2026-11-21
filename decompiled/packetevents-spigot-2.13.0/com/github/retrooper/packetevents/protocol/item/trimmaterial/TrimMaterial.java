package com.github.retrooper.packetevents.protocol.item.trimmaterial;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.item.armormaterial.ArmorMaterial;
import com.github.retrooper.packetevents.protocol.item.armormaterial.ArmorMaterials;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.mapper.CopyableEntity;
import com.github.retrooper.packetevents.protocol.mapper.DeepComparableEntity;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTFloat;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus.Obsolete;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface TrimMaterial extends MappedEntity, CopyableEntity<TrimMaterial>, DeepComparableEntity {
   float FALLBACK_ITEM_MODEL_INDEX = 0.0F;

   String getAssetName();

   @Obsolete
   ItemType getIngredient();

   @Obsolete
   float getItemModelIndex();

   @Nullable
   default String getArmorMaterialOverride(ArmorMaterial armorMaterial) {
      return this.getOverrideArmorMaterials().get(armorMaterial);
   }

   Map<ArmorMaterial, String> getOverrideArmorMaterials();

   Component getDescription();

   static TrimMaterial read(PacketWrapper<?> wrapper) {
      return wrapper.readMappedEntityOrDirect(TrimMaterials.getRegistry(), TrimMaterial::readDirect);
   }

   static TrimMaterial readDirect(PacketWrapper<?> wrapper) {
      String assetName = wrapper.readString();
      ItemType ingredient = wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_5) ? null : wrapper.readMappedEntity(ItemTypes::getById);
      float itemModelIndex = wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_4) ? 0.0F : wrapper.readFloat();
      Map<ArmorMaterial, String> overrideArmorMaterials = wrapper.readMap(ew -> ew.readMappedEntity(ArmorMaterials::getById), PacketWrapper::readString);
      Component description = wrapper.readComponent();
      return new StaticTrimMaterial(assetName, ingredient, itemModelIndex, overrideArmorMaterials, description);
   }

   static void write(PacketWrapper<?> wrapper, TrimMaterial material) {
      wrapper.writeMappedEntityOrDirect(material, TrimMaterial::writeDirect);
   }

   static void writeDirect(PacketWrapper<?> wrapper, TrimMaterial material) {
      wrapper.writeString(material.getAssetName());
      if (wrapper.getServerVersion().isOlderThan(ServerVersion.V_1_21_5)) {
         wrapper.writeMappedEntity(material.getIngredient());
      }

      if (wrapper.getServerVersion().isOlderThan(ServerVersion.V_1_21_4)) {
         wrapper.writeFloat(material.getItemModelIndex());
      }

      wrapper.writeMap(material.getOverrideArmorMaterials(), PacketWrapper::writeMappedEntity, PacketWrapper::writeString);
      wrapper.writeComponent(material.getDescription());
   }

   @Deprecated
   static TrimMaterial decode(NBT nbt, ClientVersion version, @Nullable TypesBuilderData data) {
      return decode(nbt, PacketWrapper.createDummyWrapper(version), data);
   }

   static TrimMaterial decode(NBT nbt, PacketWrapper<?> wrapper, @Nullable TypesBuilderData data) {
      NBTCompound compound = (NBTCompound)nbt;
      String assetName = compound.getStringTagValueOrThrow("asset_name");
      ItemType ingredient = wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_5)
         ? null
         : ItemTypes.getByName(compound.getStringTagValueOrThrow("ingredient"));
      float itemModelIndex = wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_4)
         ? 0.0F
         : compound.getNumberTagOrThrow("item_model_index").getAsFloat();
      NBTCompound overrideArmorMaterialsTag = compound.getCompoundTagOrNull("override_armor_materials");
      Map<ArmorMaterial, String> overrideArmorMaterials;
      if (overrideArmorMaterialsTag != null) {
         overrideArmorMaterials = new HashMap<>();

         for (Entry<String, NBT> entry : overrideArmorMaterialsTag.getTags().entrySet()) {
            ArmorMaterial material = ArmorMaterials.getByName(entry.getKey());
            String override = ((NBTString)entry.getValue()).getValue();
            overrideArmorMaterials.put(material, override);
         }
      } else {
         overrideArmorMaterials = Collections.emptyMap();
      }

      Component description = ((NBTCompound)nbt).getOrThrow("description", wrapper.getSerializers(), wrapper);
      return new StaticTrimMaterial(data, assetName, ingredient, itemModelIndex, overrideArmorMaterials, description);
   }

   @Deprecated
   static NBT encode(TrimMaterial material, ClientVersion version) {
      return encode(PacketWrapper.createDummyWrapper(version), material);
   }

   static NBT encode(PacketWrapper<?> wrapper, TrimMaterial material) {
      NBTCompound overrideArmorMaterialsTag;
      if (!material.getOverrideArmorMaterials().isEmpty()) {
         overrideArmorMaterialsTag = new NBTCompound();

         for (Entry<ArmorMaterial, String> entry : material.getOverrideArmorMaterials().entrySet()) {
            String materialName = entry.getKey().getName().toString();
            NBTString overrideTag = new NBTString(entry.getValue());
            overrideArmorMaterialsTag.setTag(materialName, overrideTag);
         }
      } else {
         overrideArmorMaterialsTag = null;
      }

      NBTCompound compound = new NBTCompound();
      compound.setTag("asset_name", new NBTString(material.getAssetName()));
      if (wrapper.getServerVersion().isOlderThan(ServerVersion.V_1_21_5)) {
         compound.setTag("ingredient", new NBTString(material.getIngredient().getName().toString()));
      }

      if (wrapper.getServerVersion().isOlderThan(ServerVersion.V_1_21_4)) {
         compound.setTag("item_model_index", new NBTFloat(material.getItemModelIndex()));
      }

      if (overrideArmorMaterialsTag != null) {
         compound.setTag("override_armor_materials", overrideArmorMaterialsTag);
      }

      compound.set("description", material.getDescription(), wrapper.getSerializers(), wrapper);
      return compound;
   }
}
