package com.viaversion.viaversion.api.type.types.math;

import com.viaversion.viaversion.api.minecraft.Vector3d;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.MathUtil;
import io.netty.buffer.ByteBuf;

public class LowPrecisionVectorType extends Type<Vector3d> {
   private static final double MAX_QUANTIZED_VALUE = 32766.0;
   private static final int SCALE_BITS = 2;
   private static final int SCALE_BITS_MASK = 3;
   private static final int CONTINUATION_FLAG = 4;
   private static final double ABS_MAX_VALUE = 1.7179869183E10;
   private static final double ABS_MIN_VALUE = 3.051944088384301E-5;

   public LowPrecisionVectorType() {
      super(Vector3d.class);
   }

   public Vector3d read(ByteBuf buffer) {
      int first = Types.UNSIGNED_BYTE.read(buffer);
      if (first == 0) {
         return Vector3d.ZERO;
      } else {
         int second = Types.UNSIGNED_BYTE.read(buffer);
         long remaining = Types.UNSIGNED_INT.read(buffer);
         long packed = remaining << 16 | second << 8 | first;
         long scale = first & 3;
         if ((first & 4) != 0) {
            scale |= (Types.VAR_INT.readPrimitive(buffer) & 4294967295L) << 2;
         }

         return new Vector3d(this.unpack(packed >> 3) * scale, this.unpack(packed >> 18) * scale, this.unpack(packed >> 33) * scale);
      }
   }

   public void write(ByteBuf buffer, Vector3d vec) {
      double x = this.sanitize(vec.x());
      double y = this.sanitize(vec.y());
      double z = this.sanitize(vec.z());
      double maxPart = Math.max(Math.abs(x), Math.max(Math.abs(y), Math.abs(z)));
      if (maxPart < 3.051944088384301E-5) {
         buffer.writeByte(0);
      } else {
         long scale = MathUtil.ceilLong(maxPart);
         boolean scaleTooLargeForBits = (scale & 3L) != scale;
         long scaleBits = scaleTooLargeForBits ? scale & 3L | 4L : scale;
         long packed = scaleBits | this.pack(x / scale) << 3 | this.pack(y / scale) << 18 | this.pack(z / scale) << 33;
         buffer.writeByte((byte)packed);
         buffer.writeByte((byte)(packed >> 8));
         buffer.writeInt((int)(packed >> 16));
         if (scaleTooLargeForBits) {
            Types.VAR_INT.writePrimitive(buffer, (int)(scale >> 2));
         }
      }
   }

   private double sanitize(double value) {
      return Double.isNaN(value) ? 0.0 : MathUtil.clamp(value, -1.7179869183E10, 1.7179869183E10);
   }

   private long pack(double value) {
      double shifted = value * 0.5 + 0.5;
      return Math.round(shifted * 32766.0);
   }

   private double unpack(long value) {
      double clamped = Math.min((double)(value & 32767L), 32766.0);
      return clamped * 2.0 / 32766.0 - 1.0;
   }
}
