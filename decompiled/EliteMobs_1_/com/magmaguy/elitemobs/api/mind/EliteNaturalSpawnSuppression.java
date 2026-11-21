package com.magmaguy.elitemobs.api.mind;

import java.util.Objects;

public record EliteNaturalSpawnSuppression(String reason) implements EliteNaturalSpawnClaim {
   public EliteNaturalSpawnSuppression(String reason) {
      Objects.requireNonNull(reason, "reason");
      if (reason.isBlank()) {
         throw new IllegalArgumentException("Natural spawn suppression needs a reason");
      } else {
         this.reason = reason;
      }
   }
}
