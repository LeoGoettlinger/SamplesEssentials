package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import java.io.Serializable;
import java.util.Iterator;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
@GwtCompatible(
   serializable = true
)
final class PairwiseEquivalence<E, T extends E> extends Equivalence<Iterable<T>> implements Serializable {
   final Equivalence<E> elementEquivalence;
   private static final long serialVersionUID = 1L;

   PairwiseEquivalence(Equivalence<E> elementEquivalence) {
      this.elementEquivalence = Preconditions.checkNotNull(elementEquivalence);
   }

   protected boolean doEquivalent(Iterable<T> iterableA, Iterable<T> iterableB) {
      Iterator<T> iteratorA = iterableA.iterator();
      Iterator<T> iteratorB = iterableB.iterator();

      while (iteratorA.hasNext() && iteratorB.hasNext()) {
         if (!this.elementEquivalence.equivalent((E)iteratorA.next(), (E)iteratorB.next())) {
            return false;
         }
      }

      return !iteratorA.hasNext() && !iteratorB.hasNext();
   }

   protected int doHash(Iterable<T> iterable) {
      int hash = 78721;

      for (T element : iterable) {
         hash = hash * 24943 + this.elementEquivalence.hash((E)element);
      }

      return hash;
   }

   @Override
   public boolean equals(@CheckForNull Object object) {
      if (object instanceof PairwiseEquivalence) {
         PairwiseEquivalence<Object, Object> that = (PairwiseEquivalence<Object, Object>)object;
         return this.elementEquivalence.equals(that.elementEquivalence);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.elementEquivalence.hashCode() ^ 1185147655;
   }

   @Override
   public String toString() {
      return this.elementEquivalence + ".pairwise()";
   }
}
