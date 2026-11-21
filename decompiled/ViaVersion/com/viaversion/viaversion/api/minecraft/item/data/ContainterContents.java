package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;

public final class ContainterContents {
   public static final class ContainerContentsType extends ArrayType<Item> {
      private final Type<Item> itemType;

      public ContainerContentsType(Type<Item> itemType) {
         super(itemType, 256);
         this.itemType = itemType;
      }

      public void write(Ops ops, Item[] value) {
         ops.writeList(list -> {
            for (int i = 0; i < value.length; i++) {
               Item item = value[i];
               if (item != null && !item.isEmpty()) {
                  int slot = i;
                  list.writeMap(map -> map.write("slot", Types.INT, slot).write("item", this.itemType, item));
               }
            }
         });
      }
   }
}
