package net.luckperms.api.context;

import java.util.Objects;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
public interface ContextCalculator<T> {
   @NonNull
   static <T> ContextCalculator<T> forSingleContext(@NonNull String key, @NonNull Function<T, String> valueFunction) {
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(valueFunction, "valueFunction");
      return (target, consumer) -> {
         String value = valueFunction.apply(target);
         if (value != null) {
            consumer.accept(key, value);
         }
      };
   }

   void calculate(@NonNull T var1, @NonNull ContextConsumer var2);

   @NonNull
   default ContextSet estimatePotentialContexts() {
      return ImmutableContextSet.empty();
   }
}
