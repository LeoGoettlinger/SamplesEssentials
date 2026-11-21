package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import java.io.Serializable;
import java.util.Spliterator;
import java.util.function.Consumer;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
@GwtCompatible(
   emulated = true
)
final class ImmutableMapKeySet<K, V> extends IndexedImmutableSet<K> {
   private final ImmutableMap<K, V> map;

   ImmutableMapKeySet(ImmutableMap<K, V> map) {
      this.map = map;
   }

   @Override
   public int size() {
      return this.map.size();
   }

   @Override
   public UnmodifiableIterator<K> iterator() {
      return this.map.keyIterator();
   }

   @Override
   public Spliterator<K> spliterator() {
      return this.map.keySpliterator();
   }

   @Override
   public boolean contains(@CheckForNull Object object) {
      return this.map.containsKey(object);
   }

   @Override
   K get(int index) {
      return this.map.entrySet().asList().get(index).getKey();
   }

   @Override
   public void forEach(Consumer<? super K> action) {
      Preconditions.checkNotNull(action);
      this.map.forEach((k, v) -> action.accept(k));
   }

   @Override
   boolean isPartialView() {
      return true;
   }

   @GwtIncompatible
   @J2ktIncompatible
   private static class KeySetSerializedForm<K> implements Serializable {
      final ImmutableMap<K, ?> map;
      private static final long serialVersionUID = 0L;

      KeySetSerializedForm(ImmutableMap<K, ?> map) {
         this.map = map;
      }

      Object readResolve() {
         return this.map.keySet();
      }
   }
}
