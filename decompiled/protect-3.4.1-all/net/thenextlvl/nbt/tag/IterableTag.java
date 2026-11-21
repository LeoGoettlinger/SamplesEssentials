package net.thenextlvl.nbt.tag;

import java.util.Iterator;
import org.jetbrains.annotations.Contract;

public interface IterableTag<E> extends Iterable<E> {
   @Contract(
      pure = true
   )
   int size();

   @Contract(
      pure = true
   )
   E get(int var1);

   @Contract(
      pure = true
   )
   @Override
   default Iterator<E> iterator() {
      return new Iterator<E>() {
         private int index;

         @Override
         public boolean hasNext() {
            return this.index < IterableTag.this.size();
         }

         @Override
         public E next() {
            return (E)IterableTag.this.get(this.index++);
         }
      };
   }
}
