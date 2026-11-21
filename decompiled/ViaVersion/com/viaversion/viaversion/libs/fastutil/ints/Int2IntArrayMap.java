package com.viaversion.viaversion.libs.fastutil.ints;

import com.viaversion.viaversion.libs.fastutil.objects.AbstractObjectSet;
import com.viaversion.viaversion.libs.fastutil.objects.ObjectIterator;
import com.viaversion.viaversion.libs.fastutil.objects.ObjectSpliterator;
import com.viaversion.viaversion.libs.fastutil.objects.ObjectSpliterators;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Map.Entry;
import java.util.function.Consumer;

public class Int2IntArrayMap extends AbstractInt2IntMap implements Serializable, Cloneable {
   private static final long serialVersionUID = 1L;
   protected transient int[] key;
   protected transient int[] value;
   protected int size;
   protected transient Int2IntMap.FastEntrySet entries;
   protected transient IntSet keys;
   protected transient IntCollection values;

   public Int2IntArrayMap(int[] key, int[] value) {
      this.key = key;
      this.value = value;
      this.size = key.length;
      if (key.length != value.length) {
         throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
      }
   }

   public Int2IntArrayMap() {
      this.key = IntArrays.EMPTY_ARRAY;
      this.value = IntArrays.EMPTY_ARRAY;
   }

   public Int2IntArrayMap(int capacity) {
      this.key = new int[capacity];
      this.value = new int[capacity];
   }

   public Int2IntArrayMap(Int2IntMap m) {
      this(m.size());
      int i = 0;

      for (Int2IntMap.Entry e : m.int2IntEntrySet()) {
         this.key[i] = e.getIntKey();
         this.value[i] = e.getIntValue();
         i++;
      }

      this.size = i;
   }

   public Int2IntArrayMap(Map<? extends Integer, ? extends Integer> m) {
      this(m.size());
      int i = 0;

      for (Entry<? extends Integer, ? extends Integer> e : m.entrySet()) {
         this.key[i] = e.getKey();
         this.value[i] = e.getValue();
         i++;
      }

      this.size = i;
   }

   public Int2IntArrayMap(int[] key, int[] value, int size) {
      this.key = key;
      this.value = value;
      this.size = size;
      if (key.length != value.length) {
         throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
      } else if (size > key.length) {
         throw new IllegalArgumentException("The provided size (" + size + ") is larger than or equal to the backing-arrays size (" + key.length + ")");
      }
   }

   public Int2IntMap.FastEntrySet int2IntEntrySet() {
      if (this.entries == null) {
         this.entries = new Int2IntArrayMap.EntrySet();
      }

      return this.entries;
   }

   private int findKey(int k) {
      int[] key = this.key;
      int i = this.size;

      while (i-- != 0) {
         if (key[i] == k) {
            return i;
         }
      }

      return -1;
   }

