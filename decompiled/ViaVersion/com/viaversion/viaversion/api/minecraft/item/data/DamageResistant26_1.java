package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.RegistryKey;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.TransformingType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.HolderSetType;
import java.util.function.Function;

public record DamageResistant26_1(HolderSet types) {
   public static final Type<DamageResistant26_1> TYPE = new TransformingType<HolderSet, DamageResistant26_1>(
      Types.HOLDER_SET, DamageResistant26_1.class, DamageResistant26_1::new, DamageResistant26_1::types
   ) {
      public void write(Ops ops, DamageResistant26_1 value) {
         ops.writeMap(map -> map.write("types", new HolderSetType(RegistryKey.of("damage_type")), value.types));
      }
   };
}
