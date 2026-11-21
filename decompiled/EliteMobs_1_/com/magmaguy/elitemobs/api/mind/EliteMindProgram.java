package com.magmaguy.elitemobs.api.mind;

import java.util.List;
import java.util.Objects;
import org.bukkit.NamespacedKey;

public record EliteMindProgram(NamespacedKey key, long revision, List<EliteMindModule> resolvedModules, String compositionFingerprint) {
   public EliteMindProgram(NamespacedKey key, long revision) {
      this(key, revision, List.of(), "");
   }

   public EliteMindProgram(NamespacedKey key, long revision, List<EliteMindModule> resolvedModules, String compositionFingerprint) {
      Objects.requireNonNull(key, "key");
      if (revision < 1L) {
         throw new IllegalArgumentException("revision must be positive");
      } else {
         resolvedModules = List.copyOf(Objects.requireNonNull(resolvedModules, "resolvedModules"));
         compositionFingerprint = Objects.requireNonNull(compositionFingerprint, "compositionFingerprint");
         this.key = key;
         this.revision = revision;
         this.resolvedModules = resolvedModules;
         this.compositionFingerprint = compositionFingerprint;
      }
   }
}
