package com.magmaguy.elitemobs.advancedcombat.progression;

import java.util.Objects;

public record RunSelection(String formId, InputProfile inputProfile) {
   public RunSelection(String formId, InputProfile inputProfile) {
      Objects.requireNonNull(formId, "formId");
      Objects.requireNonNull(inputProfile, "inputProfile");
      if (formId.isBlank()) {
         throw new IllegalArgumentException("formId must not be blank");
      } else {
         this.formId = formId;
         this.inputProfile = inputProfile;
      }
   }
}
