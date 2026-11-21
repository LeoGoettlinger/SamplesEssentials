package com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_21_4;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.libs.mcstructs.converter.codec.Codec;
import com.viaversion.viaversion.libs.mcstructs.converter.impl.v1_20_3.NbtConverter_v1_20_3;
import com.viaversion.viaversion.libs.mcstructs.converter.model.Result;
import java.util.Arrays;
import java.util.UUID;

public class ExtraCodecs_v1_21_4 {
   public static final Codec<Integer> ARGB_COLOR = Codec.oneOf(Codec.INTEGER, Codec.FLOAT.listOf(4, 4).map(i -> {
      float a = (i >> 24 & 0xFF) / 255.0F;
      float r = (i >> 16 & 0xFF) / 255.0F;
      float g = (i >> 8 & 0xFF) / 255.0F;
      float b = (i & 0xFF) / 255.0F;
      return Arrays.asList(r, g, b, a);
   }, floats -> {
      int r = (int)(floats.get(0) * 255.0F);
      int g = (int)(floats.get(1) * 255.0F);
      int b = (int)(floats.get(2) * 255.0F);
      int a = (int)(floats.get(3) * 255.0F);
      return a << 24 | r << 16 | g << 8 | b;
   }));
   public static final Codec<UUID> LENIENT_UUID = Codec.oneOf(Codec.INT_ARRAY_UUID, Codec.STRICT_STRING_UUID);
   public static final Codec<CompoundTag> INLINED_COMPOUND_TAG = NbtConverter_v1_20_3.INSTANCE
      .toCodec()
      .verified(tag -> !(tag instanceof CompoundTag) ? Result.error("Expected a compound tag") : null)
      .map(tag -> tag, tag -> (CompoundTag)tag);
}
