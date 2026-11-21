package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

final class SourceSuppressionLeaseRegistry<K, S> implements AutoCloseable {
   private final Map<K, SourceSuppressionLeaseRegistry.Entry<S>> entries = new LinkedHashMap<>();

   boolean apply(K target, S source, long expiresAtTick, Supplier<? extends AutoCloseable> suppressionFactory) {
      Objects.requireNonNull(target, "target");
      Objects.requireNonNull(source, "source");
      Objects.requireNonNull(suppressionFactory, "suppressionFactory");
      SourceSuppressionLeaseRegistry.Entry<S> current = this.entries.get(target);
      if (current != null) {
         current.expiryBySource.merge(source, expiresAtTick, Math::max);
         return true;
      } else {
         AutoCloseable suppression;
         try {
            suppression = suppressionFactory.get();
         } catch (RuntimeException var9) {
            return false;
         }

         if (suppression == null) {
            return false;
         } else {
            SourceSuppressionLeaseRegistry.Entry<S> created = new SourceSuppressionLeaseRegistry.Entry<>(suppression);
            created.expiryBySource.put(source, expiresAtTick);
            this.entries.put(target, created);
            return true;
         }
      }
   }

   boolean extend(K target, S source, long additionalTicks, long currentTick) {
      if (additionalTicks <= 0L) {
         return false;
      } else {
         SourceSuppressionLeaseRegistry.Entry<S> entry = this.entries.get(target);
         if (entry == null) {
            return false;
         } else {
            Long current = entry.expiryBySource.get(source);
            if (current != null && current > currentTick) {
               long base = Math.max(currentTick, current);
               entry.expiryBySource.put(source, saturatedAdd(base, additionalTicks));
               return true;
            } else {
               return false;
            }
         }
      }
   }

   void maintain(long currentTick, Predicate<K> targetValid) {
      Objects.requireNonNull(targetValid, "targetValid");

      for (K target : new ArrayList<>(this.entries.keySet())) {
         SourceSuppressionLeaseRegistry.Entry<S> entry = this.entries.get(target);
         if (entry != null) {
            entry.expiryBySource.entrySet().removeIf(item -> item.getValue() <= currentTick);
            if (entry.expiryBySource.isEmpty() || !targetValid.test(target)) {
               this.remove(target, entry);
            }
         }
      }
   }

   Set<K> activeTargets() {
      return Set.copyOf(new LinkedHashSet<>(this.entries.keySet()));
   }

   int remainingTicks(K target, S source, long currentTick) {
      SourceSuppressionLeaseRegistry.Entry<S> entry = this.entries.get(target);
      long expiry = entry == null ? currentTick : entry.expiryBySource.getOrDefault(source, currentTick);
      return (int)Math.min(2147483647L, Math.max(0L, expiry - currentTick));
   }

   boolean ownedBy(K target, S source) {
      SourceSuppressionLeaseRegistry.Entry<S> entry = this.entries.get(target);
      return entry != null && entry.expiryBySource.containsKey(source);
   }

   void clearSource(S source) {
      for (K target : new ArrayList<>(this.entries.keySet())) {
         SourceSuppressionLeaseRegistry.Entry<S> entry = this.entries.get(target);
         entry.expiryBySource.remove(source);
         if (entry.expiryBySource.isEmpty()) {
            this.remove(target, entry);
         }
      }
   }

   void remove(K target) {
      SourceSuppressionLeaseRegistry.Entry<S> entry = this.entries.get(target);
      if (entry != null) {
         this.remove(target, entry);
      }
   }

   @Override
   public void close() {
      for (SourceSuppressionLeaseRegistry.Entry<S> entry : new ArrayList<>(this.entries.values())) {
         closeQuietly(entry.suppression);
      }

      this.entries.clear();
   }

   private void remove(K target, SourceSuppressionLeaseRegistry.Entry<S> expected) {
      if (this.entries.remove(target, expected)) {
         closeQuietly(expected.suppression);
      }
   }

   private static void closeQuietly(AutoCloseable lease) {
      try {
         lease.close();
      } catch (Exception var2) {
      }
   }

   private static long saturatedAdd(long value, long increment) {
      return value > Long.MAX_VALUE - increment ? Long.MAX_VALUE : value + increment;
   }

   private static final class Entry<S> {
      private final Map<S, Long> expiryBySource = (Map<S, Long>)(new LinkedHashMap<>());
      private final AutoCloseable suppression;

      private Entry(AutoCloseable suppression) {
         this.suppression = suppression;
      }
   }
}
