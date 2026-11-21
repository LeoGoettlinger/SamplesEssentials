package com.magmaguy.elitemobs.api.mind;

import java.util.Objects;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public record EliteNaturalSpawnContext(LivingEntity source, SpawnReason spawnReason, int suggestedLevel) {
   public EliteNaturalSpawnContext(LivingEntity source, SpawnReason spawnReason, int suggestedLevel) {
      Objects.requireNonNull(source, "source");
      Objects.requireNonNull(spawnReason, "spawnReason");
      if (suggestedLevel < 0) {
         throw new IllegalArgumentException("suggestedLevel cannot be negative");
      } else {
         this.source = source;
         this.spawnReason = spawnReason;
         this.suggestedLevel = suggestedLevel;
      }
   }
}
