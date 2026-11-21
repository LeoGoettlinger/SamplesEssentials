package com.magmaguy.elitemobs.advancedcombat.progression;

import java.util.Optional;

@FunctionalInterface
public interface ClassContentAvailability {
   ClassContentAvailability ALL_AVAILABLE = ignored -> Optional.empty();

   Optional<String> unavailableReason(String var1);
}
