package org.enginehub.piston.converter;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

public final class SuccessfulConversion<T> extends ConversionResult<T> {
   private final Collection<T> result;
   private final boolean exactMatch;

   public static <T> SuccessfulConversion<T> fromSingle(T result) {
      return from(ImmutableList.of(result));
   }

   public static <T> SuccessfulConversion<T> fromSingle(T result, boolean exactMatch) {
      return from(ImmutableList.of(result), exactMatch);
   }

   public static <T> SuccessfulConversion<T> from(Collection<T> result) {
      return new SuccessfulConversion<>(result, true);
   }

   public static <T> SuccessfulConversion<T> from(Collection<T> result, boolean exactMatch) {
      return new SuccessfulConversion<>(result, exactMatch);
   }

   private SuccessfulConversion(Collection<T> result, boolean exactMatch) {
      this.result = result;
      this.exactMatch = exactMatch;
   }

   public boolean isExactMatch() {
      return this.exactMatch;
   }

   @Override
   public boolean isSuccessful() {
      return true;
   }

   @Override
   public <U> ConversionResult<U> failureAsAny() {
      throw new IllegalStateException("This is not a failed conversion result.");
   }

   @Override
   public ConversionResult<T> orElse(ConversionResult<T> result) {
      return this;
   }

   @Override
   public <U> ConversionResult<U> map(Function<? super Collection<T>, ? extends Collection<U>> mapper) {
      Collection<U> mapped;
      try {
         mapped = (Collection<U>)mapper.apply(this.get());
      } catch (Throwable var4) {
         return FailedConversion.from(var4);
      }

      return (ConversionResult<U>)(mapped == null ? FailedConversion.from(new NullPointerException()) : from(mapped, this.exactMatch));
   }

   @Override
   public Collection<T> get() {
      return this.result;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         SuccessfulConversion<?> that = (SuccessfulConversion<?>)o;
         return this.result.equals(that.result);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.result);
   }
}
