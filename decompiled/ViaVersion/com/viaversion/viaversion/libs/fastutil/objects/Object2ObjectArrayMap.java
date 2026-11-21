package com.viaversion.viaversion.libs.fastutil.objects;

import com.viaversion.viaversion.libs.fastutil.Pair;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.Consumer;

public class Object2ObjectArrayMap<K, V> extends AbstractObject2ObjectMap<K, V> implements Serializable, Cloneable {
   private static final long serialVersionUID = 1L;
   protected transient Object[] key;
   protected transient Object[] value;
   protected int size;
   protected transient Object2ObjectMap.FastEntrySet<K, V> entries;
   protected transient ObjectSet<K> keys;
   protected transient ObjectCollection<V> values;

   public Object2ObjectArrayMap(Object[] key, Object[] value) {
      this.key = key;
      this.value = value;
      this.size = key.length;
      if (key.length != value.length) {
         throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
      }
   }

   public Object2ObjectArrayMap() {
      this.key = ObjectArrays.EMPTY_ARRAY;
      this.value = ObjectArrays.EMPTY_ARRAY;
   }

   public Object2ObjectArrayMap(int capacity) {
      this.key = new Object[capacity];
      this.value = new Object[capacity];
   }

   public Object2ObjectArrayMap(Object2ObjectMap<K, V> m) {
      this(m.size());
      int i = 0;

      for (Object2ObjectMap.Entry<K, V> e : m.object2ObjectEntrySet()) {
         this.key[i] = e.getKey();
         this.value[i] = e.getValue();
         i++;
      }

      this.size = i;
   }

   public Object2ObjectArrayMap(Map<? extends K, ? extends V> m) {
      this(m.size());
      int i = 0;

      for (Entry<? extends K, ? extends V> e : m.entrySet()) {
         this.key[i] = e.getKey();
         this.value[i] = e.getValue();
         i++;
      }

      this.size = i;
   }

   public Object2ObjectArrayMap(Object[] key, Object[] value, int size) {
      this.key = key;
      this.value = value;
      this.size = size;
      if (key.length != value.length) {
         throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
      } else if (size > key.length) {
         throw new IllegalArgumentException("The provided size (" + size + ") is larger than or equal to the backing-arrays size (" + key.length + ")");
      }
   }

   public Object2ObjectMap.FastEntrySet<K, V> object2ObjectEntrySet() {
      if (this.entries == null) {
         this.entries = new Object2ObjectArrayMap.EntrySet();
      }

      return this.entries;
   }

   private int findKey(Object k) {
      Object[] key = this.key;
      int i = this.size;

      while (i-- != 0) {
         if (Objects.equals(key[i], k)) {
            return i;
         }
      }

      return -1;
   }

   @Override
   public V get(Object k) {
      Object[] key = this.key;
      int i = this.size;

      while (i-- != 0) {
         if (Objects.equals(key[i], k)) {
            return (V)this.value[i];
         }
      }

      return this.defRetValue;
   }

   @Override
   public int size() {
      return this.size;
   }

   @Override
   public void clear() {
      Object[] key = this.key;
      Object[] value = this.value;

      for (int i = this.size; i-- != 0; value[i] = null) {
         key[i] = null;
      }

      this.size = 0;
   }

   @Override
   public boolean containsKey(Object k) {
      return this.findKey(k) != -1;
   }

