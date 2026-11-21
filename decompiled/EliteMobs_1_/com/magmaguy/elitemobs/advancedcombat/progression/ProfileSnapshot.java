package com.magmaguy.elitemobs.advancedcombat.progression;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record ProfileSnapshot(
   UUID playerId,
   String selectedFormId,
   InputProfile selectedInputProfile,
   int catalogVersion,
   UUID lockedRunId,
   RunSelection lockedRunSelection,
   ActiveLineageSnapshot activeLineage,
   Map<String, FormProgressSnapshot> forms
) {
   public ProfileSnapshot(
      UUID playerId,
      String selectedFormId,
      InputProfile selectedInputProfile,
      int catalogVersion,
      UUID lockedRunId,
      RunSelection lockedRunSelection,
      ActiveLineageSnapshot activeLineage,
      Map<String, FormProgressSnapshot> forms
   ) {
      Objects.requireNonNull(playerId, "playerId");
      Objects.requireNonNull(selectedInputProfile, "selectedInputProfile");
      if (lockedRunId == null != (lockedRunSelection == null)) {
         throw new IllegalArgumentException("Run id and run selection must both be present or absent");
      } else {
         forms = Collections.unmodifiableMap(new LinkedHashMap<>(forms));
         this.playerId = playerId;
         this.selectedFormId = selectedFormId;
         this.selectedInputProfile = selectedInputProfile;
         this.catalogVersion = catalogVersion;
         this.lockedRunId = lockedRunId;
         this.lockedRunSelection = lockedRunSelection;
         this.activeLineage = activeLineage;
         this.forms = forms;
      }
   }

   public Optional<String> optionalSelectedFormId() {
      return Optional.ofNullable(this.selectedFormId);
   }

   public Optional<RunSelection> optionalLockedRunSelection() {
      return Optional.ofNullable(this.lockedRunSelection);
   }

   public Optional<UUID> optionalLockedRunId() {
      return Optional.ofNullable(this.lockedRunId);
   }

   public Optional<ActiveLineageSnapshot> optionalActiveLineage() {
      return Optional.ofNullable(this.activeLineage);
   }

   public Optional<String> activeFormId() {
      return this.lockedRunSelection != null ? Optional.of(this.lockedRunSelection.formId()) : this.optionalSelectedFormId();
   }

   public InputProfile activeInputProfile() {
      return this.lockedRunSelection == null ? this.selectedInputProfile : this.lockedRunSelection.inputProfile();
   }
}
