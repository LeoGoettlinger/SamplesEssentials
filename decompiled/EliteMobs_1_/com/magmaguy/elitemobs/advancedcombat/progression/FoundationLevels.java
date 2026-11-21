package com.magmaguy.elitemobs.advancedcombat.progression;

import java.util.Optional;
import java.util.UUID;

@FunctionalInterface
public interface FoundationLevels {
   Optional<FoundationLevelSnapshot> snapshot(UUID var1);
}
