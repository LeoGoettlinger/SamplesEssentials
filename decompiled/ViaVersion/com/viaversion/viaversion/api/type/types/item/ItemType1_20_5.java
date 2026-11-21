package com.viaversion.viaversion.api.type.types.item;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.fastutil.objects.Reference2ObjectOpenHashMap;
import com.viaversion.viaversion.util.Key;
import io.netty.buffer.ByteBuf;
import java.util.Map;

public class ItemType1_20_5 extends Type<Item> {
   private final StructuredDataTypeBase dataType;

   public ItemType1_20_5(StructuredDataTypeBase dataType) {
      super(Item.class);
      this.dataType = dataType;
   }

   public Item read(ByteBuf buffer) {
      int amount = Types.VAR_INT.readPrimitive(buffer);
      if (amount <= 0) {
         return StructuredItem.empty();
      } else {
         int id = Types.VAR_INT.readPrimitive(buffer);
         Map<StructuredDataKey<?>, StructuredData<?>> data = this.readData(buffer);
         return new StructuredItem(id, amount, new StructuredDataContainer(data));
      }
   }

   protected Map<StructuredDataKey<?>, StructuredData<?>> readData(ByteBuf buffer) {
      int valuesSize = Types.VAR_INT.readPrimitive(buffer);
      int markersSize = Types.VAR_INT.readPrimitive(buffer);
      if (valuesSize == 0 && markersSize == 0) {
         return new Reference2ObjectOpenHashMap<>(0);
      } else {
         Map<StructuredDataKey<?>, StructuredData<?>> map = new Reference2ObjectOpenHashMap<>(Math.min(valuesSize + markersSize, 128));

         for (int i = 0; i < valuesSize; i++) {
            StructuredData<?> value = this.dataType.read(buffer);
            StructuredDataKey<?> key = this.dataType.key(value.id());
            Preconditions.checkNotNull(key, "No data component serializer found for %s", new Object[]{value});
            map.put(key, value);
         }

         for (int i = 0; i < markersSize; i++) {
            int id = Types.VAR_INT.readPrimitive(buffer);
            StructuredDataKey<?> key = this.dataType.key(id);
            Preconditions.checkNotNull(key, "No data component serializer found for empty id %s", new Object[]{id});
            map.put(key, StructuredData.empty(key, id));
         }

         return map;
      }
   }

   public void write(ByteBuf buffer, Item object) {
      if (object.isEmpty()) {
         Types.VAR_INT.writePrimitive(buffer, 0);
      } else {
         Types.VAR_INT.writePrimitive(buffer, object.amount());
         Types.VAR_INT.writePrimitive(buffer, object.identifier());
         this.writeData(buffer, object);
      }
   }

   protected void writeData(ByteBuf buffer, Item object) {
      Map<StructuredDataKey<?>, StructuredData<?>> data = object.dataContainer().data();
      int valuesSize = 0;
      int markersSize = 0;

      for (StructuredData<?> value : data.values()) {
         if (value.isPresent()) {
            valuesSize++;
         } else {
            markersSize++;
         }
      }

      Types.VAR_INT.writePrimitive(buffer, valuesSize);
      Types.VAR_INT.writePrimitive(buffer, markersSize);

      for (StructuredData<?> valuex : data.values()) {
         if (valuex.isPresent()) {
            this.dataType.write(buffer, valuex);
         }
      }

      for (StructuredData<?> valuexx : data.values()) {
         if (valuexx.isEmpty()) {
            Types.VAR_INT.writePrimitive(buffer, valuexx.id());
         }
      }
   }

   public void write(Ops ops, Item item) {
      ops.writeMap(map -> {
         map.write("id", Types.IDENTIFIER, ops.context().registryAccess().item(item.identifier())).write("count", Types.VAR_INT, item.amount());
         this.writeComponents(map, item);
      });
   }

   protected void writeComponents(Ops.MapSerializer map, Item item) {
      if (!item.dataContainer().isEmpty()) {
         map.writeMap("components", components -> {
            for (StructuredData<?> data : item.dataContainer().data().values()) {
               String key = Key.namespaced(data.key().identifier());
               if (data.isEmpty()) {
                  key = "!" + key;
               }

               components.write(key, (Type<StructuredData<?>>)this.dataType, data);
            }
         });
      }
   }

   public final class OptionalItemType extends OptionalType<Item> {
      public OptionalItemType() {
         super(ItemType1_20_5.this);
      }
   }
}
