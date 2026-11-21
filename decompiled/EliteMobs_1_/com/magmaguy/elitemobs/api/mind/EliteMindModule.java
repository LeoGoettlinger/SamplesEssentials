package com.magmaguy.elitemobs.api.mind;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import org.bukkit.NamespacedKey;

public record EliteMindModule(NamespacedKey key, long revision, List<NamespacedKey> dependencies) {
   public EliteMindModule(NamespacedKey key, long revision, List<NamespacedKey> dependencies) {
      Objects.requireNonNull(key, "key");
      if (revision < 1L) {
         throw new IllegalArgumentException("revision must be positive");
      } else {
         dependencies = List.copyOf(Objects.requireNonNull(dependencies, "dependencies"));
         if (new HashSet<>(dependencies).size() != dependencies.size()) {
            throw new IllegalArgumentException("Mind module dependencies cannot repeat");
         } else {
            for (NamespacedKey dependency : dependencies) {
               Objects.requireNonNull(dependency, "dependency");
            }

            this.key = key;
            this.revision = revision;
            this.dependencies = dependencies;
         }
      }
   }
}
