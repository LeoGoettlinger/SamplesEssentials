package com.google.common.reflect;

import com.google.common.base.Preconditions;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ForwardingMapEntry;
import com.google.common.collect.ForwardingSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotCall;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
public final class MutableTypeToInstanceMap<B> extends ForwardingMap<TypeToken<? extends B>, B> implements TypeToInstanceMap<B> {
   private final Map<TypeToken<? extends B>, B> backingMap = Maps.newHashMap();

   @CheckForNull
   @Override
   public <T extends B> T getInstance(Class<T> type) {
      return this.trustedGet(TypeToken.of(type));
   }

   @CheckForNull
   @Override
   public <T extends B> T getInstance(TypeToken<T> type) {
      return this.trustedGet(type.rejectTypeVariables());
   }

   @CheckForNull
   @CanIgnoreReturnValue
   @Override
   public <T extends B> T putInstance(Class<T> type, @ParametricNullness T value) {
      return this.trustedPut(TypeToken.of(type), value);
   }

   @CheckForNull
   @CanIgnoreReturnValue
   @Override
   public <T extends B> T putInstance(TypeToken<T> type, @ParametricNullness T value) {
      return this.trustedPut(type.rejectTypeVariables(), value);
   }

   @Deprecated
   @CheckForNull
   @CanIgnoreReturnValue
   @DoNotCall("Always throws UnsupportedOperationException")
   public B put(TypeToken<? extends B> key, @ParametricNullness B value) {
      throw new UnsupportedOperationException("Please use putInstance() instead.");
   }

   @Deprecated
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public void putAll(Map<? extends TypeToken<? extends B>, ? extends B> map) {
      throw new UnsupportedOperationException("Please use putInstance() instead.");
   }

   @Override
   public Set<Entry<TypeToken<? extends B>, B>> entrySet() {
      return MutableTypeToInstanceMap.UnmodifiableEntry.transformEntries(super.entrySet());
   }

   @Override
   protected Map<TypeToken<? extends B>, B> delegate() {
      return this.backingMap;
   }

   @CheckForNull
   private <T extends B> T trustedPut(TypeToken<T> type, @ParametricNullness T value) {
      return (T)this.backingMap.put(type, (B)value);
   }

   @CheckForNull
   private <T extends B> T trustedGet(TypeToken<T> type) {
      return (T)this.backingMap.get(type);
   }

   private static final class UnmodifiableEntry<K, V> extends ForwardingMapEntry<K, V> {
      private final Entry<K, V> delegate;

      static <K, V> Set<Entry<K, V>> transformEntries(Set<Entry<K, V>> entries) {
         return new ForwardingSet<Entry<K, V>>() {
            @Override
            protected Set<Entry<K, V>> delegate() {
               return entries;
            }

            @Override
            public Iterator<Entry<K, V>> iterator() {
               return MutableTypeToInstanceMap.UnmodifiableEntry.transformEntries(super.iterator());
            }

            @Override
            public Object[] toArray() {
               return this.standardToArray();
            }

            @Override
            public <T> T[] toArray(T[] array) {
               return (T[])this.standardToArray(array);
            }
         };
      }

      private static <K, V> Iterator<Entry<K, V>> transformEntries(Iterator<Entry<K, V>> entries) {
         return Iterators.transform(entries, MutableTypeToInstanceMap.UnmodifiableEntry::new);
      }

      private UnmodifiableEntry(Entry<K, V> delegate) {
         this.delegate = Preconditions.checkNotNull(delegate);
      }

      @Override
      protected Entry<K, V> delegate() {
         return this.delegate;
      }

      @ParametricNullness
      @Override
      public V setValue(@ParametricNullness V value) {
         throw new UnsupportedOperationException();
      }
   }
}
