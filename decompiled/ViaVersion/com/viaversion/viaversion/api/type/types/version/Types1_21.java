package com.viaversion.viaversion.api.type.types.version;

import com.viaversion.viaversion.api.minecraft.data.version.StructuredDataKeys1_20_5;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.item.ItemType1_20_5;
import java.util.function.Function;

public final class Types1_21 extends Types1_20_5<StructuredDataKeys1_20_5, EntityDataTypes1_21> {
   public final Type<Item> optionalItem = (ItemType1_20_5)this.item.new OptionalItemType();

   public Types1_21(
      Function<VersionedTypesHolder, StructuredDataKeys1_20_5> keysSupplier, Function<VersionedTypesHolder, EntityDataTypes1_21> entityDataTypesSupplier
   ) {
      super(keysSupplier, entityDataTypesSupplier);
   }
}
