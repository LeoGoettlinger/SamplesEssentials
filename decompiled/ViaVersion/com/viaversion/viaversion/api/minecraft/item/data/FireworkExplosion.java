package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.util.ArrayUtil;
import com.viaversion.viaversion.util.Copyable;
import io.netty.buffer.ByteBuf;

public record FireworkExplosion(int shape, int[] colors, int[] fadeColors, boolean hasTrail, boolean hasTwinkle) implements Copyable {
   public static final String[] SHAPES = new String[]{"small_ball", "large_ball", "star", "creeper", "burst"};
   public static final Type<FireworkExplosion> TYPE = new Type<FireworkExplosion>(FireworkExplosion.class) {
      public FireworkExplosion read(ByteBuf buffer) {
         int shape = Types.VAR_INT.readPrimitive(buffer);
         int[] colors = Types.INT_ARRAY_PRIMITIVE.read(buffer);
         int[] fadeColors = Types.INT_ARRAY_PRIMITIVE.read(buffer);
         boolean hasTrail = buffer.readBoolean();
         boolean hasTwinkle = buffer.readBoolean();
         return new FireworkExplosion(shape, colors, fadeColors, hasTrail, hasTwinkle);
      }

      public void write(ByteBuf buffer, FireworkExplosion value) {
         Types.VAR_INT.writePrimitive(buffer, value.shape);
         Types.INT_ARRAY_PRIMITIVE.write(buffer, value.colors);
         Types.INT_ARRAY_PRIMITIVE.write(buffer, value.fadeColors);
         buffer.writeBoolean(value.hasTrail);
         buffer.writeBoolean(value.hasTwinkle);
      }

      public void write(Ops ops, FireworkExplosion value) {
         ops.writeMap(
            map -> map.write("shape", Types.STRING, FireworkExplosion.SHAPES[value.shape])
               .writeOptional("colors", new ArrayType<>(Types.INT), ArrayUtil.boxedArray(value.colors), new Integer[0])
               .writeOptional("fade_colors", new ArrayType<>(Types.INT), ArrayUtil.boxedArray(value.fadeColors), new Integer[0])
               .writeOptional("has_trail", Types.BOOLEAN, value.hasTrail, false)
               .writeOptional("has_twinkle", Types.BOOLEAN, value.hasTwinkle, false)
         );
      }
   };
   public static final Type<FireworkExplosion[]> ARRAY_TYPE = new ArrayType<>(TYPE);

   public FireworkExplosion copy() {
      return new FireworkExplosion(this.shape, Copyable.copy(this.colors), Copyable.copy(this.fadeColors), this.hasTrail, this.hasTwinkle);
   }
}
