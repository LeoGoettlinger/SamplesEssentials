package com.magmaguy.elitemobs.advancedcombat.progression;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record ActiveLineageSnapshot(
   String activeFormId, int activeLocalLevel, int activeEffectiveLevel, List<String> formIds, Map<String, Integer> contributionLevels
) {
   public ActiveLineageSnapshot(
      String activeFormId, int activeLocalLevel, int activeEffectiveLevel, List<String> formIds, Map<String, Integer> contributionLevels
   ) {
      Objects.requireNonNull(activeFormId, "activeFormId");
      formIds = List.copyOf(formIds);
      contributionLevels = Collections.unmodifiableMap(new LinkedHashMap<>(contributionLevels));
      this.activeFormId = activeFormId;
      this.activeLocalLevel = activeLocalLevel;
      this.activeEffectiveLevel = activeEffectiveLevel;
      this.formIds = formIds;
      this.contributionLevels = contributionLevels;
   }
}
