package me.byteful.plugin.leveltools.libs.nbtapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import me.byteful.plugin.leveltools.libs.nbtapi.iface.ReadWriteNBTList;
import me.byteful.plugin.leveltools.libs.nbtapi.utils.MinecraftVersion;
import me.byteful.plugin.leveltools.libs.nbtapi.utils.nmsmappings.ReflectionMethod;

public abstract class NBTList<T> implements List<T>, ReadWriteNBTList<T> {
   private String listName;
   private NBTCompound parent;
   private NBTType type;
   protected Object listObject;

   protected NBTList(NBTCompound owner, String name, NBTType type, Object list) {
      this.parent = owner;
      this.listName = name;
      this.type = type;
      this.listObject = list;
   }

   public String getName() {
      return this.listName;
   }

   public NBTCompound getParent() {
      return this.parent;
   }

   private void validateClosed() {
      if (this.parent.isClosed()) {
         throw new NbtApiException("Tried using closed NBT data!");
      }
   }

   private void validateWritable() {
      if (this.getParent().isReadOnly()) {
         throw new NbtApiException("Tried setting data in read only mode!");
      }
   }

   protected void save() {
      this.validateClosed();
      this.parent.set(this.listName, this.listObject);
   }

   protected abstract Object asTag(T var1);

   @Override
   public boolean add(T element) {
      this.validateClosed();
      this.validateWritable();

      boolean ex;
      try {
         this.parent.getWriteLock().lock();
         if (MinecraftVersion.getVersion().getVersionId() >= MinecraftVersion.MC1_14_R1.getVersionId()) {
            ReflectionMethod.LIST_ADD.run(this.listObject, this.size(), this.asTag(element));
         } else {
            ReflectionMethod.LEGACY_LIST_ADD.run(this.listObject, this.asTag(element));
         }

         this.save();
         ex = true;
      } catch (Exception var6) {
         throw new NbtApiException(var6);
      } finally {
         this.parent.getWriteLock().unlock();
      }

      return ex;
   }

   @Override
   public void add(int index, T element) {
      this.validateClosed();
      this.validateWritable();

      try {
         this.parent.getWriteLock().lock();
         if (MinecraftVersion.getVersion().getVersionId() >= MinecraftVersion.MC1_14_R1.getVersionId()) {
            ReflectionMethod.LIST_ADD.run(this.listObject, index, this.asTag(element));
         } else {
            ReflectionMethod.LEGACY_LIST_ADD.run(this.listObject, this.asTag(element));
         }

         this.save();
      } catch (Exception var7) {
         throw new NbtApiException(var7);
      } finally {
         this.parent.getWriteLock().unlock();
      }
   }

   @Override
   public T set(int index, T element) {
      this.validateClosed();
      this.validateWritable();

      Object var4;
      try {
         this.parent.getWriteLock().lock();
         T prev = this.get(index);
         ReflectionMethod.LIST_SET.run(this.listObject, index, this.asTag(element));
         this.save();
         var4 = prev;
      } catch (Exception var8) {
         throw new NbtApiException(var8);
      } finally {
         this.parent.getWriteLock().unlock();
      }

      return (T)var4;
   }

   @Override
   public T remove(int i) {
      this.validateClosed();
      this.validateWritable();

      Object var3;
      try {
         this.parent.getWriteLock().lock();
         T old = this.get(i);
         ReflectionMethod.LIST_REMOVE_KEY.run(this.listObject, i);
         this.save();
         var3 = old;
      } catch (Exception var7) {
         throw new NbtApiException(var7);
      } finally {
         this.parent.getWriteLock().unlock();
      }

      return (T)var3;
   }

   @Override
   public int size() {
      this.validateClosed();

      int ex;
      try {
         this.parent.getReadLock().lock();
         ex = (Integer)ReflectionMethod.LIST_SIZE.run(this.listObject);
      } catch (Exception var5) {
         throw new NbtApiException(var5);
      } finally {
         this.parent.getReadLock().unlock();
      }

      return ex;
   }

   @Override
   public NBTType getType() {
      return this.type;
   }

   @Override
   public boolean isEmpty() {
      return this.size() == 0;
   }

   @Override
   public void clear() {
      while (!this.isEmpty()) {
         this.remove(0);
      }
   }

   @Override
   public boolean contains(Object o) {
      this.validateClosed();

      try {
         this.parent.getReadLock().lock();

         for (int i = 0; i < this.size(); i++) {
            if (o.equals(this.get(i))) {
               return true;
            }
         }

         return false;
      } finally {
         this.parent.getReadLock().unlock();
      }
   }

   @Override
   public int indexOf(Object o) {
      this.validateClosed();

      try {
         this.parent.getReadLock().lock();

         for (int i = 0; i < this.size(); i++) {
            if (o.equals(this.get(i))) {
               return i;
            }
         }

         return -1;
      } finally {
         this.parent.getReadLock().unlock();
      }
   }

   @Override
   public boolean addAll(Collection<? extends T> c) {
      this.validateClosed();

      boolean var8;
      try {
         this.parent.getWriteLock().lock();
         int size = this.size();

         for (T ele : c) {
            this.add(ele);
         }

         var8 = size != this.size();
      } finally {
         this.parent.getWriteLock().unlock();
      }

      return var8;
   }

   @Override
   public boolean addAll(int index, Collection<? extends T> c) {
      this.validateClosed();

      boolean var9;
      try {
         this.parent.getWriteLock().lock();
         int size = this.size();

         for (T ele : c) {
            this.add(index++, ele);
         }

         var9 = size != this.size();
      } finally {
         this.parent.getWriteLock().unlock();
      }

      return var9;
   }

