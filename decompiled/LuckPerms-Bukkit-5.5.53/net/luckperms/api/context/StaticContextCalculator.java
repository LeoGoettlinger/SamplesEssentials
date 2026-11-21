package net.luckperms.api.context;

import java.util.Objects;
import java.util.function.Supplier;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
public interface StaticContextCalculator extends ContextCalculator<Object> {
   static StaticContextCalculator forSingleContext(String key, Supplier<String> valueFunction) {
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(valueFunction, "valueFunction");
      return consumer -> {
         String value = valueFunction.get();
         if (value != null) {
            consumer.accept(key, value);
         }
      };
   }

   void calculate(@NonNull ContextConsumer var1);

   @Override
   default void calculate(@NonNull Object target, @NonNull ContextConsumer consumer) {
      this.calculate(consumer);
   }
}
