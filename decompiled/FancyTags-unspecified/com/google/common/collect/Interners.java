package com.google.common.collect;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Equivalence;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
@J2ktIncompatible
@GwtIncompatible
public final class Interners {
   private Interners() {
   }

   public static Interners.InternerBuilder newBuilder() {
      return new Interners.InternerBuilder();
   }

   public static <E> Interner<E> newStrongInterner() {
      return newBuilder().strong().build();
   }

   @GwtIncompatible("java.lang.ref.WeakReference")
   public static <E> Interner<E> newWeakInterner() {
      return newBuilder().weak().build();
   }

   public static <E> Function<E, E> asFunction(Interner<E> interner) {
      return new Interners.InternerFunction<>(Preconditions.checkNotNull(interner));
   }

   public static class InternerBuilder {
      private final MapMaker mapMaker = new MapMaker();
      private boolean strong = true;

      private InternerBuilder() {
      }

      public Interners.InternerBuilder strong() {
         this.strong = true;
         return this;
      }

      @GwtIncompatible("java.lang.ref.WeakReference")
      public Interners.InternerBuilder weak() {
         this.strong = false;
         return this;
      }

      public Interners.InternerBuilder concurrencyLevel(int concurrencyLevel) {
         this.mapMaker.concurrencyLevel(concurrencyLevel);
         return this;
      }

      public <E> Interner<E> build() {
         if (!this.strong) {
            this.mapMaker.weakKeys();
         }

         return new Interners.InternerImpl<>(this.mapMaker);
      }
   }

   private static class InternerFunction<E> implements Function<E, E> {
      private final Interner<E> interner;

      public InternerFunction(Interner<E> interner) {
         this.interner = interner;
      }

      @Override
      public E apply(E input) {
         return this.interner.intern(input);
      }

      @Override
      public int hashCode() {
         return this.interner.hashCode();
      }

      @Override
      public boolean equals(@CheckForNull Object other) {
         if (other instanceof Interners.InternerFunction) {
            Interners.InternerFunction<?> that = (Interners.InternerFunction<?>)other;
            return this.interner.equals(that.interner);
         } else {
            return false;
         }
      }
   }

   @VisibleForTesting
   static final class InternerImpl<E> implements Interner<E> {
      @VisibleForTesting
      final MapMakerInternalMap<E, MapMaker.Dummy, ?, ?> map;

      private InternerImpl(MapMaker mapMaker) {
         this.map = MapMakerInternalMap.createWithDummyValues(mapMaker.keyEquivalence(Equivalence.equals()));
      }

      @Override
      public E intern(E sample) {
         MapMaker.Dummy sneaky;
         do {
            MapMakerInternalMap.InternalEntry entry = this.map.getEntry(sample);
            if (entry != null) {
               sneaky = (MapMaker.Dummy)entry.getKey();
               if (sneaky != null) {
                  return (E)sneaky;
               }
            }

            sneaky = this.map.putIfAbsent(sample, MapMaker.Dummy.VALUE);
         } while (sneaky != null);

         return sample;
      }
   }
}
