package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.ResolvableInt;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.TransformingType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import java.util.function.Function;

public record Compostable(ResolvableInt layers) {
   public static final Type<Compostable> TYPE = new TransformingType<ResolvableInt, Compostable>(
      Types.RESOLVABLE_INT, Compostable.class, Compostable::new, Compostable::layers
   ) {
      public void write(Ops ops, Compostable value) {
         ops.writeMap(map -> map.write("layers", Types.RESOLVABLE_INT, value.layers));
      }
   };
}
