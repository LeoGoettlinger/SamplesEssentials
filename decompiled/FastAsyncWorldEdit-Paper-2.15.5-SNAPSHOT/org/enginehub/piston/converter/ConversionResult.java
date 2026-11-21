package org.enginehub.piston.converter;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.function.Function;

public abstract class ConversionResult<T> {
   ConversionResult() {
   }

   public abstract boolean isSuccessful();

   public abstract <U> ConversionResult<U> failureAsAny();

   public abstract ConversionResult<T> orElse(ConversionResult<T> var1);

   public abstract <U> ConversionResult<U> map(Function<? super Collection<T>, ? extends Collection<U>> var1);

   public final <U> ConversionResult<U> mapSingle(Function<? super T, ? extends U> mapper) {
      return this.map(many -> {
         Preconditions.checkArgument(many.size() == 1, "Need exactly one result");
         U result = (U)mapper.apply(many.iterator().next());
         return result == null ? null : ImmutableList.of(result);
      });
   }

   public final Collection<T> orElse(Collection<T> other) {
      return this.isSuccessful() ? this.get() : other;
   }

   public abstract Collection<T> get();
}
