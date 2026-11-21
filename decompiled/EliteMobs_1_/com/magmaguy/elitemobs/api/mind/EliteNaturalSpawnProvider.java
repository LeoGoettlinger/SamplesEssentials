package com.magmaguy.elitemobs.api.mind;

import java.util.Optional;

@FunctionalInterface
public interface EliteNaturalSpawnProvider {
   Optional<EliteNaturalSpawnClaim> select(EliteNaturalSpawnContext var1);
}