   @Override
   public boolean containsValue(Object v) {
      Object[] value = this.value;
      int i = this.size;

      while (i-- != 0) {
         if (Objects.equals(value[i], v)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean isEmpty() {
      return this.size == 0;
   }

   @Override
   public V put(K k, V v) {
      int oldKey = this.findKey(k);
      if (oldKey != -1) {
         V oldValue = (V)this.value[oldKey];
         this.value[oldKey] = v;
         return oldValue;
      } else {
         if (this.size == this.key.length) {
            Object[] newKey = new Object[this.size == 0 ? 2 : this.size * 2];
            Object[] newValue = new Object[this.size == 0 ? 2 : this.size * 2];

            for (int i = this.size; i-- != 0; newValue[i] = this.value[i]) {
               newKey[i] = this.key[i];
            }

            this.key = newKey;
            this.value = newValue;
         }

         this.key[this.size] = k;
         this.value[this.size] = v;
         this.size++;
         return this.defRetValue;
      }
   }

   @Override
   public V remove(Object k) {
      int oldPos = this.findKey(k);
      if (oldPos == -1) {
         return this.defRetValue;
      } else {
         V oldValue = (V)this.value[oldPos];
         int tail = this.size - oldPos - 1;
         System.arraycopy(this.key, oldPos + 1, this.key, oldPos, tail);
         System.arraycopy(this.value, oldPos + 1, this.value, oldPos, tail);
         this.size--;
         this.key[this.size] = null;
         this.value[this.size] = null;
         return oldValue;
      }
   }

   @Override
   public ObjectSet<K> keySet() {
      if (this.keys == null) {
         this.keys = new Object2ObjectArrayMap.KeySet();
      }

      return this.keys;
   }

   @Override
   public ObjectCollection<V> values() {
      if (this.values == null) {
         this.values = new Object2ObjectArrayMap.ValuesCollection();
      }

      return this.values;
   }

   public Object2ObjectArrayMap<K, V> clone() {
      Object2ObjectArrayMap<K, V> c;
      try {
         c = (Object2ObjectArrayMap<K, V>)super.clone();
      } catch (CloneNotSupportedException var3) {
         throw new InternalError();
      }

      c.key = (Object[])this.key.clone();
      c.value = (Object[])this.value.clone();
      c.entries = null;
      c.keys = null;
      c.values = null;
      return c;
   }

   private void writeObject(ObjectOutputStream s) throws IOException {
      s.defaultWriteObject();
      Object[] key = this.key;
      Object[] value = this.value;
      int i = 0;

      for (int max = this.size; i < max; i++) {
         s.writeObject(key[i]);
         s.writeObject(value[i]);
      }
   }

   private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
      s.defaultReadObject();
      Object[] key = this.key = new Object[this.size];
      Object[] value = this.value = new Object[this.size];

      for (int i = 0; i < this.size; i++) {
         key[i] = s.readObject();
         value[i] = s.readObject();
      }
   }

   private final class EntrySet extends AbstractObjectSet<Object2ObjectMap.Entry<K, V>> implements Object2ObjectMap.FastEntrySet<K, V> {
      private EntrySet() {
      }

      @Override
      public ObjectIterator<Object2ObjectMap.Entry<K, V>> iterator() {
         return new ObjectIterator<Object2ObjectMap.Entry<K, V>>() {
            private Object2ObjectArrayMap<K, V>.MapEntry entry;
            int curr = -1;
            int next = 0;

            @Override
            public boolean hasNext() {
               return this.next < Object2ObjectArrayMap.this.size;
            }

            public Object2ObjectMap.Entry<K, V> next() {
               if (!this.hasNext()) {
                  throw new NoSuchElementException();
               } else {
                  return this.entry = Object2ObjectArrayMap.this.new MapEntry(this.curr = this.next++);
               }
            }

            @Override
            public void remove() {
               if (this.curr == -1) {
                  throw new IllegalStateException();
               } else {
                  this.curr = -1;
                  int tail = Object2ObjectArrayMap.this.size-- - this.next--;
                  System.arraycopy(Object2ObjectArrayMap.this.key, this.next + 1, Object2ObjectArrayMap.this.key, this.next, tail);
                  System.arraycopy(Object2ObjectArrayMap.this.value, this.next + 1, Object2ObjectArrayMap.this.value, this.next, tail);
                  this.entry.index = -1;
                  Object2ObjectArrayMap.this.key[Object2ObjectArrayMap.this.size] = null;
                  Object2ObjectArrayMap.this.value[Object2ObjectArrayMap.this.size] = null;
               }
            }

            @Override
            public int skip(int n) {
               if (n < 0) {
                  throw new IllegalArgumentException("Argument must be nonnegative: " + n);
               } else {
                  n = Math.min(n, Object2ObjectArrayMap.this.size - this.next);
                  this.next += n;
                  if (n != 0) {
                     this.curr = this.next - 1;
                  }

                  return n;
               }
            }

            @Override
            public void forEachRemaining(Consumer<? super Object2ObjectMap.Entry<K, V>> action) {
               int max = Object2ObjectArrayMap.this.size;

               while (this.next < max) {
                  this.entry = Object2ObjectArrayMap.this.new MapEntry(this.curr = this.next++);
                  action.accept(this.entry);
               }
            }
         };
      }

      @Override
      public ObjectIterator<Object2ObjectMap.Entry<K, V>> fastIterator() {
         return new ObjectIterator<Object2ObjectMap.Entry<K, V>>() {
            private Object2ObjectArrayMap<K, V>.MapEntry entry = Object2ObjectArrayMap.this.new MapEntry();
            int next = 0;
            int curr = -1;

            @Override
            public boolean hasNext() {
               return this.next < Object2ObjectArrayMap.this.size;
            }

            public Object2ObjectMap.Entry<K, V> next() {
               if (!this.hasNext()) {
                  throw new NoSuchElementException();
               } else {
                  this.entry.index = this.curr = this.next++;
                  return this.entry;
               }
            }

            @Override
            public void remove() {
               if (this.curr == -1) {
                  throw new IllegalStateException();
               } else {
                  this.curr = -1;
                  int tail = Object2ObjectArrayMap.this.size-- - this.next--;
                  System.arraycopy(Object2ObjectArrayMap.this.key, this.next + 1, Object2ObjectArrayMap.this.key, this.next, tail);
                  System.arraycopy(Object2ObjectArrayMap.this.value, this.next + 1, Object2ObjectArrayMap.this.value, this.next, tail);
                  this.entry.index = -1;
                  Object2ObjectArrayMap.this.key[Object2ObjectArrayMap.this.size] = null;
                  Object2ObjectArrayMap.this.value[Object2ObjectArrayMap.this.size] = null;
               }
            }

            @Override
            public int skip(int n) {
               if (n < 0) {
                  throw new IllegalArgumentException("Argument must be nonnegative: " + n);
               } else {
                  n = Math.min(n, Object2ObjectArrayMap.this.size - this.next);
                  this.next += n;
                  if (n != 0) {
                     this.curr = this.next - 1;
                  }

                  return n;
               }
            }

            @Override
            public void forEachRemaining(Consumer<? super Object2ObjectMap.Entry<K, V>> action) {
               int max = Object2ObjectArrayMap.this.size;

               while (this.next < max) {
                  this.entry.index = this.curr = this.next++;
                  action.accept(this.entry);
               }
            }
         };
      }

      @Override
      public ObjectSpliterator<Object2ObjectMap.Entry<K, V>> spliterator() {
         return new Object2ObjectArrayMap.EntrySet.EntrySetSpliterator(0, Object2ObjectArrayMap.this.size);
      }

      @Override
      public void forEach(Consumer<? super Object2ObjectMap.Entry<K, V>> action) {
         int i = 0;

         for (int max = Object2ObjectArrayMap.this.size; i < max; i++) {
            action.accept(Object2ObjectArrayMap.this.new MapEntry(i));
         }
      }

      @Override
      public void fastForEach(Consumer<? super Object2ObjectMap.Entry<K, V>> action) {
         Object2ObjectArrayMap<K, V>.MapEntry entry = Object2ObjectArrayMap.this.new MapEntry();
         int i = 0;

         for (int max = Object2ObjectArrayMap.this.size; i < max; i++) {
            entry.index = i;
            action.accept(entry);
         }
      }

      @Override
      public int size() {
         return Object2ObjectArrayMap.this.size;
      }

      @Override
      public boolean contains(Object o) {
         if (!(o instanceof Entry)) {
            return false;
         } else {
            Entry<?, ?> e = (Entry<?, ?>)o;
            K k = (K)e.getKey();
            return Object2ObjectArrayMap.this.containsKey(k) && Objects.equals(Object2ObjectArrayMap.this.get(k), e.getValue());
         }
      }

      @Override
      public boolean remove(Object o) {
         if (!(o instanceof Entry)) {
            return false;
         } else {
            Entry<?, ?> e = (Entry<?, ?>)o;
            K k = (K)e.getKey();
            V v = (V)e.getValue();
            int oldPos = Object2ObjectArrayMap.this.findKey(k);
            if (oldPos != -1 && Objects.equals(v, Object2ObjectArrayMap.this.value[oldPos])) {
               int tail = Object2ObjectArrayMap.this.size - oldPos - 1;
               System.arraycopy(Object2ObjectArrayMap.this.key, oldPos + 1, Object2ObjectArrayMap.this.key, oldPos, tail);
               System.arraycopy(Object2ObjectArrayMap.this.value, oldPos + 1, Object2ObjectArrayMap.this.value, oldPos, tail);
               Object2ObjectArrayMap.this.size--;
               Object2ObjectArrayMap.this.key[Object2ObjectArrayMap.this.size] = null;
               Object2ObjectArrayMap.this.value[Object2ObjectArrayMap.this.size] = null;
               return true;
            } else {
               return false;
            }
         }
      }

      final class EntrySetSpliterator
         extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Object2ObjectMap.Entry<K, V>>
         implements ObjectSpliterator<Object2ObjectMap.Entry<K, V>> {
         EntrySetSpliterator(int pos, int maxPos) {
            super(pos, maxPos);
         }

         @Override
         public int characteristics() {
            return 16465;
         }

         protected final Object2ObjectMap.Entry<K, V> get(int location) {
            return Object2ObjectArrayMap.this.new MapEntry(location);
         }

         protected final Object2ObjectArrayMap<K, V>.EntrySet.EntrySetSpliterator makeForSplit(int pos, int maxPos) {
            return EntrySet.this.new EntrySetSpliterator(pos, maxPos);
         }
      }
   }

   private final class KeySet extends AbstractObjectSet<K> {
      private KeySet() {
      }

      @Override
      public boolean contains(Object k) {
         return Object2ObjectArrayMap.this.findKey(k) != -1;
      }

      @Override
      public boolean remove(Object k) {
         int oldPos = Object2ObjectArrayMap.this.findKey(k);
         if (oldPos == -1) {
            return false;
         } else {
            int tail = Object2ObjectArrayMap.this.size - oldPos - 1;
            System.arraycopy(Object2ObjectArrayMap.this.key, oldPos + 1, Object2ObjectArrayMap.this.key, oldPos, tail);
            System.arraycopy(Object2ObjectArrayMap.this.value, oldPos + 1, Object2ObjectArrayMap.this.value, oldPos, tail);
            Object2ObjectArrayMap.this.size--;
            Object2ObjectArrayMap.this.key[Object2ObjectArrayMap.this.size] = null;
            Object2ObjectArrayMap.this.value[Object2ObjectArrayMap.this.size] = null;
            return true;
         }
      }

      @Override
      public ObjectIterator<K> iterator() {
         return new ObjectIterator<K>() {
            int pos = 0;

            @Override
            public boolean hasNext() {
               return this.pos < Object2ObjectArrayMap.this.size;
            }

            @Override
            public K next() {
               if (!this.hasNext()) {
                  throw new NoSuchElementException();
               } else {
                  return (K)Object2ObjectArrayMap.this.key[this.pos++];
               }
            }

            @Override
            public void remove() {
               if (this.pos == 0) {
                  throw new IllegalStateException();
               } else {
                  int tail = Object2ObjectArrayMap.this.size - this.pos;
                  System.arraycopy(Object2ObjectArrayMap.this.key, this.pos, Object2ObjectArrayMap.this.key, this.pos - 1, tail);
                  System.arraycopy(Object2ObjectArrayMap.this.value, this.pos, Object2ObjectArrayMap.this.value, this.pos - 1, tail);
                  Object2ObjectArrayMap.this.size--;
                  this.pos--;
                  Object2ObjectArrayMap.this.key[Object2ObjectArrayMap.this.size] = null;
                  Object2ObjectArrayMap.this.value[Object2ObjectArrayMap.this.size] = null;
               }
            }

            @Override
            public void forEachRemaining(Consumer<? super K> action) {
               Object[] key = Object2ObjectArrayMap.this.key;
               int max = Object2ObjectArrayMap.this.size;

               while (this.pos < max) {
                  action.accept((K)key[this.pos++]);
               }
            }
         };
      }

      @Override
      public ObjectSpliterator<K> spliterator() {
         return new Object2ObjectArrayMap.KeySet.KeySetSpliterator(0, Object2ObjectArrayMap.this.size);
      }

      @Override
      public void forEach(Consumer<? super K> action) {
         Object[] key = Object2ObjectArrayMap.this.key;
         int i = 0;

         for (int max = Object2ObjectArrayMap.this.size; i < max; i++) {
            action.accept((K)key[i]);
         }
      }

      @Override
      public int size() {
         return Object2ObjectArrayMap.this.size;
      }

      @Override
      public void clear() {
         Object2ObjectArrayMap.this.clear();
      }

      final class KeySetSpliterator extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<K> implements ObjectSpliterator<K> {
         KeySetSpliterator(int pos, int maxPos) {
            super(pos, maxPos);
         }

         @Override
         public int characteristics() {
            return 16465;
         }

         @Override
         protected final K get(int location) {
            return (K)Object2ObjectArrayMap.this.key[location];
         }

         protected final Object2ObjectArrayMap<K, V>.KeySet.KeySetSpliterator makeForSplit(int pos, int maxPos) {
            return KeySet.this.new KeySetSpliterator(pos, maxPos);
         }

         @Override
         public void forEachRemaining(Consumer<? super K> action) {
            Object[] key = Object2ObjectArrayMap.this.key;
            int max = Object2ObjectArrayMap.this.size;

            while (this.pos < max) {
               action.accept((K)key[this.pos++]);
            }
         }
      }
   }

   private final class MapEntry implements Object2ObjectMap.Entry<K, V>, Entry<K, V>, Pair<K, V> {
      int index;

      MapEntry() {
      }

      MapEntry(final int index) {
         this.index = index;
      }

      @Override
      public K getKey() {
         return (K)Object2ObjectArrayMap.this.key[this.index];
      }

      @Override
      public K left() {
         return (K)Object2ObjectArrayMap.this.key[this.index];
      }

      @Override
      public V getValue() {
         return (V)Object2ObjectArrayMap.this.value[this.index];
      }

      @Override
      public V right() {
         return (V)Object2ObjectArrayMap.this.value[this.index];
      }

      @Override
      public V setValue(V v) {
         V oldValue = (V)Object2ObjectArrayMap.this.value[this.index];
         Object2ObjectArrayMap.this.value[this.index] = v;
         return oldValue;
      }

      @Override
      public Pair<K, V> right(V v) {
         Object2ObjectArrayMap.this.value[this.index] = v;
         return this;
      }

      @Override
      public boolean equals(Object o) {
         if (!(o instanceof Entry)) {
            return false;
         } else {
            Entry<K, V> e = (Entry<K, V>)o;
            return Objects.equals(Object2ObjectArrayMap.this.key[this.index], e.getKey())
               && Objects.equals(Object2ObjectArrayMap.this.value[this.index], e.getValue());
         }
      }

      @Override
      public int hashCode() {
         return (Object2ObjectArrayMap.this.key[this.index] == null ? 0 : Object2ObjectArrayMap.this.key[this.index].hashCode())
            ^ (Object2ObjectArrayMap.this.value[this.index] == null ? 0 : Object2ObjectArrayMap.this.value[this.index].hashCode());
      }

      @Override
      public String toString() {
         return Object2ObjectArrayMap.this.key[this.index] + "=>" + Object2ObjectArrayMap.this.value[this.index];
      }
   }

   private final class ValuesCollection extends AbstractObjectCollection<V> {
      private ValuesCollection() {
      }

      @Override
      public boolean contains(Object v) {
         return Object2ObjectArrayMap.this.containsValue(v);
      }

      @Override
      public ObjectIterator<V> iterator() {
         return new ObjectIterator<V>() {
            int pos = 0;

            @Override
            public boolean hasNext() {
               return this.pos < Object2ObjectArrayMap.this.size;
            }

            @Override
            public V next() {
               if (!this.hasNext()) {
                  throw new NoSuchElementException();
               } else {
                  return (V)Object2ObjectArrayMap.this.value[this.pos++];
               }
            }

            @Override
            public void remove() {
               if (this.pos == 0) {
                  throw new IllegalStateException();
               } else {
                  int tail = Object2ObjectArrayMap.this.size - this.pos;
                  System.arraycopy(Object2ObjectArrayMap.this.key, this.pos, Object2ObjectArrayMap.this.key, this.pos - 1, tail);
                  System.arraycopy(Object2ObjectArrayMap.this.value, this.pos, Object2ObjectArrayMap.this.value, this.pos - 1, tail);
                  Object2ObjectArrayMap.this.size--;
                  this.pos--;
                  Object2ObjectArrayMap.this.key[Object2ObjectArrayMap.this.size] = null;
                  Object2ObjectArrayMap.this.value[Object2ObjectArrayMap.this.size] = null;
               }
            }

            @Override
            public void forEachRemaining(Consumer<? super V> action) {
               Object[] value = Object2ObjectArrayMap.this.value;
               int max = Object2ObjectArrayMap.this.size;

               while (this.pos < max) {
                  action.accept((V)value[this.pos++]);
               }
            }
         };
      }

      @Override
      public ObjectSpliterator<V> spliterator() {
         return new Object2ObjectArrayMap.ValuesCollection.ValuesSpliterator(0, Object2ObjectArrayMap.this.size);
      }

      @Override
      public void forEach(Consumer<? super V> action) {
         Object[] value = Object2ObjectArrayMap.this.value;
         int i = 0;

         for (int max = Object2ObjectArrayMap.this.size; i < max; i++) {
            action.accept((V)value[i]);
         }
      }

      @Override
      public int size() {
         return Object2ObjectArrayMap.this.size;
      }

      @Override
      public void clear() {
         Object2ObjectArrayMap.this.clear();
      }

      final class ValuesSpliterator extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<V> implements ObjectSpliterator<V> {
         ValuesSpliterator(int pos, int maxPos) {
            super(pos, maxPos);
         }

         @Override
         public int characteristics() {
            return 16464;
         }

         @Override
         protected final V get(int location) {
            return (V)Object2ObjectArrayMap.this.value[location];
         }

         protected final Object2ObjectArrayMap<K, V>.ValuesCollection.ValuesSpliterator makeForSplit(int pos, int maxPos) {
            return ValuesCollection.this.new ValuesSpliterator(pos, maxPos);
         }

         @Override
         public void forEachRemaining(Consumer<? super V> action) {
            Object[] value = Object2ObjectArrayMap.this.value;
            int max = Object2ObjectArrayMap.this.size;

            while (this.pos < max) {
               action.accept((V)value[this.pos++]);
            }
         }
      }
   }
}
