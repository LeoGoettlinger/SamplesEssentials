package com.magmaguy.elitemobs.magmacore.ai;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public interface MindHost {
   default MindHandle open(UUID logicalOwner, MindProgram program) {
      return this.open(logicalOwner, program, MindPersistentState.empty());
   }

   MindHandle open(UUID var1, MindProgram var2, MindPersistentState var3);

   MindBodyCapabilities bodyCapabilities();

   default MobBody spawnBody(Location location) {
      return this.spawnBody(location, MindBodyProfile.GROUNDED);
   }

   default MobBody spawnBody(Location location, MindBodyProfile profile) {
      return this.spawnBody(location, profile, body -> {});
   }

   MobBody spawnBody(Location var1, MindBodyProfile var2, Consumer<MobBody> var3);

   void tick();

   Optional<MindBodyRehydration> rehydrateBody(LivingEntity var1);
}
