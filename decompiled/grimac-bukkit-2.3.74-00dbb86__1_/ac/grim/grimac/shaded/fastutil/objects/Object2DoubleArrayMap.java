package ac.grim.grimac.shaded.fastutil.objects;

import ac.grim.grimac.shaded.fastutil.HashCommon;
import ac.grim.grimac.shaded.fastutil.doubles.AbstractDoubleCollection;
import ac.grim.grimac.shaded.fastutil.doubles.DoubleArrays;
import ac.grim.grimac.shaded.fastutil.doubles.DoubleCollection;
import ac.grim.grimac.shaded.fastutil.doubles.DoubleIterator;
import ac.grim.grimac.shaded.fastutil.doubles.DoubleSpliterator;
import ac.grim.grimac.shaded.fastutil.doubles.DoubleSpliterators;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

public class Object2DoubleArrayMap<K> extends AbstractObject2DoubleMap<K> implements Serializable, Cloneable {
   private static final long serialVersionUID = 1L;
   protected transient Object[] key;
   protected transient double[] value;
   protected int size;
   protected transient Object2DoubleMap.FastEntrySet<K> entries;
   protected transient ObjectSet<K> keys;
   protected transient DoubleCollection values;

   public Object2DoubleArrayMap(Object[] key, double[] value) {
      this.key = key;
      this.value = value;
      this.size = key.length;
      if (key.length != value.length) {
         throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
      }
   }

   public Object2DoubleArrayMap() {
      this.key = ObjectArrays.EMPTY_ARRAY;
      this.value = DoubleArrays.EMPTY_ARRAY;
   }

   public Object2DoubleArrayMap(int capacity) {
      this.key = new Object[capacity];
      this.value = new double[capacity];
   }

   public Object2DoubleArrayMap(Object2DoubleMap<K> m) {
      this(m.size());
      int i = 0;

      for (Object2DoubleMap.Entry<K> e : m.object2DoubleEntrySet()) {
         this.key[i] = e.getKey();
         this.value[i] = e.getDoubleValue();
         i++;
      }

      this.size = i;
   }

   public Object2DoubleArrayMap(Map<? extends K, ? extends Double> m) {
      this(m.size());
      int i = 0;

      for (Entry<? extends K, ? extends Double> e : m.entrySet()) {
         this.key[i] = e.getKey();
         this.value[i] = e.getValue();
         i++;
      }

      this.size = i;
   }

   public Object2DoubleArrayMap(Object[] key, double[] value, int size) {
      this.key = key;
      this.value = value;
      this.size = size;
      if (key.length != value.length) {
         throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
      } else if (size > key.length) {
         throw new IllegalArgumentException("The provided size (" + size + ") is larger than or equal to the backing-arrays size (" + key.length + ")");
      }
   }

