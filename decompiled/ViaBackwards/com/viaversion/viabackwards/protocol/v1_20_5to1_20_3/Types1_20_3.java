package com.viaversion.viabackwards.protocol.v1_20_5to1_20_3;

import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.version.VersionedStructuredDataKeys;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.types.AbstractEntityDataTypes;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.api.type.types.item.StructuredDataType;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.VersionedTypesHolder;
import java.util.List;

final class Types1_20_3 implements VersionedTypesHolder {
   public Type<Item> item() {
      return Types.ITEM1_20_2;
   }

   public ArrayType<Item> itemArray() {
      return (ArrayType<Item>)Types.ITEM1_20_2_ARRAY;
   }

   public Type<Item> itemTemplate() {
      return this.item();
   }

   public Type<Item> optionalItemTemplate() {
      return this.item();
   }

   public ArrayType<Item> itemTemplateArray() {
      return this.itemArray();
   }

   public Type<Item> itemCost() {
      return null;
   }

   public Type<Item> optionalItemCost() {
      return null;
   }

   public Type<Item> lengthPrefixedItem() {
      return null;
   }

   public StructuredDataType structuredData() {
      return null;
   }

   public Type<StructuredData<?>[]> structuredDataArray() {
      return null;
   }

   public VersionedStructuredDataKeys structuredDataKeys() {
      return null;
   }

   public ParticleType particle() {
      return com.viaversion.viaversion.api.type.types.version.Types1_20_3.PARTICLE;
   }

   public ArrayType<Particle> particles() {
      return null;
   }

   public AbstractEntityDataTypes entityDataTypes() {
      return com.viaversion.viaversion.api.type.types.version.Types1_20_3.ENTITY_DATA_TYPES;
   }

   public Type<List<EntityData>> entityDataList() {
      return com.viaversion.viaversion.api.type.types.version.Types1_20_3.ENTITY_DATA_LIST;
   }
}
