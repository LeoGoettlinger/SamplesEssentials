package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;

public record TropicalFishPattern(int packedId) {
   public static final Type<TropicalFishPattern> TYPE = new Type<TropicalFishPattern>(TropicalFishPattern.class) {
      public TropicalFishPattern read(ByteBuf buffer) {
         int packedId = Types.VAR_INT.readPrimitive(buffer);
         return new TropicalFishPattern(packedId);
      }

      public void write(ByteBuf buffer, TropicalFishPattern value) {
         Types.VAR_INT.writePrimitive(buffer, value.packedId);
      }

      public void write(Ops ops, TropicalFishPattern value) {
         TropicalFishPattern.Pattern pattern = Arrays.stream(TropicalFishPattern.Pattern.values())
            .filter(e -> e.packedId == value.packedId)
            .findAny()
            .orElse(TropicalFishPattern.Pattern.KOB);
         ops.write(Types.STRING, pattern.key);
      }
   };

   public int sizeId() {
      return this.packedId & 0xFF;
   }

   public int sizeSpecificId() {
      return this.packedId >> 8;
   }

   private static enum Pattern {
      KOB("kob", 0, 0),
      SUNSTREAK("sunstreak", 0, 1),
      SNOOPER("snooper", 0, 2),
      DASHER("dasher", 0, 3),
      BRINELY("brinely", 0, 4),
      SPOTTY("spotty", 0, 5),
      FLOPPER("flopper", 1, 0),
      STRIPEY("stripey", 1, 1),
      GLITTER("glitter", 1, 2),
      BLOCKFISH("blockfish", 1, 3),
      BETTY("betty", 1, 4),
      CLAYFISH("clayfish", 1, 5);

      private final String key;
      private final int packedId;

      private Pattern(String key, int sizeId, int sizeSpecificId) {
         this.key = key;
         this.packedId = sizeId | sizeSpecificId << 8;
      }
   }
}
