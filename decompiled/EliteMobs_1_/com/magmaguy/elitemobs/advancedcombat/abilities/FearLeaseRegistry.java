package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

final class FearLeaseRegistry<K, S> implements AutoCloseable {
   private final Map<K, FearLeaseRegistry.Entry<S>> entries = new LinkedHashMap<>();

   boolean apply(K target, UUID source, S sourceState, long expiresAtTick, Supplier<Optional<FearLeaseRegistry.Redirectable<S>>> factory) {
      Objects.requireNonNull(target, "target");
      Objects.requireNonNull(source, "source");
      Objects.requireNonNull(sourceState, "sourceState");
      Objects.requireNonNull(factory, "factory");
      FearLeaseRegistry.Entry<S> current = this.entries.get(target);
      if (current != null) {
         if (isActive(current.handle) && retarget(current.handle, sourceState)) {
            current.source = source;
            current.expiresAtTick = Math.max(current.expiresAtTick, expiresAtTick);
            return true;
         }

         this.remove(target, current);
      }

      Optional<FearLeaseRegistry.Redirectable<S>> opened;
      try {
         opened = factory.get();
      } catch (RuntimeException var10) {
         return false;
      }

      if (opened == null) {
         return false;
      } else if (opened.isEmpty()) {
         return false;
      } else {
         FearLeaseRegistry.Redirectable<S> handle = opened.get();
         if (isActive(handle) && retarget(handle, sourceState)) {
            this.entries.put(target, new FearLeaseRegistry.Entry<>(source, expiresAtTick, handle));
            return true;
         } else {
            closeQuietly(handle);
            return false;
         }
      }
   }

   void maintain(long currentTick, Predicate<K> targetValid, Function<UUID, Optional<S>> sourceResolver) {
      Objects.requireNonNull(targetValid, "targetValid");
      Objects.requireNonNull(sourceResolver, "sourceResolver");

      for (K target : new ArrayList<>(this.entries.keySet())) {
         FearLeaseRegistry.Entry<S> entry = this.entries.get(target);
         if (entry != null) {
            if (currentTick < entry.expiresAtTick && targetValid.test(target)) {
               Optional<S> sourceState = sourceResolver.apply(entry.source);
               if (sourceState.isEmpty() || !isActive(entry.handle) || !retarget(entry.handle, sourceState.get())) {
                  this.remove(target, entry);
               }
            } else {
               this.remove(target, entry);
            }
         }
      }
   }

   void clearSource(UUID source) {
      for (K target : new ArrayList<>(this.entries.keySet())) {
         FearLeaseRegistry.Entry<S> entry = this.entries.get(target);
         if (entry.source.equals(source)) {
            this.remove(target, entry);
         }
      }
   }

   void remove(K target) {
      FearLeaseRegistry.Entry<S> entry = this.entries.get(target);
      if (entry != null) {
         this.remove(target, entry);
      }
   }

   int size() {
      return this.entries.size();
   }

   int remainingTicks(K target, UUID source, long currentTick) {
      FearLeaseRegistry.Entry<S> entry = this.entries.get(target);
      return entry != null && entry.source.equals(source) ? (int)Math.min(2147483647L, Math.max(0L, entry.expiresAtTick - currentTick)) : 0;
   }

   boolean ownedBy(K target, UUID source) {
      FearLeaseRegistry.Entry<S> entry = this.entries.get(target);
      return entry != null && entry.source.equals(source) && isActive(entry.handle);
   }

   @Override
   public void close() {
      for (FearLeaseRegistry.Entry<S> entry : new ArrayList<>(this.entries.values())) {
         closeQuietly(entry.handle);
      }

      this.entries.clear();
   }

   private void remove(K target, FearLeaseRegistry.Entry<S> expected) {
      if (this.entries.remove(target, expected)) {
         closeQuietly(expected.handle);
      }
   }

   private static <S> boolean isActive(FearLeaseRegistry.Redirectable<S> handle) {
      try {
         return handle.isActive();
      } catch (RuntimeException var2) {
         return false;
      }
   }

   private static <S> boolean retarget(FearLeaseRegistry.Redirectable<S> handle, S sourceState) {
      try {
         return handle.retarget(sourceState);
      } catch (RuntimeException var3) {
         return false;
      }
   }

   private static void closeQuietly(FearLeaseRegistry.Redirectable<?> handle) {
      try {
         handle.close();
      } catch (RuntimeException var2) {
      }
   }

   private static final class Entry<S> {
      private UUID source;
      private long expiresAtTick;
      private final FearLeaseRegistry.Redirectable<S> handle;

      private Entry(UUID source, long expiresAtTick, FearLeaseRegistry.Redirectable<S> handle) {
         this.source = source;
         this.expiresAtTick = expiresAtTick;
         this.handle = handle;
      }
   }

   interface Redirectable<S> extends AutoCloseable {
      boolean isActive();

      boolean retarget(S var1);

      @Override
      void close();
   }
}
