package ac.grim.grimac.shaded.incendo.cloud.type;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@API(
   status = Status.STABLE
)
@Immutable
public interface Either<U, V> {
   @NonNull
   static <U, V> Either<U, V> ofPrimary(@NonNull final U value) {
      return EitherImpl.of(Objects.requireNonNull(value, "value"), null);
   }

   @NonNull
   static <U, V> Either<U, V> ofFallback(@NonNull final V value) {
      return EitherImpl.of(null, Objects.requireNonNull(value, "value"));
   }

   @NonNull
   Optional<U> primary();

   @NonNull
   Optional<V> fallback();

   @NonNull
   default U primaryOrMapFallback(@NonNull final Function<V, U> mapFallback) {
      return this.primary().orElseGet(() -> mapFallback.apply(this.fallback().get()));
   }

   @NonNull
   default V fallbackOrMapPrimary(@NonNull final Function<U, V> mapPrimary) {
      return this.fallback().orElseGet(() -> mapPrimary.apply(this.primary().get()));
   }

   @NonNull
   default <R> R mapEither(@NonNull final Function<U, R> mapPrimary, @NonNull final Function<V, R> mapFallback) {
      return this.primary().map(mapPrimary).orElseGet(() -> this.fallback().map(mapFallback).get());
   }
}
