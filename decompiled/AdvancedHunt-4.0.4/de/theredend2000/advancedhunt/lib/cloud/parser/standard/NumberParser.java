package de.theredend2000.advancedhunt.lib.cloud.parser.standard;

import de.theredend2000.advancedhunt.lib.cloud.parser.ArgumentParser;
import de.theredend2000.advancedhunt.lib.cloud.type.range.Range;
import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.STABLE
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
