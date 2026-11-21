package de.tr7zw.nbtapi.iface;

import de.tr7zw.nbtapi.NBTType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface ReadableNBTList<T> extends Iterable<T> {
   T get(int var1);

   int size();

   NBTType getType();

   boolean isEmpty();

   boolean contains(Object var1);

   int indexOf(Object var1);

   boolean containsAll(Collection<?> var1);

   int lastIndexOf(Object var1);

   Object[] toArray();

   <E> E[] toArray(E[] var1);

   List<T> subList(int var1, int var2);

   default List<T> toListCopy() {
      List<T> list = new ArrayList<>();
      this.iterator().forEachRemaining(list::add);
      return list;
   }
}
