package com.viaversion.viaversion.libs.fastutil.longs;

import com.viaversion.viaversion.libs.fastutil.longs.Long2ObjectFunctions.SynchronizedFunction;
import com.viaversion.viaversion.libs.fastutil.longs.Long2ObjectFunctions.UnmodifiableFunction;
import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.LongFunction;

public final class Long2ObjectFunctions {
   public static final Long2ObjectFunctions.EmptyFunction EMPTY_FUNCTION = new Long2ObjectFunctions.EmptyFunction();

   private Long2ObjectFunctions() {
   }

   public static <V> Long2ObjectFunction<V> singleton(long key, V value) {
      return new Long2ObjectFunctions.Singleton<>(key, value);
   }

   public static <V> Long2ObjectFunction<V> singleton(Long key, V value) {
      return new Long2ObjectFunctions.Singleton<>(key, value);
   }

   public static <V> Long2ObjectFunction<V> synchronize(Long2ObjectFunction<V> f) {
      return new SynchronizedFunction(f);
   }

   public static <V> Long2ObjectFunction<V> synchronize(Long2ObjectFunction<V> f, Object sync) {
      return new SynchronizedFunction(f, sync);
   }

   public static <V> Long2ObjectFunction<V> unmodifiable(Long2ObjectFunction<? extends V> f) {
      return new UnmodifiableFunction(f);
   }

   public static <V> Long2ObjectFunction<V> primitive(Function<? super Long, ? extends V> f) {
      Objects.requireNonNull(f);
      if (f instanceof Long2ObjectFunction) {
         return (Long2ObjectFunction<V>)f;
      } else {
         return (Long2ObjectFunction<V>)(f instanceof LongFunction ? ((LongFunction)f)::apply : new Long2ObjectFunctions.PrimitiveFunction<>(f));
      }
   }

   public static class EmptyFunction<V> extends AbstractLong2ObjectFunction<V> implements Serializable, Cloneable {
      private static final long serialVersionUID = -7046029254386353129L;

      protected EmptyFunction() {
      }

      @Override
      public V get(long k) {
         return null;
      }

      @Override
      public V getOrDefault(long k, V defaultValue) {
         return defaultValue;
      }

      @Override
      public boolean containsKey(long k) {
         return false;
      }

      @Override
      public V defaultReturnValue() {
         return null;
      }

      @Override
      public void defaultReturnValue(V defRetValue) {
         throw new UnsupportedOperationException();
      }

      @Override
      public int size() {
         return 0;
      }

      @Override
      public void clear() {
      }

      @Override
      public Object clone() {
         return Long2ObjectFunctions.EMPTY_FUNCTION;
      }

      @Override
      public int hashCode() {
         return 0;
      }

      @Override
      public boolean equals(Object o) {
         return !(o instanceof com.viaversion.viaversion.libs.fastutil.Function) ? false : ((com.viaversion.viaversion.libs.fastutil.Function)o).size() == 0;
      }

      @Override
      public String toString() {
         return "{}";
      }

      private Object readResolve() {
         return Long2ObjectFunctions.EMPTY_FUNCTION;
      }
   }

   public static class PrimitiveFunction<V> implements Long2ObjectFunction<V> {
      protected final Function<? super Long, ? extends V> function;

      protected PrimitiveFunction(Function<? super Long, ? extends V> function) {
         this.function = function;
      }

      @Override
      public boolean containsKey(long key) {
         return this.function.apply(key) != null;
      }

      @Deprecated
      @Override
      public boolean containsKey(Object key) {
         return key == null ? false : this.function.apply((Long)key) != null;
      }

      @Override
      public V get(long key) {
         V v = (V)this.function.apply(key);
         return v == null ? null : v;
      }

      @Override
      public V getOrDefault(long key, V defaultValue) {
         V v = (V)this.function.apply(key);
         return v == null ? defaultValue : v;
      }

      @Deprecated
      @Override
      public V get(Object key) {
         return (V)(key == null ? null : this.function.apply((Long)key));
      }

      @Deprecated
      @Override
      public V getOrDefault(Object key, V defaultValue) {
         if (key == null) {
            return defaultValue;
         } else {
            V v;
            return (v = (V)this.function.apply((Long)key)) == null ? defaultValue : v;
         }
      }

      @Deprecated
      @Override
      public V put(Long key, V value) {
         throw new UnsupportedOperationException();
      }
   }

   public static class Singleton<V> extends AbstractLong2ObjectFunction<V> implements Serializable, Cloneable {
      private static final long serialVersionUID = -7046029254386353129L;
      protected final long key;
      protected final V value;

      protected Singleton(long key, V value) {
         this.key = key;
         this.value = value;
      }

      @Override
      public boolean containsKey(long k) {
         return this.key == k;
      }

      @Override
      public V get(long k) {
         return this.key == k ? this.value : this.defRetValue;
      }

      @Override
      public V getOrDefault(long k, V defaultValue) {
         return this.key == k ? this.value : defaultValue;
      }

      @Override
      public int size() {
         return 1;
      }

      @Override
      public Object clone() {
         return this;
      }
   }
}
