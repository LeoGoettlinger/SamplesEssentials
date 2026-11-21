package com.magmaguy.elitemobs.easyminecraftgoals.visuals.terrain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

final class LayeredTerrainIndex<K, V> {
   private final Map<UUID, LayeredTerrainIndex.Layer<K, V>> layers = new LinkedHashMap<>();
   private long nextPriority;

   List<LayeredTerrainIndex.VisibilityChange<K, V>> replace(UUID impactId, Map<K, V> values) {
      Objects.requireNonNull(impactId, "impactId");
      Objects.requireNonNull(values, "values");
      LinkedHashMap<K, V> replacement = new LinkedHashMap<>();
      values.forEach((key, value) -> replacement.put(Objects.requireNonNull((K)key, "terrain coordinate"), Objects.requireNonNull((V)value, "terrain visual")));
      if (replacement.isEmpty()) {
         return this.remove(impactId);
      } else {
         LayeredTerrainIndex.Layer<K, V> previous = this.layers.get(impactId);
         Set<K> affected = new LinkedHashSet<>();
         if (previous != null) {
            affected.addAll(previous.values().keySet());
         }

         affected.addAll(replacement.keySet());
         Map<K, Optional<V>> before = this.snapshot(affected);
         long priority = previous == null ? ++this.nextPriority : previous.priority();
         this.layers.put(impactId, new LayeredTerrainIndex.Layer<>(priority, Map.copyOf(replacement)));
         return this.changes(affected, before);
      }
   }

   List<LayeredTerrainIndex.VisibilityChange<K, V>> remove(UUID impactId) {
      Objects.requireNonNull(impactId, "impactId");
      LayeredTerrainIndex.Layer<K, V> existing = this.layers.get(impactId);
      if (existing == null) {
         return List.of();
      } else {
         Set<K> affected = new LinkedHashSet<>(existing.values().keySet());
         Map<K, Optional<V>> before = this.snapshot(affected);
         this.layers.remove(impactId);
         return this.changes(affected, before);
      }
   }

   Optional<V> visible(K key) {
      Objects.requireNonNull(key, "key");
      LayeredTerrainIndex.Layer<K, V> winner = null;

      for (LayeredTerrainIndex.Layer<K, V> layer : this.layers.values()) {
         if (layer.values().containsKey(key) && (winner == null || layer.priority() > winner.priority())) {
            winner = layer;
         }
      }

      return winner == null ? Optional.empty() : Optional.of(winner.values().get(key));
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

   private List<LayeredTerrainIndex.VisibilityChange<K, V>> changes(Set<K> affected, Map<K, Optional<V>> before) {
      List<LayeredTerrainIndex.VisibilityChange<K, V>> changes = new ArrayList<>();

      for (K key : affected) {
         Optional<V> after = this.visible(key);
         Optional<V> prior = before.getOrDefault(key, Optional.empty());
         if (!prior.equals(after)) {
            changes.add(new LayeredTerrainIndex.VisibilityChange<>(key, prior, after));
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
