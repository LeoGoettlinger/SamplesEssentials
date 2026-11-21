package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import io.netty.buffer.ByteBuf;

public record BannerPatternLayer(Holder<BannerPattern> pattern, int dyeColor) {
   public static final Type<BannerPatternLayer> TYPE = new Type<BannerPatternLayer>(BannerPatternLayer.class) {
      public BannerPatternLayer read(ByteBuf buffer) {
         Holder<BannerPattern> pattern = BannerPattern.TYPE.read(buffer);
         int color = Types.VAR_INT.readPrimitive(buffer);
         return new BannerPatternLayer(pattern, color);
      }

      public void write(ByteBuf buffer, BannerPatternLayer value) {
         BannerPattern.TYPE.write(buffer, value.pattern);
         Types.VAR_INT.writePrimitive(buffer, value.dyeColor);
      }

      public void write(Ops ops, BannerPatternLayer value) {
         ops.writeMap(map -> map.write("pattern", BannerPattern.TYPE, value.pattern).write("color", EnumTypes.DYE_COLOR, value.dyeColor));
      }
   };
   public static final Type<BannerPatternLayer[]> ARRAY_TYPE = new ArrayType<>(TYPE);
}
