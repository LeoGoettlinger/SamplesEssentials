package com.magmaguy.elitemobs.advancedcombat.progression;

import java.util.Optional;

public record SelectionResult(SelectionResult.Status status, ProfileSnapshot snapshot) {
   public boolean accepted() {
      return this.status == SelectionResult.Status.APPLIED || this.status == SelectionResult.Status.UNCHANGED;
   }

   public Optional<ProfileSnapshot> optionalSnapshot() {
      return Optional.ofNullable(this.snapshot);
   }

   public static enum Status {
      APPLIED,
      UNCHANGED,
      NOT_READY,
      UNKNOWN_FORM,
      LOCKED_FORM;
   }
}
