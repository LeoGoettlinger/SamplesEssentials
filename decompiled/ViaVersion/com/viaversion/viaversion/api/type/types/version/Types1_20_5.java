package com.viaversion.viaversion.api.type.types.version;

import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.version.VersionedStructuredDataKeys;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.types.AbstractEntityDataTypes;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.api.type.types.entitydata.EntityDataListType;
import com.viaversion.viaversion.api.type.types.entitydata.EntityDataType;
import com.viaversion.viaversion.api.type.types.item.ItemCostType1_20_5;
import com.viaversion.viaversion.api.type.types.item.ItemType1_20_5;
import com.viaversion.viaversion.api.type.types.item.LengthPrefixedStructuredDataType;
import com.viaversion.viaversion.api.type.types.item.StructuredDataType;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import java.util.List;
import java.util.function.Function;

public class Types1_20_5<K extends VersionedStructuredDataKeys, E extends AbstractEntityDataTypes> implements VersionedTypesHolder {
   public final StructuredDataType structuredData = new StructuredDataType();
   public final LengthPrefixedStructuredDataType lengthPrefixedStructuredData = new LengthPrefixedStructuredDataType(this.structuredData);
   public final Type<StructuredData<?>[]> structuredDataArray = new ArrayType<>(this.structuredData);
   public final Type<Item> item = new ItemType1_20_5(this.structuredData);
   public final Type<Item> lengthPrefixedItem = new ItemType1_20_5(this.lengthPrefixedStructuredData);
   public final ArrayType<Item> itemArray = new ArrayType<>(this.item);
   public final Type<Item> itemCost = new ItemCostType1_20_5(this.structuredDataArray);
   public final Type<Item> optionalItemCost = new ItemCostType1_20_5.OptionalItemCostType(this.itemCost);
   private K structuredDataKeys;
   public final ParticleType particle = new ParticleType();
   public final ArrayType<Particle> particles = new ArrayType<>(this.particle);
   public final E entityDataTypes;
   public final Type<EntityData> entityData;
   public final Type<List<EntityData>> entityDataList;

   public Types1_20_5(Function<VersionedTypesHolder, K> keysSupplier, Function<VersionedTypesHolder, E> entityDataTypesSupplier) {
      this.entityDataTypes = entityDataTypesSupplier.apply(this);
      this.entityData = new EntityDataType(this.entityDataTypes);
      this.entityDataList = new EntityDataListType(this.entityData);
      if (keysSupplier != null) {
         this.initKeys(keysSupplier);
      }
   }

   protected void initKeys(Function<VersionedTypesHolder, K> keysSupplier) {
      this.structuredDataKeys = keysSupplier.apply(this);
   }

   @Override
   public Type<Item> item() {
      return this.item;
   }

   @Override
   public ArrayType<Item> itemArray() {
      return this.itemArray;
   }

   @Override
   public Type<Item> itemTemplate() {
      return this.item;
   }

   @Override
   public Type<Item> optionalItemTemplate() {
      return this.item;
   }

   @Override
   public ArrayType<Item> itemTemplateArray() {
      return this.itemArray;
   }

   @Override
   public Type<Item> itemCost() {
      return this.itemCost;
   }

   @Override
   public Type<Item> optionalItemCost() {
      return this.optionalItemCost;
   }

   @Override
   public Type<Item> lengthPrefixedItem() {
      return this.lengthPrefixedItem;
   }

   @Override
   public StructuredDataType structuredData() {
      return this.structuredData;
   }

   @Override
   public Type<StructuredData<?>[]> structuredDataArray() {
      return this.structuredDataArray;
   }

   @Override
   public K structuredDataKeys() {
      return this.structuredDataKeys;
   }

   @Override
   public ParticleType particle() {
      return this.particle;
   }

   @Override
   public ArrayType<Particle> particles() {
      return this.particles;
   }

   @Override
   public E entityDataTypes() {
      return this.entityDataTypes;
   }

   @Override
   public Type<List<EntityData>> entityDataList() {
      return this.entityDataList;
   }
}
