package com.viaversion.viaversion.api.minecraft.item.data.trim;

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.HolderType;
import com.viaversion.viaversion.util.Key;
import io.netty.buffer.ByteBuf;

public record ArmorTrimMaterial26_3(String paletteId, Tag description) {
   public static final HolderType<ArmorTrimMaterial26_3> TYPE = new HolderType<ArmorTrimMaterial26_3>() {
      public ArmorTrimMaterial26_3 readDirect(ByteBuf buffer) {
         String paletteId = Types.STRING.read(buffer);
         Tag description = Types.TAG.read(buffer);
         return new ArmorTrimMaterial26_3(paletteId, description);
      }

      public void writeDirect(ByteBuf buffer, ArmorTrimMaterial26_3 value) {
         Types.STRING.write(buffer, value.paletteId());
         Types.TAG.write(buffer, value.description());
      }

      public void writeDirect(Ops ops, ArmorTrimMaterial26_3 object) {
         ops.writeMap(map -> {
            map.write("palette_id", Types.IDENTIFIER, Key.of(object.paletteId()));
            map.write("description", Types.TEXT_COMPONENT_TAG, object.description());
         });
      }

      @Override
      protected Key identifier(Ops ops, int id) {
         return ops.context().registryAccess().registryKey("trim_material", id);
      }
   };
}
