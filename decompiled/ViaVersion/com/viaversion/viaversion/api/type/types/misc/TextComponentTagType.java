package com.viaversion.viaversion.api.type.types.misc;

import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Types;
import java.util.Set;
import java.util.Map.Entry;

public class TextComponentTagType extends TagType {
   private static final Set<String> BOOLEAN_KEYS = Set.of("bold", "italic", "underlined", "strikethrough", "obfuscated", "interpret");

   @Override
   public void write(Ops ops, Tag value) {
      if (value instanceof CompoundTag compoundTag) {
         ops.writeMap(map -> {
            for (Entry<String, Tag> entry : compoundTag.entrySet()) {
               this.write(map, entry.getKey(), entry.getValue());
            }
         });
      } else {
         super.write(ops, value);
      }
   }

   private void write(Ops.MapSerializer map, String key, Tag value) {
      if (value instanceof ByteTag byteTag && BOOLEAN_KEYS.contains(key)) {
         map.write(key, Types.BOOLEAN, byteTag.asBoolean());
      } else {
         map.write(key, this, value);
      }
   }
}
