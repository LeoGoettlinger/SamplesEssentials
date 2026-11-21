package ch.njol.util;

import java.util.Objects;
import org.jetbrains.annotations.Nullable;

public interface NullableChecker<T> extends Checker<T>, java.util.function.Predicate<T> {
   NullableChecker<Object> nullChecker = Objects::nonNull;

   @Override
   boolean check(@Nullable T var1);

   @Override
   default boolean test(@Nullable T t) {
      return this.check(t);
   }
}
