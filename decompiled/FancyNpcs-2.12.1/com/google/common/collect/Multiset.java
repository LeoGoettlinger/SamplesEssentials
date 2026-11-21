package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CompatibleWith;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
@GwtCompatible
public interface Multiset<E> extends Collection<E> {
   @Override
   int size();

   int count(@CheckForNull @CompatibleWith("E") Object element);

   @CanIgnoreReturnValue
   int add(@ParametricNullness E element, int occurrences);

   @CanIgnoreReturnValue
   @Override
   boolean add(@ParametricNullness E element);

   @CanIgnoreReturnValue
   int remove(@CheckForNull @CompatibleWith("E") Object element, int occurrences);

   @CanIgnoreReturnValue
   @Override
   boolean remove(@CheckForNull Object element);

   @CanIgnoreReturnValue
   int setCount(@ParametricNullness E element, int count);

   @CanIgnoreReturnValue
   boolean setCount(@ParametricNullness E element, int oldCount, int newCount);

   Set<E> elementSet();

   Set<Multiset.Entry<E>> entrySet();

   default void forEachEntry(ObjIntConsumer<? super E> action) {
      Preconditions.checkNotNull(action);
      this.entrySet().forEach(entry -> action.accept(entry.getElement(), entry.getCount()));
   }

   @Override
   boolean equals(@CheckForNull Object object);

   @Override
   int hashCode();

   @Override
   String toString();

   @Override
   Iterator<E> iterator();

   @Override
   boolean contains(@CheckForNull Object element);

   @Override
   boolean containsAll(Collection<?> elements);

   @CanIgnoreReturnValue
   @Override
   boolean removeAll(Collection<?> c);

   @CanIgnoreReturnValue
   @Override
   boolean retainAll(Collection<?> c);

   @Override
   default void forEach(Consumer<? super E> action) {
      Preconditions.checkNotNull(action);
      this.entrySet().forEach(entry -> {
         E elem = entry.getElement();
         int count = entry.getCount();

         for (int i = 0; i < count; i++) {
            action.accept(elem);
         }
      });
   }

   @Override
   default Spliterator<E> spliterator() {
      return Multisets.spliteratorImpl(this);
   }

   public interface Entry<E> {
      @ParametricNullness
      E getElement();

      int getCount();

      @Override
      boolean equals(@CheckForNull Object o);

      @Override
      int hashCode();

      @Override
      String toString();
   }
}
