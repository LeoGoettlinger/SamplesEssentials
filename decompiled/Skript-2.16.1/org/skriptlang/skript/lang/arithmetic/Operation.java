package org.skriptlang.skript.lang.arithmetic;

import java.util.function.BiFunction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface Operation<L, R, T> extends BiFunction<L, R, T> {
   @Contract(
      pure = true
   )
   T calculate(@NotNull L var1, @NotNull R var2);

   @Override
   default T apply(L l, R r) {
      return this.calculate(l, r);
   }
}
