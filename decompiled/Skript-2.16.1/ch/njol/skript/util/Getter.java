package ch.njol.skript.util;

import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;

@Deprecated(
   since = "2.10.0",
   forRemoval = true
)
public abstract class Getter<R, A> implements Converter<A, R> {
   @Nullable
   public abstract R get(A var1);

   @Nullable
   @Override
   public final R convert(A a) {
      return this.get(a);
   }
}
