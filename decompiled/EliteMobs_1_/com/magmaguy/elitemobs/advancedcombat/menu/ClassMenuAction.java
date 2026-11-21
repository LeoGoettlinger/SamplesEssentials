package com.magmaguy.elitemobs.advancedcombat.menu;

import java.util.Objects;

sealed interface ClassMenuAction
   permits ClassMenuAction.OpenOverview,
   ClassMenuAction.OpenControls,
   ClassMenuAction.OpenForm,
   ClassMenuAction.SelectForm,
   ClassMenuAction.Challenge,
   ClassMenuAction.DeactivateClass {
   public record Challenge(String formId, double quotedFee) implements ClassMenuAction {
   }

   public record DeactivateClass() implements ClassMenuAction {
   }

   public record OpenControls() implements ClassMenuAction {
   }

   public record OpenForm(String formId, boolean showAllClasses) implements ClassMenuAction {
      public OpenForm(String formId, boolean showAllClasses) {
         Objects.requireNonNull(formId, "formId");
         this.formId = formId;
         this.showAllClasses = showAllClasses;
      }
   }

   public record OpenOverview() implements ClassMenuAction {
   }

   public record SelectForm(String formId, boolean showAllClasses) implements ClassMenuAction {
      public SelectForm(String formId, boolean showAllClasses) {
         Objects.requireNonNull(formId, "formId");
         this.formId = formId;
         this.showAllClasses = showAllClasses;
      }
   }
}
