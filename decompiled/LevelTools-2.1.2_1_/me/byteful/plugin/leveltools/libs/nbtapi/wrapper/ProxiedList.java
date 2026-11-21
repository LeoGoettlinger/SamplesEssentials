package me.byteful.plugin.leveltools.libs.nbtapi.wrapper;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import me.byteful.plugin.leveltools.libs.nbtapi.iface.ReadWriteNBT;
import me.byteful.plugin.leveltools.libs.nbtapi.iface.ReadWriteNBTCompoundList;

class ProxiedList<E extends NBTProxy> implements ProxyList<E> {
   private final ReadWriteNBTCompoundList nbt;
   private final Class<E> proxy;

   public ProxiedList(ReadWriteNBTCompoundList nbt, Class<E> proxyClass) {
      this.nbt = nbt;
      this.proxy = proxyClass;
   }

   @Override
   public E get(int index) {
      ReadWriteNBT tag = this.nbt.get(index);
      return new ProxyBuilder<>(tag, this.proxy).build();
   }

   @Override
   public int size() {
      return this.nbt.size();
   }

   @Override
   public void remove(int index) {
      this.nbt.remove(index);
   }

   @Override
   public Iterator<E> iterator() {
      return new ProxiedList.Itr();
   }

   @Override
   public E addCompound() {
      ReadWriteNBT tag = this.nbt.addCompound();
      return new ProxyBuilder<>(tag, this.proxy).build();
   }

   @Override
   public boolean isEmpty() {
      return this.nbt.isEmpty();
   }

   private class Itr implements Iterator<E> {
      int cursor = 0;
      int lastRet = -1;

      private Itr() {
      }

      @Override
      public boolean hasNext() {
         return this.cursor != ProxiedList.this.size();
      }

      public E next() {
         try {
            int i = this.cursor;
            E next = ProxiedList.this.get(i);
            this.lastRet = i;
            this.cursor = i + 1;
            return next;
         } catch (IndexOutOfBoundsException var3) {
            throw new NoSuchElementException();
         }
      }

      @Override
      public void remove() {
         if (this.lastRet < 0) {
            throw new IllegalStateException();
         } else {
            try {
               ProxiedList.this.remove(this.lastRet);
               if (this.lastRet < this.cursor) {
                  this.cursor--;
               }

               this.lastRet = -1;
            } catch (IndexOutOfBoundsException var2) {
               throw new ConcurrentModificationException();
            }
         }
      }
   }
}
