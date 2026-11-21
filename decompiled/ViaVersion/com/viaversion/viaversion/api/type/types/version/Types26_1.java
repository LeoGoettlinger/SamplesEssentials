package com.viaversion.viaversion.api.type.types.version;

import com.viaversion.viaversion.api.minecraft.data.version.VersionedStructuredDataKeys;
import com.viaversion.viaversion.api.minecraft.entitydata.types.AbstractEntityDataTypes;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.api.type.types.item.ItemTemplateType26_1;
import java.util.function.Function;

public class Types26_1<K extends VersionedStructuredDataKeys, E extends AbstractEntityDataTypes> extends Types1_20_5<K, E> {
   public final Type<Item> itemTemplate = new ItemTemplateType26_1(this.structuredData);
   public final Type<Item> optionalItemTemplate = new ItemTemplateType26_1.OptionalItemTemplateType(this.itemTemplate);
   public final ArrayType<Item> itemTemplateArray = new ArrayType<>(this.itemTemplate);

   public Types26_1(Function<VersionedTypesHolder, K> keysSupplier, Function<VersionedTypesHolder, E> entityDataTypesSupplier) {
      super(null, entityDataTypesSupplier);
      this.initKeys(keysSupplier);
   }

   @Override
   public Type<Item> itemTemplate() {
      return this.itemTemplate;
   }

   @Override
   public Type<Item> optionalItemTemplate() {
      return this.optionalItemTemplate;
   }

   @Override
   public ArrayType<Item> itemTemplateArray() {
      return this.itemTemplateArray;
   }
}
