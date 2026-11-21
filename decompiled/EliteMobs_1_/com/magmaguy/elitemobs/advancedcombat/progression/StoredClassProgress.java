package com.magmaguy.elitemobs.advancedcombat.progression;

import java.util.Objects;
import java.util.UUID;

public record StoredClassProgress(UUID playerId, String formId, long xp, int catalogVersion, boolean challengeCompleted) {
   public StoredClassProgress(UUID playerId, String formId, long xp, int catalogVersion) {
      this(playerId, formId, xp, catalogVersion, false);
   }

   public StoredClassProgress(UUID playerId, String formId, long xp, int catalogVersion, boolean challengeCompleted) {
      Objects.requireNonNull(playerId, "playerId");
      this.playerId = playerId;
      this.formId = formId;
      this.xp = xp;
      this.catalogVersion = catalogVersion;
      this.challengeCompleted = challengeCompleted;
   }

   public static StoredClassProgress zero(UUID playerId, String formId, int catalogVersion) {
      Objects.requireNonNull(formId, "formId");
      if (formId.isBlank()) {
         throw new IllegalArgumentException("formId must not be blank");
      } else if (catalogVersion < 0) {
         throw new IllegalArgumentException("catalogVersion must not be negative");
      } else {
         return new StoredClassProgress(playerId, formId, 0L, catalogVersion);
      }
   }
}
