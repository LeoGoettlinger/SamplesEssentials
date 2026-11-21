package org.enginehub.linbus.tree;

import java.util.Objects;
import org.enginehub.linbus.stream.LinStreamable;
import org.jspecify.annotations.Nullable;

public abstract sealed class LinTag<T>
   implements ToLinTag<LinTag<T>>,
   LinStreamable
   permits LinByteArrayTag,
   LinCompoundTag,
   LinEndTag,
   LinIntArrayTag,
   LinListTag,
   LinLongArrayTag,
   LinNumberTag,
   LinStringTag {
   protected LinTag() {
   }

   public abstract LinTagType<? extends LinTag<T>> type();

   public abstract T value();

   @Override
   public final LinTag<T> toLinTag() {
      return this;
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         LinTag<?> that = (LinTag<?>)o;
         return Objects.equals(this.value(), that.value());
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.value().hashCode();
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + "[" + this.value() + "]";
   }
}
