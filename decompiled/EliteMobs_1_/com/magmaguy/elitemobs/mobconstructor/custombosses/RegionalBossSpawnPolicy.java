package com.magmaguy.elitemobs.mobconstructor.custombosses;

final class RegionalBossSpawnPolicy {
   private RegionalBossSpawnPolicy() {
   }

   static SpawnLifecycle.Context forPersistedState(long unixRespawnTime) {
      return unixRespawnTime > 0L ? SpawnLifecycle.Context.ANNOUNCED : SpawnLifecycle.Context.RESTORED;
   }
}
