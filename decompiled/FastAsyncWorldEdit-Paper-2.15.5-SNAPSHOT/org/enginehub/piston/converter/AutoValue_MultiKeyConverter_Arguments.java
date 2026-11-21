package org.enginehub.piston.converter;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;

final class AutoValue_MultiKeyConverter_Arguments<E> extends MultiKeyConverter.Arguments<E> {
   private final ImmutableSetMultimap<E, String> items;
   @Nullable
   private final E unknownValue;
   private final UnaryOperator<String> errorMessage;

   private AutoValue_MultiKeyConverter_Arguments(ImmutableSetMultimap<E, String> items, @Nullable E unknownValue, UnaryOperator<String> errorMessage) {
      this.items = items;
      this.unknownValue = unknownValue;
      this.errorMessage = errorMessage;
   }

   @Override
   ImmutableSetMultimap<E, String> items() {
      return this.items;
   }

   @Nullable
   @Override
   E unknownValue() {
      return this.unknownValue;
   }

   @Override
   UnaryOperator<String> errorMessage() {
      return this.errorMessage;
   }

   @Override
   public String toString() {
      return "Arguments{items=" + this.items + ", unknownValue=" + this.unknownValue + ", errorMessage=" + this.errorMessage + "}";
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else {
         return !(o instanceof MultiKeyConverter.Arguments<?> that)
            ? false
            : this.items.equals(that.items())
               && (this.unknownValue == null ? that.unknownValue() == null : this.unknownValue.equals(that.unknownValue()))
               && this.errorMessage.equals(that.errorMessage());
      }
   }

   @Override
   public int hashCode() {
      int h$ = 1;
      h$ *= 1000003;
      h$ ^= this.items.hashCode();
      h$ *= 1000003;
      h$ ^= this.unknownValue == null ? 0 : this.unknownValue.hashCode();
      h$ *= 1000003;
      return h$ ^ this.errorMessage.hashCode();
   }

   static final class Builder<E> implements MultiKeyConverter.Arguments.Builder<E> {
      private ImmutableSetMultimap<E, String> items;
      private E unknownValue;
      private UnaryOperator<String> errorMessage;

      @Override
      public MultiKeyConverter.Arguments.Builder<E> items(SetMultimap<E, String> items) {
         this.items = ImmutableSetMultimap.copyOf(items);
         return this;
      }

      @Override
      public MultiKeyConverter.Arguments.Builder<E> unknownValue(@Nullable E unknownValue) {
         this.unknownValue = unknownValue;
         return this;
      }

      @Override
      public MultiKeyConverter.Arguments.Builder<E> errorMessage(UnaryOperator<String> errorMessage) {
         if (errorMessage == null) {
            throw new NullPointerException("Null errorMessage");
         } else {
            this.errorMessage = errorMessage;
            return this;
         }
      }

      @Override
      public MultiKeyConverter.Arguments<E> autoBuild() {
         if (this.items != null && this.errorMessage != null) {
            return new AutoValue_MultiKeyConverter_Arguments<>(this.items, this.unknownValue, this.errorMessage);
         } else {
            StringBuilder missing = new StringBuilder();
            if (this.items == null) {
               missing.append(" items");
            }

            if (this.errorMessage == null) {
               missing.append(" errorMessage");
            }

            throw new IllegalStateException("Missing required properties:" + missing);
         }
      }
   }
}
