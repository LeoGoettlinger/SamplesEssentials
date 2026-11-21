package com.viaversion.viaversion.api.type.types.chunk;

import com.viaversion.viaversion.api.minecraft.chunks.Heightmap;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public final class HeightmapType extends Type<Heightmap> {
   public HeightmapType() {
      super(Heightmap.class);
   }

   public Heightmap read(ByteBuf buffer) {
      int type = Types.VAR_INT.readPrimitive(buffer);
      long[] data = Types.LONG_ARRAY_PRIMITIVE.read(buffer);
      return new Heightmap(type, data);
   }

   public void write(ByteBuf buffer, Heightmap value) {
      Types.VAR_INT.writePrimitive(buffer, value.type());
      Types.LONG_ARRAY_PRIMITIVE.write(buffer, value.data());
   }
}
