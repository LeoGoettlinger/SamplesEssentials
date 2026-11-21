package com.viaversion.viaversion.api.type.types.math;

import com.viaversion.viaversion.api.minecraft.Vector3d;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

public class Vector3dType extends Type<Vector3d> {
   public Vector3dType() {
      super(Vector3d.class);
   }

   public Vector3d read(ByteBuf buffer) {
      double x = buffer.readDouble();
      double y = buffer.readDouble();
      double z = buffer.readDouble();
      return new Vector3d(x, y, z);
   }

   public void write(ByteBuf buffer, Vector3d object) {
      buffer.writeDouble(object.x());
      buffer.writeDouble(object.y());
      buffer.writeDouble(object.z());
   }
}
