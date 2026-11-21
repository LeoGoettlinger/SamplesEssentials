package ch.njol.skript.lang.util.common;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
@Deprecated(
   since = "2.13",
   forRemoval = true
)
public interface AnyAmount extends AnyProvider {
   @NotNull
   Number amount();

   default boolean supportsAmountChange() {
      return false;
   }

   default void setAmount(Number amount) throws UnsupportedOperationException {
      throw new UnsupportedOperationException();
   }

   default boolean isEmpty() {
      return this.amount().intValue() == 0;
   }
}
