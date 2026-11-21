package com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_21_6;

import com.viaversion.viaversion.libs.mcstructs.converter.codec.Codec;
import com.viaversion.viaversion.libs.mcstructs.converter.model.Result;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.UUID;

public class ExtraCodecs_v1_21_6 {
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
   public static final Codec<URI> UNTRUSTED_URI = Codec.STRING.flatMap(uri -> Result.success(uri.toString()), s -> {
      try {
         URI uri = new URI(s);
         if (uri.getScheme() == null) {
            throw new URISyntaxException(s, "Missing scheme");
         } else if (!uri.getScheme().equalsIgnoreCase("http") && !uri.getScheme().equalsIgnoreCase("https")) {
            throw new URISyntaxException(s, "Unsupported scheme: " + uri.getScheme());
         } else {
            return Result.success(uri);
         }
      } catch (Throwable var2) {
         return Result.error(var2);
      }
   });
   public static final Codec<String> CHAT_STRING = Codec.STRING.verified(s -> {
      for (char c : s.toCharArray()) {
         if (c == 167 || c < ' ' || c == 127) {
            return Result.error("Illegal character: " + c);
         }
      }

      return null;
   });
   public static final Codec<UUID> LENIENT_UUID = Codec.oneOf(Codec.INT_ARRAY_UUID, Codec.STRICT_STRING_UUID);
}
