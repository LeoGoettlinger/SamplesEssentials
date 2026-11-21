package ch.njol.skript.lang.util.common;

import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.ApiStatus.Internal;

@FunctionalInterface
@Deprecated(
   since = "2.13",
   forRemoval = true
)
public interface AnyContains<Type> extends AnyProvider {
   boolean contains(@UnknownNullability Type var1);

   default boolean isSafeToCheck(Object value) {
      return true;
   }

   @Internal
   default boolean checkSafely(Object value) {
      return this.isSafeToCheck(value) && this.contains((Type)value);
   }
}
