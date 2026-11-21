package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
@GwtCompatible
public abstract class ForwardingSortedSet<E> extends ForwardingSet<E> implements SortedSet<E> {
   protected ForwardingSortedSet() {
   }

   protected abstract SortedSet<E> delegate();

   @CheckForNull
   @Override
   public Comparator<? super E> comparator() {
      return this.delegate().comparator();
   }

   @ParametricNullness
   @Override
   public E first() {
      return this.delegate().first();
   }

   @Override
   public SortedSet<E> headSet(@ParametricNullness E toElement) {
      return this.delegate().headSet(toElement);
   }

   @ParametricNullness
   @Override
   public E last() {
      return this.delegate().last();
   }

   @Override
   public SortedSet<E> subSet(@ParametricNullness E fromElement, @ParametricNullness E toElement) {
      return this.delegate().subSet(fromElement, toElement);
   }

   @Override
   public SortedSet<E> tailSet(@ParametricNullness E fromElement) {
      return this.delegate().tailSet(fromElement);
   }

   @Override
   protected boolean standardContains(@CheckForNull Object object) {
      try {
         Object ceiling = this.tailSet((E)object).first();
         return ForwardingSortedMap.unsafeCompare(this.comparator(), ceiling, object) == 0;
      } catch (NoSuchElementException | NullPointerException | ClassCastException var4) {
         return false;
      }
   }

   @Override
   protected boolean standardRemove(@CheckForNull Object object) {
      try {
         Iterator<?> iterator = this.tailSet((E)object).iterator();
         if (iterator.hasNext()) {
            Object ceiling = iterator.next();
            if (ForwardingSortedMap.unsafeCompare(this.comparator(), ceiling, object) == 0) {
               iterator.remove();
               return true;
            }
         }

         return false;
      } catch (NullPointerException | ClassCastException var5) {
         return false;
      }
   }

   protected SortedSet<E> standardSubSet(@ParametricNullness E fromElement, @ParametricNullness E toElement) {
      return this.tailSet(fromElement).headSet(toElement);
   }
}
