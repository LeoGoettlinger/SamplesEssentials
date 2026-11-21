package com.magmaguy.elitemobs.advancedcombat.progression;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record RunLockResult(RunLockResult.Status status, UUID runId, RunSelection selection) {
   public RunLockResult(RunLockResult.Status status, UUID runId, RunSelection selection) {
      Objects.requireNonNull(status, "status");
      Objects.requireNonNull(runId, "runId");
      if ((status == RunLockResult.Status.LOCKED || status == RunLockResult.Status.ALREADY_LOCKED || status == RunLockResult.Status.LOCKED_BY_ANOTHER_RUN)
         != (selection != null)) {
         throw new IllegalArgumentException("Only lock-bearing results include a selection");
      } else {
         this.status = status;
         this.runId = runId;
         this.selection = selection;
      }
   }

   public boolean locked() {
      return this.status == RunLockResult.Status.LOCKED || this.status == RunLockResult.Status.ALREADY_LOCKED;
   }

   public Optional<RunSelection> optionalSelection() {
      return Optional.ofNullable(this.selection);
   }

   public static enum Status {
      LOCKED,
      ALREADY_LOCKED,
      LOCKED_BY_ANOTHER_RUN,
      NOT_READY,
      NO_SELECTED_FORM,
      FORM_LOCKED;
   }
}
