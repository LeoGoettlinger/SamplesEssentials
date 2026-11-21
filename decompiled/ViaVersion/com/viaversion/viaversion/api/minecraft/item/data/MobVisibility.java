package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.HolderSetType;
import io.netty.buffer.ByteBuf;

public record MobVisibility(HolderSet targetingEntityTypes, float visibility) {
   public static final Type<MobVisibility> TYPE = new Type<MobVisibility>(MobVisibility.class) {
      public MobVisibility read(ByteBuf buffer) {
         HolderSet targetingEntityTypes = Types.HOLDER_SET.read(buffer);
         float visibility = Types.FLOAT.readPrimitive(buffer);
         return new MobVisibility(targetingEntityTypes, visibility);
      }

      public void write(ByteBuf buffer, MobVisibility value) {
         Types.HOLDER_SET.write(buffer, value.targetingEntityTypes);
         Types.FLOAT.writePrimitive(buffer, value.visibility);
      }

      public void write(Ops ops, MobVisibility value) {
         ops.writeMap(
            map -> map.write("targeting_entity_types", new HolderSetType(MappingData.MappingType.ENTITY_TYPE), value.targetingEntityTypes)
               .write("visibility", Types.FLOAT, value.visibility)
         );
      }
   };
}
