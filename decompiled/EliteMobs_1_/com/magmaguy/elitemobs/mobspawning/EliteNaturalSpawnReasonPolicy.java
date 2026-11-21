package com.magmaguy.elitemobs.mobspawning;

import java.util.EnumSet;
import java.util.Objects;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

final class EliteNaturalSpawnReasonPolicy {
   private static final EnumSet<SpawnReason> EXTERNALLY_OWNED = EnumSet.of(
      SpawnReason.SPAWNER_EGG, SpawnReason.DISPENSE_EGG, SpawnReason.COMMAND, SpawnReason.CUSTOM, SpawnReason.DEFAULT
   );
   private static final EnumSet<SpawnReason> PROVIDER_ELIGIBLE = EnumSet.complementOf(EXTERNALLY_OWNED);

   private EliteNaturalSpawnReasonPolicy() {
   }

   static boolean isProviderEligible(SpawnReason reason) {
      return PROVIDER_ELIGIBLE.contains(Objects.requireNonNull(reason, "reason"));
   }

   static EnumSet<SpawnReason> externallyOwnedReasons() {
      return EnumSet.copyOf(EXTERNALLY_OWNED);
   }

   static EnumSet<SpawnReason> providerEligibleReasons() {
      return EnumSet.copyOf(PROVIDER_ELIGIBLE);
   }
}
