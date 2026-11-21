package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.HolderType;
import com.viaversion.viaversion.util.Key;
import io.netty.buffer.ByteBuf;

public record BannerPattern(String assetId, String translationKey) {
   public static final HolderType<BannerPattern> TYPE = new HolderType<BannerPattern>() {
      public BannerPattern readDirect(ByteBuf buffer) {
         String assetId = Types.STRING.read(buffer);
         String translationKey = Types.STRING.read(buffer);
         return new BannerPattern(assetId, translationKey);
      }

      public void writeDirect(ByteBuf buffer, BannerPattern value) {
         Types.STRING.write(buffer, value.assetId);
         Types.STRING.write(buffer, value.translationKey);
      }

      public void writeDirect(Ops ops, BannerPattern object) {
         ops.writeMap(map -> map.write("asset_id", Types.IDENTIFIER, Key.of(object.assetId)).write("translation_key", Types.STRING, object.translationKey));
      }

      @Override
      protected Key identifier(Ops ops, int id) {
         return ops.context().registryAccess().registryKey("banner_pattern", id);
      }
   };
}
