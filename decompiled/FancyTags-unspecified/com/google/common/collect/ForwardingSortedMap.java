package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
@GwtCompatible
public abstract class ForwardingSortedMap<K, V> extends ForwardingMap<K, V> implements SortedMap<K, V> {
   protected ForwardingSortedMap() {
   }

   protected abstract SortedMap<K, V> delegate();

   @CheckForNull
   @Override
   public Comparator<? super K> comparator() {
      return this.delegate().comparator();
   }

   @ParametricNullness
   @Override
   public K firstKey() {
      return this.delegate().firstKey();
   }

   @Override
   public SortedMap<K, V> headMap(@ParametricNullness K toKey) {
      return this.delegate().headMap(toKey);
   }

   @ParametricNullness
   @Override
   public K lastKey() {
      return this.delegate().lastKey();
   }

   @Override
   public SortedMap<K, V> subMap(@ParametricNullness K fromKey, @ParametricNullness K toKey) {
      return this.delegate().subMap(fromKey, toKey);
   }

   @Override
   public SortedMap<K, V> tailMap(@ParametricNullness K fromKey) {
      return this.delegate().tailMap(fromKey);
   }

   static int unsafeCompare(@CheckForNull Comparator<?> comparator, @CheckForNull Object o1, @CheckForNull Object o2) {
      return comparator == null ? ((Comparable)o1).compareTo(o2) : ((Comparator<Object>)comparator).compare(o1, o2);
   }

   @Override
   protected boolean standardContainsKey(@CheckForNull Object key) {
      try {
         Object ceilingKey = this.tailMap((K)key).firstKey();
         return unsafeCompare(this.comparator(), ceilingKey, key) == 0;
      } catch (NoSuchElementException | NullPointerException | ClassCastException var4) {
         return false;
      }
   }

   protected SortedMap<K, V> standardSubMap(K fromKey, K toKey) {
      Preconditions.checkArgument(unsafeCompare(this.comparator(), fromKey, toKey) <= 0, "fromKey must be <= toKey");
      return this.tailMap(fromKey).headMap(toKey);
   }

   protected class StandardKeySet extends Maps.SortedKeySet<K, V> {
      public StandardKeySet() {
         super(ForwardingSortedMap.this);
      }
   }
}
