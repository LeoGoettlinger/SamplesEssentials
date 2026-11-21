package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.VisibleForTesting;
import java.util.Spliterator;
import java.util.Spliterators;

@ElementTypesAreNonnullByDefault
@GwtCompatible(
   serializable = true,
   emulated = true
)
class RegularImmutableList<E> extends ImmutableList<E> {
   static final ImmutableList<Object> EMPTY = new RegularImmutableList<>(new Object[0]);
   @VisibleForTesting
   final transient Object[] array;

   RegularImmutableList(Object[] array) {
      this.array = array;
   }

   @Override
   public int size() {
      return this.array.length;
   }

   @Override
   boolean isPartialView() {
      return false;
   }

   @Override
   Object[] internalArray() {
      return this.array;
   }

   @Override
   int internalArrayStart() {
      return 0;
   }

   @Override
   int internalArrayEnd() {
      return this.array.length;
   }

   @Override
   int copyIntoArray(Object[] dst, int dstOff) {
      System.arraycopy(this.array, 0, dst, dstOff, this.array.length);
      return dstOff + this.array.length;
   }

   @Override
   public E get(int index) {
      return (E)this.array[index];
   }

   @Override
   public UnmodifiableListIterator<E> listIterator(int index) {
      return Iterators.forArray((E[])this.array, 0, this.array.length, index);
   }

   @Override
   public Spliterator<E> spliterator() {
      return Spliterators.spliterator(this.array, 1296);
   }
}
