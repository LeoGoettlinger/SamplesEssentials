package com.magmaguy.elitemobs.api.power;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import org.bukkit.NamespacedKey;

public record EliteLuaPowerActivity(
   NamespacedKey key,
   long revision,
   Map<String, Long> successfulEventHooks,
   Map<String, Long> failedEventHooks,
   long acceptedMindActions,
   long deferredMindActions,
   long rejectedMindActions,
   boolean runtimeActive
) {
   public EliteLuaPowerActivity(
      NamespacedKey key,
      long revision,
      Map<String, Long> successfulEventHooks,
      Map<String, Long> failedEventHooks,
      long acceptedMindActions,
      long deferredMindActions,
      long rejectedMindActions,
      boolean runtimeActive
   ) {
      Objects.requireNonNull(key, "key");
      if (revision < 1L) {
         throw new IllegalArgumentException("revision must be positive");
      } else {
         successfulEventHooks = immutableCounters(successfulEventHooks, "successfulEventHooks");
         failedEventHooks = immutableCounters(failedEventHooks, "failedEventHooks");
         requireNonNegative(acceptedMindActions, "acceptedMindActions");
         requireNonNegative(deferredMindActions, "deferredMindActions");
         requireNonNegative(rejectedMindActions, "rejectedMindActions");
         this.key = key;
         this.revision = revision;
         this.successfulEventHooks = successfulEventHooks;
         this.failedEventHooks = failedEventHooks;
         this.acceptedMindActions = acceptedMindActions;
         this.deferredMindActions = deferredMindActions;
         this.rejectedMindActions = rejectedMindActions;
         this.runtimeActive = runtimeActive;
      }
   }

   public boolean activated() {
      return !this.successfulEventHooks.isEmpty() || this.acceptedMindActions > 0L || this.deferredMindActions > 0L;
   }

   private static Map<String, Long> immutableCounters(Map<String, Long> values, String label) {
      Objects.requireNonNull(values, label);
      LinkedHashMap<String, Long> copy = new LinkedHashMap<>();

      for (Entry<String, Long> entry : values.entrySet()) {
         String hook = Objects.requireNonNull(entry.getKey(), label + " hook");
         if (hook.isBlank()) {
            throw new IllegalArgumentException(label + " contains a blank hook");
         }

         long count = Objects.requireNonNull(entry.getValue(), label + " count");
         requireNonNegative(count, label + "[" + hook + "]");
         if (count > 0L) {
            copy.put(hook, count);
         }
      }

      return Collections.unmodifiableMap(copy);
   }

   private static void requireNonNegative(long value, String label) {
      if (value < 0L) {
         throw new IllegalArgumentException(label + " cannot be negative");
      }
   }
}
