package net.kyori.adventure.builder;

import java.util.function.Consumer;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface AbstractBuilder<R> {
   @Contract(
      mutates = "param1"
   )
   static <R, B extends AbstractBuilder<R>> R configureAndBuild(final B builder, @Nullable final Consumer<? super B> consumer) {
      if (consumer != null) {
         consumer.accept(builder);
      }

      return builder.build();
   }

   @Contract(
      value = "-> new",
      pure = true
   )
   R build();
}
