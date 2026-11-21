package com.github.retrooper.packetevents.protocol.attribute;

import com.github.retrooper.packetevents.protocol.util.CodecNameable;
import com.github.retrooper.packetevents.protocol.util.NbtCodec;
import com.github.retrooper.packetevents.protocol.util.NbtCodecs;
import org.jspecify.annotations.NullMarked;

@NullMarked
public enum AttributeOperation implements CodecNameable {
   ADDITION("add_value"),
   MULTIPLY_BASE("add_multiplied_base"),
   MULTIPLY_TOTAL("add_multiplied_total");

   public static final NbtCodec<AttributeOperation> CODEC = NbtCodecs.forEnum(values());
   private final String name;

   private AttributeOperation(String name) {
      this.name = name;
   }

   @Override
   public String getCodecName() {
      return this.name;
   }
}
