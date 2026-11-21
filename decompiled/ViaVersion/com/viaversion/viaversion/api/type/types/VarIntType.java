package com.viaversion.viaversion.api.type.types;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.TypeConverter;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public class VarIntType extends Type<Integer> implements TypeConverter<Integer> {
   private static final int CONTINUE_BIT = 128;
   private static final int VALUE_BITS = 127;
   private static final int MULTI_BYTE_BITS = -128;
   private static final int MAX_BYTES = 5;
   private static final int[] VAR_INT_LENGTHS = new int[65];

   public VarIntType() {
      super("VarInt", Integer.class);
   }

   public int readPrimitive(ByteBuf buffer) {
      int value = 0;
      int bytes = 0;

      byte in;
      do {
         in = buffer.readByte();
         value |= (in & 127) << bytes++ * 7;
         if (bytes > 5) {
            throw new RuntimeException("VarInt too big");
         }
      } while ((in & 128) == 128);

      return value;
   }

   public void writePrimitive(ByteBuf buffer, int value) {
      while ((value & -128) != 0) {
         buffer.writeByte(value & 127 | 128);
         value >>>= 7;
      }

      buffer.writeByte(value);
   }

   @Deprecated
   public Integer read(ByteBuf buffer) {
      return this.readPrimitive(buffer);
   }

   @Deprecated
   public void write(ByteBuf buffer, Integer object) {
      this.writePrimitive(buffer, object);
   }

   public void write(Ops ops, Integer integer) {
      Types.INT.write(ops, integer);
   }

   public Integer from(Object o) {
      if (o instanceof Number number) {
         return number.intValue();
      } else if (o instanceof Boolean boo) {
         return boo ? 1 : 0;
      } else {
         throw new UnsupportedOperationException();
      }
   }

   public static int varIntLength(int value) {
      return VAR_INT_LENGTHS[Integer.numberOfLeadingZeros(value)];
   }

   static {
      for (int i = 0; i <= 32; i++) {
         VAR_INT_LENGTHS[i] = (int)Math.ceil((31.0 - (i - 1)) / 7.0);
      }

      VAR_INT_LENGTHS[32] = 1;
   }

   public static final class OptionalVarIntType extends OptionalType<Integer> {
      public OptionalVarIntType() {
         super(Types.VAR_INT);
      }
   }
}
