package org.skriptlang.skript.lang.converter;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface Converter<F, T> {
   int ALL_CHAINING = 0;
   int NO_LEFT_CHAINING = 1;
   int NO_RIGHT_CHAINING = 2;
   int ALLOW_UNSAFE_CASTS = 4;
   int NO_CHAINING = 3;

   @Nullable
   T convert(F var1);
}
