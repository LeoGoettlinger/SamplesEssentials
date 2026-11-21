package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.TransformingType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import java.util.function.Function;

public record VillagerFood(int nutrition) {
   public static final Type<VillagerFood> TYPE = new TransformingType<Integer, VillagerFood>(
      Types.VAR_INT, VillagerFood.class, VillagerFood::new, VillagerFood::nutrition
   ) {
      public void write(Ops ops, VillagerFood value) {
         ops.writeMap(map -> map.write("nutrition", Types.VAR_INT, value.nutrition));
      }
   };
}
