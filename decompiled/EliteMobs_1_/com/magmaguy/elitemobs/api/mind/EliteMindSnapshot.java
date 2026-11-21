package com.magmaguy.elitemobs.api.mind;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.bukkit.NamespacedKey;

public record EliteMindSnapshot(
   UUID eliteId,
   NamespacedKey programKey,
   long programRevision,
   long generation,
   Set<String> activeBehaviors,
   boolean bodyAttached,
   boolean paused,
   List<EliteMindModule> resolvedModules,
   String compositionFingerprint
) {
   public EliteMindSnapshot(UUID eliteId, NamespacedKey programKey, long programRevision, long generation, Set<String> activeBehaviors, boolean bodyAttached) {
      this(eliteId, programKey, programRevision, generation, activeBehaviors, bodyAttached, false, List.of(), "");
   }

   public EliteMindSnapshot(
      UUID eliteId,
      NamespacedKey programKey,
      long programRevision,
      long generation,
      Set<String> activeBehaviors,
      boolean bodyAttached,
      boolean paused,
      List<EliteMindModule> resolvedModules,
      String compositionFingerprint
   ) {
      Objects.requireNonNull(eliteId, "eliteId");
      Objects.requireNonNull(programKey, "programKey");
      Objects.requireNonNull(activeBehaviors, "activeBehaviors");
      if (programRevision < 1L) {
         throw new IllegalArgumentException("programRevision must be positive");
      } else if (generation < 0L) {
         throw new IllegalArgumentException("generation cannot be negative");
      } else {
         activeBehaviors = Collections.unmodifiableSet(new LinkedHashSet<>(activeBehaviors));
         resolvedModules = List.copyOf(Objects.requireNonNull(resolvedModules, "resolvedModules"));
         compositionFingerprint = Objects.requireNonNull(compositionFingerprint, "compositionFingerprint");
         this.eliteId = eliteId;
         this.programKey = programKey;
         this.programRevision = programRevision;
         this.generation = generation;
         this.activeBehaviors = activeBehaviors;
         this.bodyAttached = bodyAttached;
         this.paused = paused;
         this.resolvedModules = resolvedModules;
         this.compositionFingerprint = compositionFingerprint;
      }
   }
}
