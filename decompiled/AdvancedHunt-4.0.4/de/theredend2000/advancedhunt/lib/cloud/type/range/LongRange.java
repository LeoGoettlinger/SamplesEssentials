package de.theredend2000.advancedhunt.lib.cloud.type.range;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@Immutable
public interface LongRange extends Range<Long> {
   long minLong();

   long maxLong();

   @NonNull
   default Long min() {
      return this.minLong();
   }

   @NonNull
   default Long max() {
      return this.maxLong();
   }
}
