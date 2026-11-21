package de.tr7zw.nbtapi.wrapper;

public interface ProxyList<T extends NBTProxy> extends Iterable<T> {
   T addCompound();

   int size();

   boolean isEmpty();

   T get(int var1);

   void remove(int var1);
}
