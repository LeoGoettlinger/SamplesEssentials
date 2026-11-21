package com.magmaguy.elitemobs.advancedcombat.progression;

public record ClassProgressionForgetResult(ClassProgressionForgetResult.Status status, int formsReset, boolean selectionCleared) {
   public static enum Status {
      APPLIED,
      NOT_READY,
      UNKNOWN_FORM,
      RUN_LOCKED;
   }
}
