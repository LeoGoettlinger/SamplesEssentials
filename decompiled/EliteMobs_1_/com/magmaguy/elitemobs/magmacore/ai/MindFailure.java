package com.magmaguy.elitemobs.magmacore.ai;

import java.util.Objects;
import java.util.UUID;

public record MindFailure(UUID entityId, String programIdentifier, String callbackIdentifier, MindFailure.Kind kind, Throwable cause) {
   public MindFailure(UUID entityId, String programIdentifier, String callbackIdentifier, MindFailure.Kind kind, Throwable cause) {
      Objects.requireNonNull(entityId, "entityId");
      Objects.requireNonNull(programIdentifier, "programIdentifier");
      Objects.requireNonNull(callbackIdentifier, "callbackIdentifier");
      Objects.requireNonNull(kind, "kind");
      this.entityId = entityId;
      this.programIdentifier = programIdentifier;
      this.callbackIdentifier = callbackIdentifier;
      this.kind = kind;
      this.cause = cause;
   }

   public static enum Kind {
      CALLBACK_EXCEPTION,
      RUNAWAY_LIMIT_EXCEEDED,
      @Deprecated
      CALLBACK_TIME_EXCEEDED,
      @Deprecated
      ENTITY_BUDGET_EXHAUSTED,
      @Deprecated
      SERVER_BUDGET_EXHAUSTED;
   }
}
