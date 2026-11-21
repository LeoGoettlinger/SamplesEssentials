package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.util.ArrayUtil;
import com.viaversion.viaversion.util.Copyable;
import io.netty.buffer.ByteBuf;

public record CustomModelData1_21_4(float[] floats, boolean[] booleans, String[] strings, int[] colors) implements Copyable {
   public static final Type<CustomModelData1_21_4> TYPE = new Type<CustomModelData1_21_4>(CustomModelData1_21_4.class) {
      public CustomModelData1_21_4 read(ByteBuf buffer) {
         float[] floats = Types.FLOAT_ARRAY_PRIMITIVE.read(buffer);
         boolean[] booleans = Types.BOOLEAN_ARRAY_PRIMITIVE.read(buffer);
         String[] strings = Types.STRING_ARRAY.read(buffer);
         int[] colors = Types.INT_ARRAY_PRIMITIVE.read(buffer);
         return new CustomModelData1_21_4(floats, booleans, strings, colors);
      }

      public void write(ByteBuf buffer, CustomModelData1_21_4 value) {
         Types.FLOAT_ARRAY_PRIMITIVE.write(buffer, value.floats());
         Types.BOOLEAN_ARRAY_PRIMITIVE.write(buffer, value.booleans());
         Types.STRING_ARRAY.write(buffer, value.strings());
         Types.INT_ARRAY_PRIMITIVE.write(buffer, value.colors());
      }

      public void write(Ops ops, CustomModelData1_21_4 value) {
         ops.writeMap(
            map -> map.writeOptional("floats", new ArrayType<>(Types.FLOAT), ArrayUtil.boxedArray(value.floats), new Float[0])
               .writeOptional("flags", new ArrayType<>(Types.BOOLEAN), ArrayUtil.boxedArray(value.booleans), new Boolean[0])
               .writeOptional("strings", Types.STRING_ARRAY, value.strings(), new String[0])
               .writeOptional("colors", new ArrayType<>(Types.INT), ArrayUtil.boxedArray(value.colors), new Integer[0])
         );
      }
   };

   public CustomModelData1_21_4 copy() {
      return new CustomModelData1_21_4(Copyable.copy(this.floats), Copyable.copy(this.booleans), Copyable.copy(this.strings), Copyable.copy(this.colors));
   }
}