   @Override
   public int get(int k) {
      int[] key = this.key;
      int i = this.size;

      while (i-- != 0) {
         if (key[i] == k) {
            return this.value[i];
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
      this.size = 0;
   }

   @Override
   public boolean containsKey(int k) {
      return this.findKey(k) != -1;
   }

   @Override
   public boolean containsValue(int v) {
      int[] value = this.value;
      int i = this.size;

      while (i-- != 0) {
         if (value[i] == v) {
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
   public int put(int k, int v) {
      int oldKey = this.findKey(k);
      if (oldKey != -1) {
         int oldValue = this.value[oldKey];
         this.value[oldKey] = v;
         return oldValue;
      } else {
         if (this.size == this.key.length) {
            int[] newKey = new int[this.size == 0 ? 2 : this.size * 2];
            int[] newValue = new int[this.size == 0 ? 2 : this.size * 2];

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
   public int remove(int k) {
      int oldPos = this.findKey(k);
      if (oldPos == -1) {
         return this.defRetValue;
      } else {
         int oldValue = this.value[oldPos];
         int tail = this.size - oldPos - 1;
         System.arraycopy(this.key, oldPos + 1, this.key, oldPos, tail);
         System.arraycopy(this.value, oldPos + 1, this.value, oldPos, tail);
         this.size--;
         return oldValue;
      }
   }

   @Override
   public IntSet keySet() {
      if (this.keys == null) {
         this.keys = new Int2IntArrayMap.KeySet();
      }

      return this.keys;
   }

   @Override
   public IntCollection values() {
      if (this.values == null) {
         this.values = new Int2IntArrayMap.ValuesCollection();
      }

      return this.values;
   }

   public Int2IntArrayMap clone() {
      Int2IntArrayMap c;
      try {
         c = (Int2IntArrayMap)super.clone();
      } catch (CloneNotSupportedException var3) {
         throw new InternalError();
      }

      c.key = (int[])this.key.clone();
      c.value = (int[])this.value.clone();
      c.entries = null;
      c.keys = null;
      c.values = null;
      return c;
   }

   private void writeObject(ObjectOutputStream s) throws IOException {
      s.defaultWriteObject();
      int[] key = this.key;
      int[] value = this.value;
      int i = 0;

      for (int max = this.size; i < max; i++) {
         s.writeInt(key[i]);
         s.writeInt(value[i]);
      }
   }

   private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
      s.defaultReadObject();
      int[] key = this.key = new int[this.size];
      int[] value = this.value = new int[this.size];

      for (int i = 0; i < this.size; i++) {
         key[i] = s.readInt();
         value[i] = s.readInt();
      }
   }

   private final class EntrySet extends AbstractObjectSet<Int2IntMap.Entry> implements Int2IntMap.FastEntrySet {
      private EntrySet() {
      }

      @Override
      public ObjectIterator<Int2IntMap.Entry> iterator() {
         return new ObjectIterator<Int2IntMap.Entry>() {
            private Int2IntArrayMap.MapEntry entry;
            int curr = -1;
            int next = 0;

            @Override
            public boolean hasNext() {
               return this.next < Int2IntArrayMap.this.size;
            }

            public Int2IntMap.Entry next() {
               if (!this.hasNext()) {
                  throw new NoSuchElementException();
               } else {
                  return this.entry = Int2IntArrayMap.this.new MapEntry(this.curr = this.next++);
               }
            }

            @Override
            public void remove() {
               if (this.curr == -1) {
                  throw new IllegalStateException();
               } else {
                  this.curr = -1;
                  int tail = Int2IntArrayMap.this.size-- - this.next--;
                  System.arraycopy(Int2IntArrayMap.this.key, this.next + 1, Int2IntArrayMap.this.key, this.next, tail);
                  System.arraycopy(Int2IntArrayMap.this.value, this.next + 1, Int2IntArrayMap.this.value, this.next, tail);
                  this.entry.index = -1;
               }
            }

            @Override
            public int skip(int n) {
               if (n < 0) {
                  throw new IllegalArgumentException("Argument must be nonnegative: " + n);
               } else {
                  n = Math.min(n, Int2IntArrayMap.this.size - this.next);
                  this.next += n;
                  if (n != 0) {
                     this.curr = this.next - 1;
                  }

                  return n;
               }
            }

            @Override
            public void forEachRemaining(Consumer<? super Int2IntMap.Entry> action) {
               int max = Int2IntArrayMap.this.size;

               while (this.next < max) {
                  this.entry = Int2IntArrayMap.this.new MapEntry(this.curr = this.next++);
                  action.accept(this.entry);
               }
            }
         };
      }

      @Override
      public ObjectIterator<Int2IntMap.Entry> fastIterator() {
         return new ObjectIterator<Int2IntMap.Entry>() {
            private Int2IntArrayMap.MapEntry entry = Int2IntArrayMap.this.new MapEntry();
            int next = 0;
            int curr = -1;

            @Override
            public boolean hasNext() {
               return this.next < Int2IntArrayMap.this.size;
            }

            public Int2IntMap.Entry next() {
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
                  int tail = Int2IntArrayMap.this.size-- - this.next--;
                  System.arraycopy(Int2IntArrayMap.this.key, this.next + 1, Int2IntArrayMap.this.key, this.next, tail);
                  System.arraycopy(Int2IntArrayMap.this.value, this.next + 1, Int2IntArrayMap.this.value, this.next, tail);
                  this.entry.index = -1;
               }
            }

            @Override
            public int skip(int n) {
               if (n < 0) {
                  throw new IllegalArgumentException("Argument must be nonnegative: " + n);
               } else {
                  n = Math.min(n, Int2IntArrayMap.this.size - this.next);
                  this.next += n;
                  if (n != 0) {
                     this.curr = this.next - 1;
                  }

                  return n;
               }
            }

            @Override
            public void forEachRemaining(Consumer<? super Int2IntMap.Entry> action) {
               int max = Int2IntArrayMap.this.size;

               while (this.next < max) {
                  this.entry.index = this.curr = this.next++;
                  action.accept(this.entry);
               }
            }
         };
      }

      @Override
      public ObjectSpliterator<Int2IntMap.Entry> spliterator() {
         return new Int2IntArrayMap.EntrySet.EntrySetSpliterator(0, Int2IntArrayMap.this.size);
      }

      @Override
      public void forEach(Consumer<? super Int2IntMap.Entry> action) {
         int i = 0;

         for (int max = Int2IntArrayMap.this.size; i < max; i++) {
            action.accept(Int2IntArrayMap.this.new MapEntry(i));
         }
      }

      @Override
      public void fastForEach(Consumer<? super Int2IntMap.Entry> action) {
         Int2IntArrayMap.MapEntry entry = Int2IntArrayMap.this.new MapEntry();
         int i = 0;

         for (int max = Int2IntArrayMap.this.size; i < max; i++) {
            entry.index = i;
            action.accept(entry);
         }
      }

      @Override
      public int size() {
         return Int2IntArrayMap.this.size;
      }

      @Override
      public boolean contains(Object o) {
         if (!(o instanceof Entry)) {
            return false;
         } else {
            Entry<?, ?> e = (Entry<?, ?>)o;
            if (e.getKey() == null || !(e.getKey() instanceof Integer)) {
               return false;
            } else if (e.getValue() != null && e.getValue() instanceof Integer) {
               int k = (Integer)e.getKey();
               return Int2IntArrayMap.this.containsKey(k) && Int2IntArrayMap.this.get(k) == (Integer)e.getValue();
            } else {
               return false;
            }
         }
      }

      @Override
      public boolean remove(Object o) {
         if (!(o instanceof Entry)) {
            return false;
         } else {
            Entry<?, ?> e = (Entry<?, ?>)o;
            if (e.getKey() == null || !(e.getKey() instanceof Integer)) {
               return false;
            } else if (e.getValue() != null && e.getValue() instanceof Integer) {
               int k = (Integer)e.getKey();
               int v = (Integer)e.getValue();
               int oldPos = Int2IntArrayMap.this.findKey(k);
               if (oldPos != -1 && v == Int2IntArrayMap.this.value[oldPos]) {
                  int tail = Int2IntArrayMap.this.size - oldPos - 1;
                  System.arraycopy(Int2IntArrayMap.this.key, oldPos + 1, Int2IntArrayMap.this.key, oldPos, tail);
                  System.arraycopy(Int2IntArrayMap.this.value, oldPos + 1, Int2IntArrayMap.this.value, oldPos, tail);
                  Int2IntArrayMap.this.size--;
                  return true;
               } else {
                  return false;
               }
            } else {
               return false;
            }
         }
      }

      final class EntrySetSpliterator
         extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Int2IntMap.Entry>
         implements ObjectSpliterator<Int2IntMap.Entry> {
         EntrySetSpliterator(int pos, int maxPos) {
            super(pos, maxPos);
         }

         @Override
         public int characteristics() {
            return 16465;
         }

         protected final Int2IntMap.Entry get(int location) {
            return Int2IntArrayMap.this.new MapEntry(location);
         }

         protected final Int2IntArrayMap.EntrySet.EntrySetSpliterator makeForSplit(int pos, int maxPos) {
            return EntrySet.this.new EntrySetSpliterator(pos, maxPos);
         }
      }
   }

   private final class KeySet extends AbstractIntSet {
      private KeySet() {
      }

      @Override
      public boolean contains(int k) {
         return Int2IntArrayMap.this.findKey(k) != -1;
      }

      @Override
      public boolean remove(int k) {
         int oldPos = Int2IntArrayMap.this.findKey(k);
         if (oldPos == -1) {
            return false;
         } else {
            int tail = Int2IntArrayMap.this.size - oldPos - 1;
            System.arraycopy(Int2IntArrayMap.this.key, oldPos + 1, Int2IntArrayMap.this.key, oldPos, tail);
            System.arraycopy(Int2IntArrayMap.this.value, oldPos + 1, Int2IntArrayMap.this.value, oldPos, tail);
            Int2IntArrayMap.this.size--;
            return true;
         }
      }

      @Override
      public IntIterator iterator() {
         return new IntIterator() {
            int pos = 0;

            @Override
            public boolean hasNext() {
               return this.pos < Int2IntArrayMap.this.size;
            }

            @Override
            public int nextInt() {
               if (!this.hasNext()) {
                  throw new NoSuchElementException();
               } else {
                  return Int2IntArrayMap.this.key[this.pos++];
               }
            }

            @Override
            public void remove() {
               if (this.pos == 0) {
                  throw new IllegalStateException();
               } else {
                  int tail = Int2IntArrayMap.this.size - this.pos;
                  System.arraycopy(Int2IntArrayMap.this.key, this.pos, Int2IntArrayMap.this.key, this.pos - 1, tail);
                  System.arraycopy(Int2IntArrayMap.this.value, this.pos, Int2IntArrayMap.this.value, this.pos - 1, tail);
                  Int2IntArrayMap.this.size--;
                  this.pos--;
               }
            }

            @Override
            public void forEachRemaining(java.util.function.IntConsumer action) {
               int[] key = Int2IntArrayMap.this.key;
               int max = Int2IntArrayMap.this.size;

               while (this.pos < max) {
                  action.accept(key[this.pos++]);
               }
            }
         };
      }

      @Override
      public IntSpliterator spliterator() {
         return new Int2IntArrayMap.KeySet.KeySetSpliterator(0, Int2IntArrayMap.this.size);
      }

      @Override
      public void forEach(java.util.function.IntConsumer action) {
         int[] key = Int2IntArrayMap.this.key;
         int i = 0;

         for (int max = Int2IntArrayMap.this.size; i < max; i++) {
            action.accept(key[i]);
         }
      }

      @Override
      public int size() {
         return Int2IntArrayMap.this.size;
      }

      @Override
      public void clear() {
         Int2IntArrayMap.this.clear();
      }

      final class KeySetSpliterator extends IntSpliterators.EarlyBindingSizeIndexBasedSpliterator implements IntSpliterator {
         KeySetSpliterator(int pos, int maxPos) {
            super(pos, maxPos);
         }

         @Override
         public int characteristics() {
            return 16721;
         }

         @Override
         protected final int get(int location) {
            return Int2IntArrayMap.this.key[location];
         }

         protected final Int2IntArrayMap.KeySet.KeySetSpliterator makeForSplit(int pos, int maxPos) {
            return KeySet.this.new KeySetSpliterator(pos, maxPos);
         }

         @Override
         public void forEachRemaining(java.util.function.IntConsumer action) {
            int[] key = Int2IntArrayMap.this.key;
            int max = Int2IntArrayMap.this.size;

            while (this.pos < max) {
               action.accept(key[this.pos++]);
            }
         }
      }
   }

   private final class MapEntry implements Int2IntMap.Entry, Entry<Integer, Integer>, IntIntPair {
      int index;

      MapEntry() {
      }

      MapEntry(final int index) {
         this.index = index;
      }

      @Override
      public int getIntKey() {
         return Int2IntArrayMap.this.key[this.index];
      }

      @Override
      public int leftInt() {
         return Int2IntArrayMap.this.key[this.index];
      }

      @Override
      public int getIntValue() {
         return Int2IntArrayMap.this.value[this.index];
      }

      @Override
      public int rightInt() {
         return Int2IntArrayMap.this.value[this.index];
      }

      @Override
      public int setValue(int v) {
         int oldValue = Int2IntArrayMap.this.value[this.index];
         Int2IntArrayMap.this.value[this.index] = v;
         return oldValue;
      }

      @Override
      public IntIntPair right(int v) {
         Int2IntArrayMap.this.value[this.index] = v;
         return this;
      }

      @Deprecated
      @Override
      public Integer getKey() {
         return Int2IntArrayMap.this.key[this.index];
      }

      @Deprecated
      @Override
      public Integer getValue() {
         return Int2IntArrayMap.this.value[this.index];
      }

      @Deprecated
      @Override
      public Integer setValue(Integer v) {
         return this.setValue(v.intValue());
      }

      @Override
      public boolean equals(Object o) {
         if (!(o instanceof Entry)) {
            return false;
         } else {
            Entry<Integer, Integer> e = (Entry<Integer, Integer>)o;
            return Int2IntArrayMap.this.key[this.index] == e.getKey() && Int2IntArrayMap.this.value[this.index] == e.getValue();
         }
      }

      @Override
      public int hashCode() {
         return Int2IntArrayMap.this.key[this.index] ^ Int2IntArrayMap.this.value[this.index];
      }

      @Override
      public String toString() {
         return Int2IntArrayMap.this.key[this.index] + "=>" + Int2IntArrayMap.this.value[this.index];
      }
   }

   private final class ValuesCollection extends AbstractIntCollection {
      private ValuesCollection() {
      }

      @Override
      public boolean contains(int v) {
         return Int2IntArrayMap.this.containsValue(v);
      }

      @Override
      public IntIterator iterator() {
         return new IntIterator() {
            int pos = 0;

            @Override
            public boolean hasNext() {
               return this.pos < Int2IntArrayMap.this.size;
            }

            @Override
            public int nextInt() {
               if (!this.hasNext()) {
                  throw new NoSuchElementException();
               } else {
                  return Int2IntArrayMap.this.value[this.pos++];
               }
            }

            @Override
            public void remove() {
               if (this.pos == 0) {
                  throw new IllegalStateException();
               } else {
                  int tail = Int2IntArrayMap.this.size - this.pos;
                  System.arraycopy(Int2IntArrayMap.this.key, this.pos, Int2IntArrayMap.this.key, this.pos - 1, tail);
                  System.arraycopy(Int2IntArrayMap.this.value, this.pos, Int2IntArrayMap.this.value, this.pos - 1, tail);
                  Int2IntArrayMap.this.size--;
                  this.pos--;
               }
            }

            @Override
            public void forEachRemaining(java.util.function.IntConsumer action) {
               int[] value = Int2IntArrayMap.this.value;
               int max = Int2IntArrayMap.this.size;

               while (this.pos < max) {
                  action.accept(value[this.pos++]);
               }
            }
         };
      }

      @Override
      public IntSpliterator spliterator() {
         return new Int2IntArrayMap.ValuesCollection.ValuesSpliterator(0, Int2IntArrayMap.this.size);
      }

      @Override
      public void forEach(java.util.function.IntConsumer action) {
         int[] value = Int2IntArrayMap.this.value;
         int i = 0;

         for (int max = Int2IntArrayMap.this.size; i < max; i++) {
            action.accept(value[i]);
         }
      }

      @Override
      public int size() {
         return Int2IntArrayMap.this.size;
      }

      @Override
      public void clear() {
         Int2IntArrayMap.this.clear();
      }

      final class ValuesSpliterator extends IntSpliterators.EarlyBindingSizeIndexBasedSpliterator implements IntSpliterator {
         ValuesSpliterator(int pos, int maxPos) {
            super(pos, maxPos);
         }

         @Override
         public int characteristics() {
            return 16720;
         }

         @Override
         protected final int get(int location) {
            return Int2IntArrayMap.this.value[location];
         }

         protected final Int2IntArrayMap.ValuesCollection.ValuesSpliterator makeForSplit(int pos, int maxPos) {
            return ValuesCollection.this.new ValuesSpliterator(pos, maxPos);
         }

         @Override
         public void forEachRemaining(java.util.function.IntConsumer action) {
            int[] value = Int2IntArrayMap.this.value;
            int max = Int2IntArrayMap.this.size;

            while (this.pos < max) {
               action.accept(value[this.pos++]);
            }
         }
      }
   }
}
