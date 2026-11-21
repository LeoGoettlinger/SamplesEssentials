package com.viaversion.viaversion.protocols.v1_12_2to1_13.data;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ObjIntConsumer;

public final class BlockStates1_13 {
   public static void forEach(CompoundTag data, ObjIntConsumer<String> consumer) {
      ListTag<CompoundTag> propertyTable = data.getListTag("properties", CompoundTag.class);
      int blockStateId = 0;

      for (CompoundTag blockTag : data.getListTag("blockstates", CompoundTag.class)) {
         String name = blockTag.getString("name");
         IntArrayTag propertiesTag = blockTag.getIntArrayTag("properties");
         if (propertiesTag == null) {
            consumer.accept(name, blockStateId++);
         } else {
            List<String> states = List.of("");

            for (int propertyIndex : propertiesTag.getValue()) {
               CompoundTag property = propertyTable.get(propertyIndex);
               String propertyName = property.getString("name");
               List<String> combined = new ArrayList<>();

               for (String state : states) {
                  for (StringTag value : property.getListTag("values", StringTag.class)) {
                     String extendedState = state.isEmpty() ? propertyName + "=" + value.getValue() : state + "," + propertyName + "=" + value.getValue();
                     combined.add(extendedState);
                  }
               }

               states = combined;
            }

            for (String state : states) {
               consumer.accept(name + "[" + state + "]", blockStateId++);
            }
         }
      }
   }
}
