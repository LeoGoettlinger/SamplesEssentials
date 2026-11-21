package com.magmaguy.elitemobs.advancedcombat.constructs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

final class PacketConstructLayerIndex<K, V> {
   private final Map<UUID, PacketConstructLayerIndex.Layer<K, V>> layers = new LinkedHashMap<>();
   private long nextPriority;

   List<PacketConstructLayerIndex.VisibilityChange<K, V>> replace(UUID constructId, Map<K, V> values) {
      Objects.requireNonNull(constructId, "constructId");
      Objects.requireNonNull(values, "values");
      PacketConstructLayerIndex.Layer<K, V> previous = this.layers.get(constructId);
      if (previous != null && previous.values().equals(values)) {
         return List.of();
      } else {
         LinkedHashMap<K, V> replacement = new LinkedHashMap<>();
         values.forEach((key, value) -> replacement.put(Objects.requireNonNull((K)key, "construct key"), Objects.requireNonNull((V)value, "construct value")));
         if (replacement.isEmpty()) {
            return this.remove(constructId);
         } else {
            Set<K> affected = new LinkedHashSet<>();
            if (previous != null) {
               affected.addAll(previous.values().keySet());
            }

            affected.addAll(replacement.keySet());
            Map<K, Optional<V>> before = this.snapshot(affected);
            long priority = previous == null ? ++this.nextPriority : previous.priority();
            this.layers.put(constructId, new PacketConstructLayerIndex.Layer<>(priority, Map.copyOf(replacement)));
            return this.changes(affected, before);
         }
      }
   }

   List<PacketConstructLayerIndex.VisibilityChange<K, V>> remove(UUID constructId) {
      Objects.requireNonNull(constructId, "constructId");
      PacketConstructLayerIndex.Layer<K, V> existing = this.layers.get(constructId);
      if (existing == null) {
         return List.of();
      } else {
         Set<K> affected = new LinkedHashSet<>(existing.values().keySet());
         Map<K, Optional<V>> before = this.snapshot(affected);
         this.layers.remove(constructId);
         return this.changes(affected, before);
      }
   }

   Optional<V> visible(K key) {
      Objects.requireNonNull(key, "key");
      PacketConstructLayerIndex.Layer<K, V> highest = null;

      for (PacketConstructLayerIndex.Layer<K, V> layer : this.layers.values()) {
         if (layer.values().containsKey(key) && (highest == null || layer.priority() > highest.priority())) {
            highest = layer;
         }
      }

      return highest == null ? Optional.empty() : Optional.of(highest.values().get(key));
   }

   boolean isEmpty() {
      return this.layers.isEmpty();
   }

   private Map<K, Optional<V>> snapshot(Set<K> keys) {
      Map<K, Optional<V>> snapshot = new LinkedHashMap<>();

      for (K key : keys) {
         snapshot.put(key, this.visible(key));
      }

      return snapshot;
   }

   private List<PacketConstructLayerIndex.VisibilityChange<K, V>> changes(Set<K> affected, Map<K, Optional<V>> before) {
      List<PacketConstructLayerIndex.VisibilityChange<K, V>> changes = new ArrayList<>();

      for (K key : affected) {
         Optional<V> after = this.visible(key);
         Optional<V> prior = before.getOrDefault(key, Optional.empty());
         if (!prior.equals(after)) {
            changes.add(new PacketConstructLayerIndex.VisibilityChange<>(key, prior, after));
         }
      }

      return List.copyOf(changes);
   }

   private record Layer<K, V>(long priority, Map<K, V> values) {
   }

   record VisibilityChange<K, V>(K key, Optional<V> before, Optional<V> after) {
      VisibilityChange(K key, Optional<V> before, Optional<V> after) {
         Objects.requireNonNull(key, "key");
         Objects.requireNonNull(before, "before");
         Objects.requireNonNull(after, "after");
         this.key = key;
         this.before = before;
         this.after = after;
      }
   }
}
