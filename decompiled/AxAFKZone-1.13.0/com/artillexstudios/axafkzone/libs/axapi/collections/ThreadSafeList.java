package com.artillexstudios.axafkzone.libs.axapi.collections;

import com.artillexstudios.axafkzone.libs.axapi.utils.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.function.Consumer;

public class ThreadSafeList<E> extends ArrayList<E> {
   private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
   private final ReadLock read = this.lock.readLock();
   private final WriteLock write = this.lock.writeLock();

   @Override
   public int indexOf(Object o) {
      this.read.lock();

      int var2;
      try {
         var2 = super.indexOf(o);
      } finally {
         this.read.unlock();
      }

      return var2;
   }

   @Override
   public int lastIndexOf(Object o) {
      this.read.lock();

      int var2;
      try {
         var2 = super.lastIndexOf(o);
      } finally {
         this.read.unlock();
      }

      return var2;
   }

   @Override
   public void add(int index, E element) {
      this.write.lock();

      try {
         super.add(index, element);
      } finally {
         this.write.unlock();
      }
   }

   @Override
   public E get(int index) {
      this.read.lock();

      Object var2;
      try {
         var2 = super.get(index);
      } finally {
         this.read.unlock();
      }

      return (E)var2;
   }

   @Override
   public E set(int index, E element) {
      this.write.lock();

      Object var3;
      try {
         var3 = super.set(index, element);
      } finally {
         this.write.unlock();
      }

      return (E)var3;
   }

   @Override
   public E remove(int index) {
      this.write.lock();

      Object var2;
      try {
         var2 = super.remove(index);
      } finally {
         this.write.unlock();
      }

      return (E)var2;
   }

   @Override
   public boolean remove(Object o) {
      this.write.lock();

      boolean var2;
      try {
         var2 = super.remove(o);
      } finally {
         this.write.unlock();
      }

      return var2;
   }

   @Override
   public boolean removeAll(Collection<?> c) {
      this.write.lock();

      boolean var2;
      try {
         var2 = super.removeAll(c);
      } finally {
         this.write.unlock();
      }

      return var2;
   }

   @Override
   public void forEach(Consumer<? super E> action) {
      this.read.lock();

      try {
         super.forEach(action);
      } finally {
         this.read.unlock();
      }
   }

   @Override
   public boolean add(E e) {
      this.write.lock();

      boolean var2;
      try {
         var2 = super.add(e);
      } finally {
         this.write.unlock();
      }

      return var2;
   }

   @Override
   public void sort(Comparator<? super E> c) {
      this.write.lock();

      try {
         super.sort(c);
      } finally {
         this.write.unlock();
      }
   }

   @Deprecated
   @Override
   public ListIterator<E> listIterator() {
      UnsupportedOperationException exception = new UnsupportedOperationException("The iterator should not be used!");
      LogUtils.warn("Unsupported operation! You should not be using any iterator from the ThreadSafeList! Advanced for loops use the iterator!", exception);
      return super.listIterator();
   }

   @Deprecated
   @Override
   public ListIterator<E> listIterator(int index) {
      UnsupportedOperationException exception = new UnsupportedOperationException("The iterator should not be used!");
      LogUtils.warn("Unsupported operation! You should not be using any iterator from the ThreadSafeList! Advanced for loops use the iterator!", exception);
      return super.listIterator(index);
   }

   @Deprecated
   @Override
   public Iterator<E> iterator() {
      UnsupportedOperationException exception = new UnsupportedOperationException("The iterator should not be used!");
      LogUtils.warn("Unsupported operation! You should not be using any iterator from the ThreadSafeList! Advanced for loops use the iterator!", exception);
      return super.iterator();
   }
}
