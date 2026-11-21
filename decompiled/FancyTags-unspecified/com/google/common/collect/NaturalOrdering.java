package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.io.Serializable;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
@GwtCompatible(
   serializable = true
)
final class NaturalOrdering extends Ordering<Comparable<?>> implements Serializable {
   static final NaturalOrdering INSTANCE = new NaturalOrdering();
   @LazyInit
   @CheckForNull
   private transient Ordering<Comparable<?>> nullsFirst;
   @LazyInit
   @CheckForNull
   private transient Ordering<Comparable<?>> nullsLast;
   private static final long serialVersionUID = 0L;

   public int compare(Comparable<?> left, Comparable<?> right) {
      Preconditions.checkNotNull(left);
      Preconditions.checkNotNull(right);
      return ((Comparable<Comparable<?>>)left).compareTo(right);
   }

   @Override
   public <S extends Comparable<?>> Ordering<S> nullsFirst() {
      Ordering<Comparable<?>> result = this.nullsFirst;
      if (result == null) {
         result = this.nullsFirst = super.nullsFirst();
      }

      return result;
   }

   @Override
   public <S extends Comparable<?>> Ordering<S> nullsLast() {
      Ordering<Comparable<?>> result = this.nullsLast;
      if (result == null) {
         result = this.nullsLast = super.nullsLast();
      }

      return result;
   }

   @Override
   public <S extends Comparable<?>> Ordering<S> reverse() {
      return ReverseNaturalOrdering.INSTANCE;
   }

   private Object readResolve() {
      return INSTANCE;
   }

   @Override
   public String toString() {
      return "Ordering.natural()";
   }

   private NaturalOrdering() {
   }
}
