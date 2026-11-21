package com.magmaguy.elitemobs.advancedcombat.progression;

import java.util.Objects;
import java.util.UUID;

public record StoredClassProfile(UUID playerId, String selectedFormId, String selectedInputId, int catalogVersion, int tutorialSkillsUsed) {
   public StoredClassProfile(UUID playerId, String selectedFormId, String selectedInputId, int catalogVersion) {
      this(playerId, selectedFormId, selectedInputId, catalogVersion, 0);
   }

   public StoredClassProfile(UUID playerId, String selectedFormId, String selectedInputId, int catalogVersion, int tutorialSkillsUsed) {
      Objects.requireNonNull(playerId, "playerId");
      tutorialSkillsUsed = new SkillTutorialProgress(tutorialSkillsUsed).usedSkills();
      this.playerId = playerId;
      this.selectedFormId = selectedFormId;
      this.selectedInputId = selectedInputId;
      this.catalogVersion = catalogVersion;
      this.tutorialSkillsUsed = tutorialSkillsUsed;
   }
}
