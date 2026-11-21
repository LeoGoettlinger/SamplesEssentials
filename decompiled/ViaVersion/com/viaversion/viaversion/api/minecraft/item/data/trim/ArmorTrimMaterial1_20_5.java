package com.viaversion.viaversion.api.minecraft.item.data.trim;

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.HolderType;
import com.viaversion.viaversion.libs.fastutil.objects.Object2ObjectArrayMap;
import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;
import java.util.Map;
import java.util.Map.Entry;

public record ArmorTrimMaterial1_20_5(String assetName, int itemId, float itemModelIndex, Map<String, String> overrideArmorMaterials, Tag description)
   implements Copyable,
   Rewritable {
   public static final HolderType<ArmorTrimMaterial1_20_5> TYPE1_20_5 = new HolderType<ArmorTrimMaterial1_20_5>() {
      public ArmorTrimMaterial1_20_5 readDirect(ByteBuf buffer) {
         String assetName = Types.STRING.read(buffer);
         int item = Types.VAR_INT.readPrimitive(buffer);
         float itemModelIndex = buffer.readFloat();
         int overrideArmorMaterialsSize = Types.VAR_INT.readPrimitive(buffer);
         Map<String, String> overrideArmorMaterials = new Object2ObjectArrayMap<>();

         for (int i = 0; i < overrideArmorMaterialsSize; i++) {
            int key = Types.VAR_INT.readPrimitive(buffer);
            String value = Types.STRING.read(buffer);
            overrideArmorMaterials.put(Integer.toString(key), value);
         }

         Tag description = Types.TAG.read(buffer);
         return new ArmorTrimMaterial1_20_5(assetName, item, itemModelIndex, overrideArmorMaterials, description);
      }

      public void writeDirect(ByteBuf buffer, ArmorTrimMaterial1_20_5 value) {
         Types.STRING.write(buffer, value.assetName());
         Types.VAR_INT.writePrimitive(buffer, value.itemId());
         buffer.writeFloat(value.itemModelIndex());
         Types.VAR_INT.writePrimitive(buffer, value.overrideArmorMaterials().size());

         for (Entry<String, String> entry : value.overrideArmorMaterials().entrySet()) {
            Types.VAR_INT.writePrimitive(buffer, Integer.parseInt(entry.getKey()));
            Types.STRING.write(buffer, entry.getValue());
         }

         Types.TAG.write(buffer, value.description());
      }
   };
   public static final HolderType<ArmorTrimMaterial1_20_5> TYPE1_21_2 = new HolderType<ArmorTrimMaterial1_20_5>() {
      public ArmorTrimMaterial1_20_5 readDirect(ByteBuf buffer) {
         String assetName = Types.STRING.read(buffer);
         int item = Types.VAR_INT.readPrimitive(buffer);
         float itemModelIndex = buffer.readFloat();
         int overrideArmorMaterialsSize = Types.VAR_INT.readPrimitive(buffer);
         Map<String, String> overrideArmorMaterials = new Object2ObjectArrayMap<>();

         for (int i = 0; i < overrideArmorMaterialsSize; i++) {
            String key = Types.STRING.read(buffer);
            String value = Types.STRING.read(buffer);
            overrideArmorMaterials.put(key, value);
         }

         Tag description = Types.TAG.read(buffer);
         return new ArmorTrimMaterial1_20_5(assetName, item, itemModelIndex, overrideArmorMaterials, description);
      }

      public void writeDirect(ByteBuf buffer, ArmorTrimMaterial1_20_5 value) {
         Types.STRING.write(buffer, value.assetName());
         Types.VAR_INT.writePrimitive(buffer, value.itemId());
         buffer.writeFloat(value.itemModelIndex());
         Types.VAR_INT.writePrimitive(buffer, value.overrideArmorMaterials().size());

         for (Entry<String, String> entry : value.overrideArmorMaterials().entrySet()) {
            Types.STRING.write(buffer, entry.getKey());
            Types.STRING.write(buffer, entry.getValue());
         }

         Types.TAG.write(buffer, value.description());
      }
   };
   public static final HolderType<ArmorTrimMaterial1_20_5> TYPE1_21_4 = new HolderType<ArmorTrimMaterial1_20_5>() {
      public ArmorTrimMaterial1_20_5 readDirect(ByteBuf buffer) {
         String assetName = Types.STRING.read(buffer);
         int item = Types.VAR_INT.readPrimitive(buffer);
         int overrideArmorMaterialsSize = Types.VAR_INT.readPrimitive(buffer);
         Map<String, String> overrideArmorMaterials = new Object2ObjectArrayMap<>();

         for (int i = 0; i < overrideArmorMaterialsSize; i++) {
            String key = Types.STRING.read(buffer);
            String value = Types.STRING.read(buffer);
            overrideArmorMaterials.put(key, value);
         }

         Tag description = Types.TAG.read(buffer);
         return new ArmorTrimMaterial1_20_5(assetName, item, overrideArmorMaterials, description);
      }

      public void writeDirect(ByteBuf buffer, ArmorTrimMaterial1_20_5 value) {
         Types.STRING.write(buffer, value.assetName());
         Types.VAR_INT.writePrimitive(buffer, value.itemId());
         Types.VAR_INT.writePrimitive(buffer, value.overrideArmorMaterials().size());

         for (Entry<String, String> entry : value.overrideArmorMaterials().entrySet()) {
            Types.STRING.write(buffer, entry.getKey());
            Types.STRING.write(buffer, entry.getValue());
         }

         Types.TAG.write(buffer, value.description());
      }
   };
   public static final HolderType<ArmorTrimMaterial1_20_5> TYPE1_21_5 = new HolderType<ArmorTrimMaterial1_20_5>() {
      public ArmorTrimMaterial1_20_5 readDirect(ByteBuf buffer) {
         String assetName = Types.STRING.read(buffer);
         int overrideArmorMaterialsSize = Types.VAR_INT.readPrimitive(buffer);
         Map<String, String> overrideArmorMaterials = new Object2ObjectArrayMap<>();

         for (int i = 0; i < overrideArmorMaterialsSize; i++) {
            String key = Types.STRING.read(buffer);
            String value = Types.STRING.read(buffer);
            overrideArmorMaterials.put(key, value);
         }

         Tag description = Types.TAG.read(buffer);
         return new ArmorTrimMaterial1_20_5(assetName, overrideArmorMaterials, description);
      }

      public void writeDirect(ByteBuf buffer, ArmorTrimMaterial1_20_5 value) {
         Types.STRING.write(buffer, value.assetName());
         Types.VAR_INT.writePrimitive(buffer, value.overrideArmorMaterials().size());

         for (Entry<String, String> entry : value.overrideArmorMaterials().entrySet()) {
            Types.STRING.write(buffer, entry.getKey());
            Types.STRING.write(buffer, entry.getValue());
         }

         Types.TAG.write(buffer, value.description());
      }

      public void writeDirect(Ops ops, ArmorTrimMaterial1_20_5 object) {
         ops.writeMap(map -> {
            map.write("asset_name", Types.STRING, object.assetName());
            if (!object.overrideArmorMaterials.isEmpty()) {
               map.writeMap("override_armor_assets", materials -> {
                  for (Entry<String, String> entry : object.overrideArmorMaterials.entrySet()) {
                     materials.write(Types.IDENTIFIER, Key.of(entry.getKey()), Types.STRING, entry.getValue());
                  }
               });
            }

            map.write("description", Types.TEXT_COMPONENT_TAG, object.description());
         });
      }

      @Override
      protected Key identifier(Ops ops, int id) {
         return ops.context().registryAccess().registryKey("trim_material", id);
      }
   };

   public ArmorTrimMaterial1_20_5(String assetName, int itemId, Map<String, String> overrideArmorMaterials, Tag description) {
      this(assetName, itemId, 0.0F, overrideArmorMaterials, description);
   }

   public ArmorTrimMaterial1_20_5(String assetName, Map<String, String> overrideArmorMaterials, Tag description) {
      this(assetName, 0, 0.0F, overrideArmorMaterials, description);
   }

   public ArmorTrimMaterial1_20_5 rewrite(UserConnection connection, Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
      return new ArmorTrimMaterial1_20_5(
         this.assetName, Rewritable.rewriteItem(protocol, clientbound, this.itemId), this.itemModelIndex, this.overrideArmorMaterials, this.description
      );
   }

   public ArmorTrimMaterial1_20_5 copy() {
      return new ArmorTrimMaterial1_20_5(
         this.assetName, this.itemId, this.itemModelIndex, new Object2ObjectArrayMap<>(this.overrideArmorMaterials), this.description
      );
   }
}
