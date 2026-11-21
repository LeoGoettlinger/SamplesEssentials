package com.magmaguy.elitemobs.api.power;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.bukkit.NamespacedKey;

public record EliteLuaPowerProgram(NamespacedKey key, long revision, int executionPriority, Set<String> hooks, EliteLuaPowerType type, String effect) {
   public EliteLuaPowerProgram(NamespacedKey key, long revision, int executionPriority, Set<String> hooks, EliteLuaPowerType type, String effect) {
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(hooks, "hooks");
      Objects.requireNonNull(type, "type");
      if (revision < 1L) {
         throw new IllegalArgumentException("revision must be positive");
      } else {
         hooks = Collections.unmodifiableSet(new LinkedHashSet<>(hooks));
         this.key = key;
         this.revision = revision;
         this.executionPriority = executionPriority;
         this.hooks = hooks;
         this.type = type;
         this.effect = effect;
      }
   }
}
