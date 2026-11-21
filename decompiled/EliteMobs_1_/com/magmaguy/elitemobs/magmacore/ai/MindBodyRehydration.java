package com.magmaguy.elitemobs.magmacore.ai;

import java.util.Objects;
import java.util.UUID;

public record MindBodyRehydration(
   MobBody body, MindBodyProfile profile, UUID logicalOwner, String programIdentifier, long programRevision, MindPersistentState persistentState
) {
   public MindBodyRehydration(
      MobBody body, MindBodyProfile profile, UUID logicalOwner, String programIdentifier, long programRevision, MindPersistentState persistentState
   ) {
      Objects.requireNonNull(body, "body");
      Objects.requireNonNull(profile, "profile");
      Objects.requireNonNull(logicalOwner, "logicalOwner");
      Objects.requireNonNull(programIdentifier, "programIdentifier");
      Objects.requireNonNull(persistentState, "persistentState");
      if (programIdentifier.isBlank()) {
         throw new IllegalArgumentException("programIdentifier cannot be blank");
      } else if (programRevision < 1L) {
         throw new IllegalArgumentException("programRevision must be positive");
      } else if (!body.profile().equals(profile)) {
         throw new IllegalArgumentException("Rehydrated body profile does not match its marker");
      } else {
         this.body = body;
         this.profile = profile;
         this.logicalOwner = logicalOwner;
         this.programIdentifier = programIdentifier;
         this.programRevision = programRevision;
         this.persistentState = persistentState;
      }
   }
}
