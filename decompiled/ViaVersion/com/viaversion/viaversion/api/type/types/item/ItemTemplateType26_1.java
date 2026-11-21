package com.viaversion.viaversion.api.type.types.item;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItemTemplate;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import java.util.Map;

public class ItemTemplateType26_1 extends ItemType1_20_5 {
   public ItemTemplateType26_1(StructuredDataTypeBase dataType) {
      super(dataType);
   }

   @Override
   public Item read(ByteBuf buffer) {
      int id = Types.VAR_INT.readPrimitive(buffer);
      int amount = Types.VAR_INT.readPrimitive(buffer);
      Map<StructuredDataKey<?>, StructuredData<?>> data = this.readData(buffer);
      return new StructuredItemTemplate(id, amount, new StructuredDataContainer(data));
   }

   @Override
   public void write(ByteBuf buffer, Item object) {
      Types.VAR_INT.writePrimitive(buffer, object.identifier());
      Types.VAR_INT.writePrimitive(buffer, object.amount());
      this.writeData(buffer, object);
   }

   @Override
   public void write(Ops ops, Item item) {
      ops.writeMap(map -> {
         map.write("id", Types.IDENTIFIER, ops.context().registryAccess().item(item.identifier())).writeOptional("count", Types.VAR_INT, item.amount(), 1);
         this.writeComponents(map, item);
      });
   }

   public static final class OptionalItemTemplateType extends OptionalType<Item> {
      public OptionalItemTemplateType(Type<Item> type) {
         super(type);
      }
   }
}
