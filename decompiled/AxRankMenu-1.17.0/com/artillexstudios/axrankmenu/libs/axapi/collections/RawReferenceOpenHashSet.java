package com.artillexstudios.axrankmenu.libs.axapi.collections;

import com.artillexstudios.axrankmenu.libs.axapi.reflection.FieldAccessor;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;

public class RawReferenceOpenHashSet<K> extends ReferenceOpenHashSet<K> {
   private static final FieldAccessor accessor = FieldAccessor.builder()
      .withClass("it.unimi.dsi.fastutil.objects.ReferenceCollections$SynchronizedCollection")
      .withField("collection")
      .build();

   public RawReferenceOpenHashSet() {
   }

   public RawReferenceOpenHashSet(final int capacity) {
      super(capacity);
   }

   public static <E> Object[] rawSet(ReferenceSet<E> set) {
      RawReferenceOpenHashSet<E> rawSet = accessor.getUnchecked(set);
      return rawSet.rawSet();
   }

   public boolean remove(Object k) {
      return super.remove(k);
   }

   public RawReferenceOpenHashSet<K> clone() {
      return (RawReferenceOpenHashSet<K>)super.clone();
   }

   public Object[] rawSet() {
      return this.key;
   }
}
