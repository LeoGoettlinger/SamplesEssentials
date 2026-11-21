package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.util.Either;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record StatePropertyMatcher(String name, Either<String, StatePropertyMatcher.RangedMatcher> matcher) {
   public static final Type<StatePropertyMatcher> TYPE = new Type<StatePropertyMatcher>(StatePropertyMatcher.class) {
      public StatePropertyMatcher read(ByteBuf buffer) {
         String name = Types.STRING.read(buffer);
         if (buffer.readBoolean()) {
            String value = Types.STRING.read(buffer);
            return new StatePropertyMatcher(name, Either.left(value));
         } else {
            String minValue = Types.OPTIONAL_STRING.read(buffer);
            String maxValue = Types.OPTIONAL_STRING.read(buffer);
            return new StatePropertyMatcher(name, Either.right(new StatePropertyMatcher.RangedMatcher(minValue, maxValue)));
         }
      }

      public void write(ByteBuf buffer, StatePropertyMatcher value) {
         Types.STRING.write(buffer, value.name);
         if (value.matcher.isLeft()) {
            buffer.writeBoolean(true);
            Types.STRING.write(buffer, value.matcher.left());
         } else {
            buffer.writeBoolean(false);
            Types.OPTIONAL_STRING.write(buffer, value.matcher.right().minValue());
            Types.OPTIONAL_STRING.write(buffer, value.matcher.right().maxValue());
         }
      }
   };
   public static final Type<StatePropertyMatcher[]> ARRAY_TYPE = new ArrayType<>(TYPE);

   public record RangedMatcher(@Nullable String minValue, @Nullable String maxValue) {
   }
}
