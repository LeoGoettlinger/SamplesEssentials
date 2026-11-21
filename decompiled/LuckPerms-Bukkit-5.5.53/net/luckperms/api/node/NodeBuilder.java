package net.luckperms.api.node;

import java.time.Duration;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAmount;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.node.metadata.NodeMetadataKey;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface NodeBuilder<N extends ScopedNode<N, B>, B extends NodeBuilder<N, B>> {
   @NonNull
   B value(boolean var1);

   @NonNull
   B negated(boolean var1);

   @NonNull
   B expiry(long var1);

   @NonNull
   B expiry(@Nullable TemporalAccessor var1);

   @NonNull
   B expiry(@Nullable TemporalAmount var1);

   @NonNull
   default B expiry(long duration, @NonNull TimeUnit unit) {
      if (duration <= 0L) {
         throw new IllegalArgumentException("duration must be positive");
      } else {
         long seconds = Objects.requireNonNull(unit, "unit").toSeconds(duration);
         return this.expiry(Duration.ofSeconds(seconds));
      }
   }

   @NonNull
   B clearExpiry();

   @NonNull
   B context(@NonNull ContextSet var1);

   @NonNull
   B withContext(@NonNull String var1, @NonNull String var2);

   @NonNull
   B withContext(@NonNull ContextSet var1);

   @NonNull
   <T> B withMetadata(@NonNull NodeMetadataKey<T> var1, @Nullable T var2);

   @NonNull
   N build();
}
