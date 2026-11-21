package com.viaversion.viaversion.api.minecraft;

import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.HolderType;
import io.netty.buffer.ByteBuf;

public record WolfVariant(String wildTexture, String tameTexture, String angryTexture, HolderSet biomes) {
   public static HolderType<WolfVariant> TYPE = new HolderType<WolfVariant>() {
      public WolfVariant readDirect(ByteBuf buffer) {
         String wildTexture = Types.STRING.read(buffer);
         String tameTexture = Types.STRING.read(buffer);
         String angryTexture = Types.STRING.read(buffer);
         HolderSet biomes = Types.HOLDER_SET.read(buffer);
         return new WolfVariant(wildTexture, tameTexture, angryTexture, biomes);
      }

      public void writeDirect(ByteBuf buffer, WolfVariant variant) {
         Types.STRING.write(buffer, variant.wildTexture());
         Types.STRING.write(buffer, variant.tameTexture());
         Types.STRING.write(buffer, variant.angryTexture());
         Types.HOLDER_SET.write(buffer, variant.biomes());
      }
   };
}
