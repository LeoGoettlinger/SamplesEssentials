package org.enginehub.linbus.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.internal.FlatteningLinStream;
import org.enginehub.linbus.stream.internal.SurroundingLinStream;
import org.enginehub.linbus.stream.token.LinToken;

public final class LinListTag<T extends LinTag<?>> extends LinTag<List<T>> {
   private final LinTagType<T> elementType;
   private final List<T> value;

   public static <T extends LinTag<?>> LinListTag<T> of(LinTagType<T> elementType, List<T> value) {
      for (T t : value) {
         if (t.type() != elementType) {
            throw new IllegalArgumentException("Element is not of type " + elementType.name() + " but " + t.type().name());
         }
      }

      return new LinListTag<>(elementType, List.copyOf(value));
   }

   public static <T extends LinTag<?>> LinListTag<T> empty(LinTagType<T> elementType) {
      return builder(elementType).build();
   }

   public static <T extends LinTag<?>> LinListTag.Builder<T> builder(LinTagType<T> elementType) {
      return new LinListTag.Builder<>(elementType);
   }

   private LinListTag(LinTagType<T> elementType, List<T> value) {
      Objects.requireNonNull(value, "value is null");
      if (!value.isEmpty() && elementType == LinTagType.endTag()) {
         throw new IllegalArgumentException("A non-empty list cannot be of type END");
      } else {
         this.elementType = elementType;
         this.value = value;
      }
   }

   @Override
   public LinTagType<LinListTag<T>> type() {
      return LinTagType.listTag();
   }

   public LinTagType<T> elementType() {
      return this.elementType;
   }

   public <U extends LinTag<?>> LinListTag<U> asTypeChecked(LinTagType<U> elementType) {
      if (elementType != this.elementType) {
         throw new IllegalStateException("List is of type " + this.elementType.name() + ", not " + elementType.name());
      } else {
         return this;
      }
   }

   public List<T> value() {
      return this.value;
   }

   @Override
   public LinStream linStream() {
      return new SurroundingLinStream(
         new LinToken.ListStart(this.value.size(), this.elementType.id()), new FlatteningLinStream(this.value.iterator()), new LinToken.ListEnd()
      );
   }

   public T get(int index) {
      return this.value.get(index);
   }

   public LinListTag.Builder<T> toBuilder() {
      return new LinListTag.Builder<>(this);
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + this.value;
   }

   public static final class Builder<T extends LinTag<?>> {
      private final LinTagType<T> elementType;
      private final List<T> collector;

      private Builder(LinTagType<T> elementType) {
         this.elementType = elementType;
         this.collector = new ArrayList<>();
      }

      private Builder(LinListTag<T> base) {
         this.elementType = base.elementType;
         this.collector = new ArrayList<>(base.value);
      }

      public LinListTag.Builder<T> add(T tag) {
         if (tag.type() != this.elementType) {
            throw new IllegalArgumentException("Element is not of type " + this.elementType.name() + " but " + tag.type().name());
         } else {
            this.collector.add(tag);
            return this;
         }
      }

      public LinListTag.Builder<T> addAll(Collection<? extends T> tags) {
         tags.forEach(this::add);
         return this;
      }

      public LinListTag.Builder<T> set(int index, T tag) {
         if (tag.type() != this.elementType) {
            throw new IllegalArgumentException("Element is not of type " + this.elementType.name() + " but " + tag.type().name());
         } else {
            this.collector.set(index, tag);
            return this;
         }
      }

      public LinListTag<T> build() {
         return new LinListTag<>(this.elementType, List.copyOf(this.collector));
      }
   }
}
