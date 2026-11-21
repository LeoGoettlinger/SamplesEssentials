package com.viaversion.viaversion.api.type.types.item;

import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public final class ItemCostType1_20_5 extends Type<Item> {
   private final Type<StructuredData<?>[]> dataArrayType;

   public ItemCostType1_20_5(Type<StructuredData<?>[]> dataArrayType) {
      super(Item.class);
      this.dataArrayType = dataArrayType;
   }

   public Item read(ByteBuf buffer) {
      int id = Types.VAR_INT.readPrimitive(buffer);
      int amount = Types.VAR_INT.readPrimitive(buffer);
      StructuredData<?>[] dataArray = this.dataArrayType.read(buffer);
      return new StructuredItem(id, amount, new StructuredDataContainer(dataArray));
   }

   public void write(ByteBuf buffer, Item object) {
      Types.VAR_INT.writePrimitive(buffer, object.identifier());
      Types.VAR_INT.writePrimitive(buffer, object.amount());
      this.dataArrayType.write(buffer, object.dataContainer().data().values().stream().filter(StructuredData::isPresent).toArray(StructuredData[]::new));
   }

   public static final class OptionalItemCostType extends OptionalType<Item> {
      public OptionalItemCostType(Type<Item> type) {
         super(type);
      }
   }
}
