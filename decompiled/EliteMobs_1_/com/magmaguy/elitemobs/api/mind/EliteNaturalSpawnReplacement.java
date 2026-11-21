package com.magmaguy.elitemobs.api.mind;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import java.util.Objects;
import java.util.function.Consumer;

public record EliteNaturalSpawnReplacement(EliteMindSpawnRequest request, Consumer<EliteEntity> afterSpawn) implements EliteNaturalSpawnClaim {
   public EliteNaturalSpawnReplacement(EliteMindSpawnRequest request, Consumer<EliteEntity> afterSpawn) {
      Objects.requireNonNull(request, "request");
      Objects.requireNonNull(afterSpawn, "afterSpawn");
      this.request = request;
      this.afterSpawn = afterSpawn;
   }

   public EliteNaturalSpawnReplacement(EliteMindSpawnRequest request) {
      this(request, ignored -> {});
   }
}
