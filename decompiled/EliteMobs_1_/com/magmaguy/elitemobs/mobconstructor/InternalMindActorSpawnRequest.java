package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.api.mind.EliteMindBodyProfile;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.magmacore.ai.MindProgram;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public record InternalMindActorSpawnRequest(
   Plugin systemOwner,
   UUID playerOwnerId,
   Location location,
   int level,
   EliteMindBodyProfile bodyProfile,
   MindProgram program,
   Consumer<EliteEntity> initializer,
   CustomBossesConfigFields customBoss
) {
   public InternalMindActorSpawnRequest(
      Plugin owner, UUID player, Location location, int level, EliteMindBodyProfile profile, MindProgram program, Consumer<EliteEntity> initializer
   ) {
      this(owner, player, location, level, profile, program, initializer, null);
   }

   public InternalMindActorSpawnRequest(
      Plugin systemOwner,
      UUID playerOwnerId,
      Location location,
      int level,
      EliteMindBodyProfile bodyProfile,
      MindProgram program,
      Consumer<EliteEntity> initializer,
      CustomBossesConfigFields customBoss
   ) {
      Objects.requireNonNull(systemOwner, "systemOwner");
      Objects.requireNonNull(playerOwnerId, "playerOwnerId");
      Objects.requireNonNull(location, "location");
      Objects.requireNonNull(bodyProfile, "bodyProfile");
      Objects.requireNonNull(program, "program");
      Objects.requireNonNull(initializer, "initializer");
      if (location.getWorld() == null) {
         throw new IllegalArgumentException("location must have a world");
      } else if (level < 1) {
         throw new IllegalArgumentException("level must be positive");
      } else {
         NamespacedKey programKey = NamespacedKey.fromString(program.identifier());
         if (programKey == null) {
            throw new IllegalArgumentException("program has an invalid Bukkit key");
         } else {
            String ownerNamespace = new NamespacedKey(systemOwner, "ownership_probe").getNamespace();
            if (!ownerNamespace.equals(programKey.getNamespace())) {
               throw new IllegalArgumentException("program namespace does not belong to systemOwner");
            } else {
               location = location.clone();
               this.systemOwner = systemOwner;
               this.playerOwnerId = playerOwnerId;
               this.location = location;
               this.level = level;
               this.bodyProfile = bodyProfile;
               this.program = program;
               this.initializer = initializer;
               this.customBoss = customBoss;
            }
         }
      }
   }

   public Location location() {
      return this.location.clone();
   }
}
