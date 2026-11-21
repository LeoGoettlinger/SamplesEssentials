package de.oliver.cloud.parser.standard;

import de.oliver.cloud.parser.ArgumentParser;
import de.oliver.cloud.type.range.Range;
import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public abstract class NumberParser<C, N extends Number, R extends Range<N>> implements ArgumentParser<C, N> {
   private final R range;

   protected NumberParser(@NonNull final R range) {
      this.range = Objects.requireNonNull(range, "range");
   }

   @NonNull
   public final R range() {
      return this.range;
   }

   public abstract boolean hasMax();

   public abstract boolean hasMin();
}