   @Override
   public boolean containsAll(Collection<?> c) {
      this.validateClosed();

      boolean var4;
      try {
         this.parent.getReadLock().lock();
         Iterator var2 = c.iterator();

         Object ele;
         do {
            if (!var2.hasNext()) {
               return true;
            }

            ele = var2.next();
         } while (this.contains(ele));

         var4 = false;
      } finally {
         this.parent.getReadLock().unlock();
      }

      return var4;
   }

   @Override
   public int lastIndexOf(Object o) {
      this.validateClosed();

      int var7;
      try {
         this.parent.getReadLock().lock();
         int index = -1;

         for (int i = 0; i < this.size(); i++) {
            if (o.equals(this.get(i))) {
               index = i;
            }
         }

         var7 = index;
      } finally {
         this.parent.getReadLock().unlock();
      }

      return var7;
   }

   @Override
   public boolean removeAll(Collection<?> c) {
      this.validateClosed();

      boolean var8;
      try {
         this.parent.getWriteLock().lock();
         int size = this.size();

         for (Object obj : c) {
            this.remove(obj);
         }

         var8 = size != this.size();
      } finally {
         this.parent.getWriteLock().unlock();
      }

      return var8;
   }

   @Override
   public boolean retainAll(Collection<?> c) {
      this.validateClosed();

      boolean var9;
      try {
         this.parent.getWriteLock().lock();
         int size = this.size();

         for (Object obj : c) {
            for (int i = 0; i < this.size(); i++) {
               if (!obj.equals(this.get(i))) {
                  this.remove(i--);
               }
            }
         }

         var9 = size != this.size();
      } finally {
         this.parent.getWriteLock().unlock();
      }

      return var9;
   }

   @Override
   public boolean remove(Object o) {
      this.validateClosed();

      boolean var4;
      try {
         this.parent.getWriteLock().lock();
         int size = this.size();
         int id = this.indexOf(o);
         if (id != -1) {
            this.remove(id);
         }

         var4 = size != this.size();
      } finally {
         this.parent.getWriteLock().unlock();
      }

      return var4;
   }

   @Override
   public Iterator<T> iterator() {
      return new Iterator<T>() {
         private int index = -1;

         @Override
         public boolean hasNext() {
            return NBTList.this.size() > this.index + 1;
         }

         @Override
         public T next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            } else {
               return (T)NBTList.this.get(++this.index);
            }
         }

         @Override
         public void remove() {
            NBTList.this.remove(this.index);
            this.index--;
         }
      };
   }

   @Override
   public ListIterator<T> listIterator() {
      return this.listIterator(0);
   }

   @Override
   public ListIterator<T> listIterator(final int startIndex) {
      final NBTList<T> list = this;
      return new ListIterator<T>() {
         int index = startIndex - 1;

         @Override
         public void add(T e) {
            list.add(this.index, e);
         }

         @Override
         public boolean hasNext() {
            return NBTList.this.size() > this.index + 1;
         }

         @Override
         public boolean hasPrevious() {
            return this.index >= 0 && this.index <= NBTList.this.size();
         }

         @Override
         public T next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            } else {
               return (T)NBTList.this.get(++this.index);
            }
         }

         @Override
         public int nextIndex() {
            return this.index + 1;
         }

         @Override
         public T previous() {
            if (!this.hasPrevious()) {
               throw new NoSuchElementException("Id: " + (this.index - 1));
            } else {
               return (T)NBTList.this.get(this.index--);
            }
         }

         @Override
         public int previousIndex() {
            return this.index - 1;
         }

         @Override
         public void remove() {
            list.remove(this.index);
            this.index--;
         }

         @Override
         public void set(T e) {
            list.set(this.index, e);
         }
      };
   }

   @Override
   public Object[] toArray() {
      this.validateClosed();

      Object[] var6;
      try {
         this.parent.getReadLock().lock();
         Object[] ar = new Object[this.size()];

         for (int i = 0; i < this.size(); i++) {
            ar[i] = this.get(i);
         }

         var6 = ar;
      } finally {
         this.parent.getReadLock().unlock();
      }

      return var6;
   }

   @Override
   public <E> E[] toArray(E[] a) {
      this.validateClosed();

      Object[] var9;
      try {
         this.parent.getReadLock().lock();
         E[] ar = Arrays.copyOf(a, this.size());
         Arrays.fill(ar, null);
         Class<?> arrayclass = a.getClass().getComponentType();

         for (int i = 0; i < this.size(); i++) {
            T obj = this.get(i);
            if (!arrayclass.isInstance(obj)) {
               throw new ArrayStoreException("The array does not match the objects stored in the List.");
            }

            ar[i] = (E)this.get(i);
         }

         var9 = ar;
      } finally {
         this.parent.getReadLock().unlock();
      }

      return (E[])var9;
   }

   @Override
   public List<T> subList(int fromIndex, int toIndex) {
      this.validateClosed();

      ArrayList var8;
      try {
         this.parent.getReadLock().lock();
         ArrayList<T> list = new ArrayList<>();

         for (int i = fromIndex; i < toIndex; i++) {
            list.add(this.get(i));
         }

         var8 = list;
      } finally {
         this.parent.getReadLock().unlock();
      }

      return var8;
   }

   @Override
   public boolean removeIf(Predicate<? super T> filter) {
      return List.super.removeIf(filter);
   }

   @Override
   public String toString() {
      this.validateClosed();

      String var1;
      try {
         this.parent.getReadLock().lock();
         var1 = this.listObject.toString();
      } finally {
         this.parent.getReadLock().unlock();
      }

      return var1;
   }
}
