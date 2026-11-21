package com.viaversion.viaversion.api.minecraft;

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.HolderType;
import com.viaversion.viaversion.util.Key;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record PaintingVariant(int width, int height, String assetId, @Nullable Tag title, @Nullable Tag author) {
   public static HolderType<PaintingVariant> TYPE1_21 = new HolderType<PaintingVariant>() {
      public PaintingVariant readDirect(ByteBuf buffer) {
         int width = Types.VAR_INT.readPrimitive(buffer);
         int height = Types.VAR_INT.readPrimitive(buffer);
         String assetId = Types.STRING.read(buffer);
         return new PaintingVariant(width, height, assetId);
      }

      public void writeDirect(ByteBuf buffer, PaintingVariant variant) {
         Types.VAR_INT.writePrimitive(buffer, variant.width());
         Types.VAR_INT.writePrimitive(buffer, variant.height());
         Types.STRING.write(buffer, variant.assetId());
      }
   };
   public static HolderType<PaintingVariant> TYPE1_21_2 = new HolderType<PaintingVariant>() {
      public PaintingVariant readDirect(ByteBuf buffer) {
         int width = Types.VAR_INT.readPrimitive(buffer);
         int height = Types.VAR_INT.readPrimitive(buffer);
         String assetId = Types.STRING.read(buffer);
         Tag title = Types.TRUSTED_OPTIONAL_TAG.read(buffer);
         Tag author = Types.TRUSTED_OPTIONAL_TAG.read(buffer);
         return new PaintingVariant(width, height, assetId, title, author);
      }

      public void writeDirect(ByteBuf buffer, PaintingVariant variant) {
         Types.VAR_INT.writePrimitive(buffer, variant.width());
         Types.VAR_INT.writePrimitive(buffer, variant.height());
         Types.STRING.write(buffer, variant.assetId());
         Types.TRUSTED_OPTIONAL_TAG.write(buffer, variant.title());
         Types.TRUSTED_OPTIONAL_TAG.write(buffer, variant.author());
      }

      public void writeDirect(Ops ops, PaintingVariant value) {
         ops.writeMap(
            map -> map.write("width", Types.INT, value.width())
               .write("height", Types.INT, value.height())
               .write("asset_id", Types.IDENTIFIER, Key.of(value.assetId()))
               .writeOptional("title", Types.TEXT_COMPONENT_TAG, value.title())
               .writeOptional("author", Types.TEXT_COMPONENT_TAG, value.author())
         );
      }

      @Override
      protected Key identifier(Ops ops, int id) {
         return ops.context().registryAccess().registryKey("painting_variant", id);
      }
   };

   public PaintingVariant(int width, int height, String assetId) {
      this(width, height, assetId, null, null);
   }
}