   public Object2DoubleMap.FastEntrySet<K> object2DoubleEntrySet() {
      if (this.entries == null) {
         this.entries = new Object2DoubleArrayMap.EntrySet();
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
   public double getDouble(Object k) {
      Object[] key = this.key;
      int i = this.size;

      while (i-- != 0) {
         if (Objects.equals(key[i], k)) {
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
      Object[] key = this.key;
      int i = this.size;

      while (i-- != 0) {
         key[i] = null;
      }

      this.size = 0;
   }

   @Override
   public boolean containsKey(Object k) {
      return this.findKey(k) != -1;
   }

   @Override
   public boolean containsValue(double v) {
      double[] value = this.value;
      int i = this.size;

      while (i-- != 0) {
         if (Double.doubleToLongBits(value[i]) == Double.doubleToLongBits(v)) {
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
   public double put(K k, double v) {
      int oldKey = this.findKey(k);
      if (oldKey != -1) {
         double oldValue = this.value[oldKey];
         this.value[oldKey] = v;
         return oldValue;
      } else {
         if (this.size == this.key.length) {
            Object[] newKey = new Object[this.size == 0 ? 2 : this.size * 2];
            double[] newValue = new double[this.size == 0 ? 2 : this.size * 2];

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
   public double removeDouble(Object k) {
      int oldPos = this.findKey(k);
      if (oldPos == -1) {
         return this.defRetValue;
      } else {
         double oldValue = this.value[oldPos];
         int tail = this.size - oldPos - 1;
         System.arraycopy(this.key, oldPos + 1, this.key, oldPos, tail);
         System.arraycopy(this.value, oldPos + 1, this.value, oldPos, tail);
         this.size--;
         this.key[this.size] = null;
         return oldValue;
      }
   }

   @Override
   public ObjectSet<K> keySet() {
      if (this.keys == null) {
         this.keys = new Object2DoubleArrayMap.KeySet();
      }

      return this.keys;
   }

   @Override
   public DoubleCollection values() {
      if (this.values == null) {
         this.values = new Object2DoubleArrayMap.ValuesCollection();
      }

      return this.values;
   }

   public Object2DoubleArrayMap<K> clone() {
      Object2DoubleArrayMap<K> c;
      try {
         c = (Object2DoubleArrayMap<K>)super.clone();
      } catch (CloneNotSupportedException var3) {
         throw new InternalError();
      }

      c.key = (Object[])this.key.clone();
      c.value = (double[])this.value.clone();
      c.entries = null;
      c.keys = null;
      c.values = null;
      return c;
   }

   private void writeObject(ObjectOutputStream s) throws IOException {
      s.defaultWriteObject();
      Object[] key = this.key;
      double[] value = this.value;
      int i = 0;

      for (int max = this.size; i < max; i++) {
         s.writeObject(key[i]);
         s.writeDouble(value[i]);
      }
   }

   private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
      s.defaultReadObject();
      Object[] key = this.key = new Object[this.size];
      double[] value = this.value = new double[this.size];

      for (int i = 0; i < this.size; i++) {
         key[i] = s.readObject();
         value[i] = s.readDouble();
      }
   }

   private final class EntrySet extends AbstractObjectSet<Object2DoubleMap.Entry<K>> implements Object2DoubleMap.FastEntrySet<K> {
      private EntrySet() {
      }

      @Override
      public ObjectIterator<Object2DoubleMap.Entry<K>> iterator() {
         return new ObjectIterator<Object2DoubleMap.Entry<K>>() {
            private Object2DoubleArrayMap<K>.MapEntry entry;
            int curr = -1;
            int next = 0;

            @Override
            public boolean hasNext() {
               return this.next < Object2DoubleArrayMap.this.size;
            }

            public Object2DoubleMap.Entry<K> next() {
               if (!this.hasNext()) {
                  throw new NoSuchElementException();
               } else {
                  return this.entry = Object2DoubleArrayMap.this.new MapEntry(this.curr = this.next++);
               }
            }

            @Override
            public void remove() {
               if (this.curr == -1) {
                  throw new IllegalStateException();
               } else {
                  this.curr = -1;
                  int tail = Object2DoubleArrayMap.this.size-- - this.next--;
                  System.arraycopy(Object2DoubleArrayMap.this.key, this.next + 1, Object2DoubleArrayMap.this.key, this.next, tail);
                  System.arraycopy(Object2DoubleArrayMap.this.value, this.next + 1, Object2DoubleArrayMap.this.value, this.next, tail);
                  this.entry.index = -1;
                  Object2DoubleArrayMap.this.key[Object2DoubleArrayMap.this.size] = null;
               }
            }

            @Override
            public int skip(int n) {
               if (n < 0) {
                  throw new IllegalArgumentException("Argument must be nonnegative: " + n);
               } else {
                  n = Math.min(n, Object2DoubleArrayMap.this.size - this.next);
                  this.next += n;
                  if (n != 0) {
                     this.curr = this.next - 1;
                  }

                  return n;
               }
            }

            @Override
            public void forEachRemaining(Consumer<? super Object2DoubleMap.Entry<K>> action) {
               int max = Object2DoubleArrayMap.this.size;

               while (this.next < max) {
                  this.entry = Object2DoubleArrayMap.this.new MapEntry(this.curr = this.next++);
                  action.accept(this.entry);
               }
            }
         };
      }

      @Override
      public ObjectIterator<Object2DoubleMap.Entry<K>> fastIterator() {
         return new ObjectIterator<Object2DoubleMap.Entry<K>>() {
            private Object2DoubleArrayMap<K>.MapEntry entry = Object2DoubleArrayMap.this.new MapEntry();
            int next = 0;
            int curr = -1;

            @Override
            public boolean hasNext() {
               return this.next < Object2DoubleArrayMap.this.size;
            }

            public Object2DoubleMap.Entry<K> next() {
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
                  int tail = Object2DoubleArrayMap.this.size-- - this.next--;
                  System.arraycopy(Object2DoubleArrayMap.this.key, this.next + 1, Object2DoubleArrayMap.this.key, this.next, tail);
                  System.arraycopy(Object2DoubleArrayMap.this.value, this.next + 1, Object2DoubleArrayMap.this.value, this.next, tail);
                  this.entry.index = -1;
                  Object2DoubleArrayMap.this.key[Object2DoubleArrayMap.this.size] = null;
               }
            }

            @Override
            public int skip(int n) {
               if (n < 0) {
                  throw new IllegalArgumentException("Argument must be nonnegative: " + n);
               } else {
                  n = Math.min(n, Object2DoubleArrayMap.this.size - this.next);
                  this.next += n;
                  if (n != 0) {
                     this.curr = this.next - 1;
                  }

                  return n;
               }
            }

            @Override
            public void forEachRemaining(Consumer<? super Object2DoubleMap.Entry<K>> action) {
               int max = Object2DoubleArrayMap.this.size;

               while (this.next < max) {
                  this.entry.index = this.curr = this.next++;
                  action.accept(this.entry);
               }
            }
         };
      }

      @Override
      public ObjectSpliterator<Object2DoubleMap.Entry<K>> spliterator() {
         return new Object2DoubleArrayMap.EntrySet.EntrySetSpliterator(0, Object2DoubleArrayMap.this.size);
      }

      @Override
      public void forEach(Consumer<? super Object2DoubleMap.Entry<K>> action) {
         int i = 0;

         for (int max = Object2DoubleArrayMap.this.size; i < max; i++) {
            action.accept(Object2DoubleArrayMap.this.new MapEntry(i));
         }
      }

      @Override
      public void fastForEach(Consumer<? super Object2DoubleMap.Entry<K>> action) {
         Object2DoubleArrayMap<K>.MapEntry entry = Object2DoubleArrayMap.this.new MapEntry();
         int i = 0;

         for (int max = Object2DoubleArrayMap.this.size; i < max; i++) {
            entry.index = i;
            action.accept(entry);
         }
      }

      @Override
      public int size() {
         return Object2DoubleArrayMap.this.size;
      }

      @Override
      public boolean contains(Object o) {
         if (!(o instanceof Entry)) {
            return false;
         } else {
            Entry<?, ?> e = (Entry<?, ?>)o;
            if (e.getValue() != null && e.getValue() instanceof Double) {
               K k = (K)e.getKey();
               return Object2DoubleArrayMap.this.containsKey(k)
                  && Double.doubleToLongBits(Object2DoubleArrayMap.this.getDouble(k)) == Double.doubleToLongBits((Double)e.getValue());
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
            if (e.getValue() != null && e.getValue() instanceof Double) {
               K k = (K)e.getKey();
               double v = (Double)e.getValue();
               int oldPos = Object2DoubleArrayMap.this.findKey(k);
               if (oldPos != -1 && Double.doubleToLongBits(v) == Double.doubleToLongBits(Object2DoubleArrayMap.this.value[oldPos])) {
                  int tail = Object2DoubleArrayMap.this.size - oldPos - 1;
                  System.arraycopy(Object2DoubleArrayMap.this.key, oldPos + 1, Object2DoubleArrayMap.this.key, oldPos, tail);
                  System.arraycopy(Object2DoubleArrayMap.this.value, oldPos + 1, Object2DoubleArrayMap.this.value, oldPos, tail);
                  Object2DoubleArrayMap.this.size--;
                  Object2DoubleArrayMap.this.key[Object2DoubleArrayMap.this.size] = null;
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
         extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Object2DoubleMap.Entry<K>>
         implements ObjectSpliterator<Object2DoubleMap.Entry<K>> {
         EntrySetSpliterator(int pos, int maxPos) {
            super(pos, maxPos);
         }

         @Override
         public int characteristics() {
            return 16465;
         }

         protected final Object2DoubleMap.Entry<K> get(int location) {
            return Object2DoubleArrayMap.this.new MapEntry(location);
         }

         protected final Object2DoubleArrayMap<K>.EntrySet.EntrySetSpliterator makeForSplit(int pos, int maxPos) {
            return EntrySet.this.new EntrySetSpliterator(pos, maxPos);
         }
      }
   }

   private final class KeySet extends AbstractObjectSet<K> {
      private KeySet() {
      }

      @Override
      public boolean contains(Object k) {
         return Object2DoubleArrayMap.this.findKey(k) != -1;
      }

      @Override
      public boolean remove(Object k) {
         int oldPos = Object2DoubleArrayMap.this.findKey(k);
         if (oldPos == -1) {
            return false;
         } else {
            int tail = Object2DoubleArrayMap.this.size - oldPos - 1;
            System.arraycopy(Object2DoubleArrayMap.this.key, oldPos + 1, Object2DoubleArrayMap.this.key, oldPos, tail);
            System.arraycopy(Object2DoubleArrayMap.this.value, oldPos + 1, Object2DoubleArrayMap.this.value, oldPos, tail);
            Object2DoubleArrayMap.this.size--;
            Object2DoubleArrayMap.this.key[Object2DoubleArrayMap.this.size] = null;
            return true;
         }
      }

      @Override
      public ObjectIterator<K> iterator() {
         return new ObjectIterator<K>() {
            int pos = 0;

            @Override
            public boolean hasNext() {
               return this.pos < Object2DoubleArrayMap.this.size;
            }

            @Override
            public K next() {
               if (!this.hasNext()) {
                  throw new NoSuchElementException();
               } else {
                  return (K)Object2DoubleArrayMap.this.key[this.pos++];
               }
            }

            @Override
            public void remove() {
               if (this.pos == 0) {
                  throw new IllegalStateException();
               } else {
                  int tail = Object2DoubleArrayMap.this.size - this.pos;
                  System.arraycopy(Object2DoubleArrayMap.this.key, this.pos, Object2DoubleArrayMap.this.key, this.pos - 1, tail);
                  System.arraycopy(Object2DoubleArrayMap.this.value, this.pos, Object2DoubleArrayMap.this.value, this.pos - 1, tail);
                  Object2DoubleArrayMap.this.size--;
                  this.pos--;
                  Object2DoubleArrayMap.this.key[Object2DoubleArrayMap.this.size] = null;
               }
            }

            @Override
            public void forEachRemaining(Consumer<? super K> action) {
               Object[] key = Object2DoubleArrayMap.this.key;
               int max = Object2DoubleArrayMap.this.size;

               while (this.pos < max) {
                  action.accept((K)key[this.pos++]);
               }
            }
         };
      }

      @Override
      public ObjectSpliterator<K> spliterator() {
         return new Object2DoubleArrayMap.KeySet.KeySetSpliterator(0, Object2DoubleArrayMap.this.size);
      }

      @Override
      public void forEach(Consumer<? super K> action) {
         Object[] key = Object2DoubleArrayMap.this.key;
         int i = 0;

         for (int max = Object2DoubleArrayMap.this.size; i < max; i++) {
            action.accept((K)key[i]);
         }
      }

      @Override
      public int size() {
         return Object2DoubleArrayMap.this.size;
      }

      @Override
      public void clear() {
         Object2DoubleArrayMap.this.clear();
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
            return (K)Object2DoubleArrayMap.this.key[location];
         }

         protected final Object2DoubleArrayMap<K>.KeySet.KeySetSpliterator makeForSplit(int pos, int maxPos) {
            return KeySet.this.new KeySetSpliterator(pos, maxPos);
         }

         @Override
         public void forEachRemaining(Consumer<? super K> action) {
            Object[] key = Object2DoubleArrayMap.this.key;
            int max = Object2DoubleArrayMap.this.size;

            while (this.pos < max) {
               action.accept((K)key[this.pos++]);
            }
         }
      }
   }

   private final class MapEntry implements Object2DoubleMap.Entry<K>, Entry<K, Double>, ObjectDoublePair<K> {
      int index;

      MapEntry() {
      }

      MapEntry(final int index) {
         this.index = index;
      }

      @Override
      public K getKey() {
         return (K)Object2DoubleArrayMap.this.key[this.index];
      }

      @Override
      public K left() {
         return (K)Object2DoubleArrayMap.this.key[this.index];
      }

      @Override
      public double getDoubleValue() {
         return Object2DoubleArrayMap.this.value[this.index];
      }

      @Override
      public double rightDouble() {
         return Object2DoubleArrayMap.this.value[this.index];
      }

      @Override
      public double setValue(double v) {
         double oldValue = Object2DoubleArrayMap.this.value[this.index];
         Object2DoubleArrayMap.this.value[this.index] = v;
         return oldValue;
      }

      @Override
      public ObjectDoublePair<K> right(double v) {
         Object2DoubleArrayMap.this.value[this.index] = v;
         return this;
      }

      @Deprecated
      @Override
      public Double getValue() {
         return Object2DoubleArrayMap.this.value[this.index];
      }

      @Deprecated
      @Override
      public Double setValue(Double v) {
         return this.setValue(v.doubleValue());
      }

      @Override
      public boolean equals(Object o) {
         if (!(o instanceof Entry)) {
            return false;
         } else {
            Entry<K, Double> e = (Entry<K, Double>)o;
            return Objects.equals(Object2DoubleArrayMap.this.key[this.index], e.getKey())
               && Double.doubleToLongBits(Object2DoubleArrayMap.this.value[this.index]) == Double.doubleToLongBits(e.getValue());
         }
      }

      @Override
      public int hashCode() {
         return (Object2DoubleArrayMap.this.key[this.index] == null ? 0 : Object2DoubleArrayMap.this.key[this.index].hashCode())
            ^ HashCommon.double2int(Object2DoubleArrayMap.this.value[this.index]);
      }

      @Override
      public String toString() {
         return Object2DoubleArrayMap.this.key[this.index] + "=>" + Object2DoubleArrayMap.this.value[this.index];
      }
   }

   private final class ValuesCollection extends AbstractDoubleCollection {
      private ValuesCollection() {
      }

      @Override
      public boolean contains(double v) {
         return Object2DoubleArrayMap.this.containsValue(v);
      }

      @Override
      public DoubleIterator iterator() {
         return new DoubleIterator() {
            int pos = 0;

            @Override
            public boolean hasNext() {
               return this.pos < Object2DoubleArrayMap.this.size;
            }

            @Override
            public double nextDouble() {
               if (!this.hasNext()) {
                  throw new NoSuchElementException();
               } else {
                  return Object2DoubleArrayMap.this.value[this.pos++];
               }
            }

            @Override
            public void remove() {
               if (this.pos == 0) {
                  throw new IllegalStateException();
               } else {
                  int tail = Object2DoubleArrayMap.this.size - this.pos;
                  System.arraycopy(Object2DoubleArrayMap.this.key, this.pos, Object2DoubleArrayMap.this.key, this.pos - 1, tail);
                  System.arraycopy(Object2DoubleArrayMap.this.value, this.pos, Object2DoubleArrayMap.this.value, this.pos - 1, tail);
                  Object2DoubleArrayMap.this.size--;
                  this.pos--;
                  Object2DoubleArrayMap.this.key[Object2DoubleArrayMap.this.size] = null;
               }
            }

            @Override
            public void forEachRemaining(DoubleConsumer action) {
               double[] value = Object2DoubleArrayMap.this.value;
               int max = Object2DoubleArrayMap.this.size;

               while (this.pos < max) {
                  action.accept(value[this.pos++]);
               }
            }
         };
      }

      @Override
      public DoubleSpliterator spliterator() {
         return new Object2DoubleArrayMap.ValuesCollection.ValuesSpliterator(0, Object2DoubleArrayMap.this.size);
      }

      @Override
      public void forEach(DoubleConsumer action) {
         double[] value = Object2DoubleArrayMap.this.value;
         int i = 0;

         for (int max = Object2DoubleArrayMap.this.size; i < max; i++) {
            action.accept(value[i]);
         }
      }

      @Override
      public int size() {
         return Object2DoubleArrayMap.this.size;
      }

      @Override
      public void clear() {
         Object2DoubleArrayMap.this.clear();
      }

      final class ValuesSpliterator extends DoubleSpliterators.EarlyBindingSizeIndexBasedSpliterator implements DoubleSpliterator {
         ValuesSpliterator(int pos, int maxPos) {
            super(pos, maxPos);
         }

         @Override
         public int characteristics() {
            return 16720;
         }

         @Override
         protected final double get(int location) {
            return Object2DoubleArrayMap.this.value[location];
         }

         protected final Object2DoubleArrayMap<K>.ValuesCollection.ValuesSpliterator makeForSplit(int pos, int maxPos) {
            return ValuesCollection.this.new ValuesSpliterator(pos, maxPos);
         }

         @Override
         public void forEachRemaining(DoubleConsumer action) {
            double[] value = Object2DoubleArrayMap.this.value;
            int max = Object2DoubleArrayMap.this.size;

            while (this.pos < max) {
               action.accept(value[this.pos++]);
            }
         }
      }
   }
}
