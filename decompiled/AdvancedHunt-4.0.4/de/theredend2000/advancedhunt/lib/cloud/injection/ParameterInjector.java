package de.theredend2000.advancedhunt.lib.cloud.injection;

import de.theredend2000.advancedhunt.lib.cloud.context.CommandContext;
import de.theredend2000.advancedhunt.lib.cloud.util.annotation.AnnotationAccessor;
import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@FunctionalInterface
@API(
   status = API.Status.STABLE
)
public interface ParameterInjector<C, T> {
   @API(
      status = API.Status.STABLE
   )
   @NonNull
   static <C, T> ParameterInjector<C, T> constantInjector(@NonNull final T value) {
      return new ParameterInjector.ConstantInjector<>(value);
   }

   @Nullable
   T create(@NonNull CommandContext<C> context, @NonNull AnnotationAccessor annotationAccessor);

   public static final class ConstantInjector<C, T> implements ParameterInjector<C, T> {
      private final T value;

      private ConstantInjector(@NonNull final T value) {
         this.value = value;
      }

      @NonNull
      @Override
      public T create(@NonNull final CommandContext<C> context, @NonNull final AnnotationAccessor annotationAccessor) {
         return this.value;
      }

      @Override
      public boolean equals(final Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            ParameterInjector.ConstantInjector<?, ?> that = (ParameterInjector.ConstantInjector<?, ?>)o;
            return Objects.equals(this.value, that.value);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.value);
      }

      @Override
      public String toString() {
         return "ConstantInjector{value=" + this.value + '}';
      }
   }
}
