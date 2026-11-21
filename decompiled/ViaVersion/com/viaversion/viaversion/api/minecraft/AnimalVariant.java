package com.viaversion.viaversion.api.minecraft;

import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.HolderType;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record AnimalVariant(int modelType, String texture, @Nullable HolderSet biomes) {
   public static HolderType<AnimalVariant> TYPE = new HolderType<AnimalVariant>() {
      public AnimalVariant readDirect(ByteBuf buffer) {
         int modelType = Types.VAR_INT.readPrimitive(buffer);
         String texture = Types.STRING.read(buffer);
         HolderSet biomes = Types.OPTIONAL_HOLDER_SET.read(buffer);
         return new AnimalVariant(modelType, texture, biomes);
      }

      public void writeDirect(ByteBuf buffer, AnimalVariant variant) {
         Types.VAR_INT.writePrimitive(buffer, variant.modelType());
         Types.STRING.write(buffer, variant.texture());
         Types.OPTIONAL_HOLDER_SET.write(buffer, variant.biomes());
      }
   };
}
