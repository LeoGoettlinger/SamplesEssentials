package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public record MapDecoration(String type, double x, double z, float rotation) {
   public static final Type<MapDecoration> TYPE = new Type<MapDecoration>(MapDecoration.class) {
      public MapDecoration read(ByteBuf buffer) {
         String type = Types.STRING.read(buffer);
         double x = Types.DOUBLE.readPrimitive(buffer);
         double z = Types.DOUBLE.readPrimitive(buffer);
         float rotation = Types.FLOAT.readPrimitive(buffer);
         return new MapDecoration(type, x, z, rotation);
      }

      public void write(ByteBuf buffer, MapDecoration value) {
         Types.STRING.write(buffer, value.type);
         buffer.writeDouble(value.x);
         buffer.writeDouble(value.z);
         buffer.writeFloat(value.rotation);
      }
   };
}
