package su.nightexpress.nightcore.util;

import java.util.Objects;
import java.util.function.Function;

@Deprecated
public interface TriFunction<T, U, V, R> {
   R apply(T var1, U var2, V var3);

   default <K> TriFunction<T, U, V, K> andThen(Function<? super R, ? extends K> after) {
      Objects.requireNonNull(after);
      return (t, u, v) -> (K)after.apply(this.apply(t, u, v));
   }
}
