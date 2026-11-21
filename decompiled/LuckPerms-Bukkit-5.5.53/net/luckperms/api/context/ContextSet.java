package net.luckperms.api.context;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Unmodifiable;

public interface ContextSet extends Iterable<Context> {
   boolean isImmutable();

   @NonNull
   ImmutableContextSet immutableCopy();

   @NonNull
   MutableContextSet mutableCopy();

   @NonNull
   @Unmodifiable
   Set<Context> toSet();

   @NonNull
   @Unmodifiable
   Map<String, Set<String>> toMap();

   @Deprecated
   @NonNull
   @Unmodifiable
   Map<String, String> toFlattenedMap();

   @NonNull
   @Unmodifiable
   @Override
   Iterator<Context> iterator();

   boolean containsKey(@NonNull String var1);

   @NonNull
   @Unmodifiable
   Set<String> getValues(@NonNull String var1);

   @NonNull
   default Optional<String> getAnyValue(@NonNull String key) {
      return this.getValues(key).stream().findAny();
   }

   boolean contains(@NonNull String var1, @NonNull String var2);

   default boolean contains(@NonNull Context entry) {
      Objects.requireNonNull(entry, "entry");
      return this.contains(entry.getKey(), entry.getValue());
   }

   default boolean containsAny(@NonNull String key, @NonNull Iterable<String> values) {
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(values, "values");

      for (String value : values) {
         if (this.contains(key, value)) {
            return true;
         }
      }

      return false;
   }

   default boolean isSatisfiedBy(@NonNull ContextSet other) {
      return this.isSatisfiedBy(other, ContextSatisfyMode.AT_LEAST_ONE_VALUE_PER_KEY);
   }

   boolean isSatisfiedBy(@NonNull ContextSet var1, @NonNull ContextSatisfyMode var2);

   boolean isEmpty();

   int size();
}
