package ch.njol.util;

import java.util.function.Function;
import org.jetbrains.annotations.Nullable;

@Deprecated(
   since = "2.10.0",
   forRemoval = true
)
@FunctionalInterface
public interface Callback<R, A> extends Function<A, R> {
   @Nullable
   R run(A var1);

   @Override
   default R apply(A a) {
      return this.run(a);
   }
}
