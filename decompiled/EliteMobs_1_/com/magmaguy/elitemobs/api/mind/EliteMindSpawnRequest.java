package com.magmaguy.elitemobs.api.mind;

import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public record EliteMindSpawnRequest(
   Plugin owner,
   NamespacedKey programKey,
   Location location,
   int level,
   boolean persistent,
   EliteMindBodyProfile bodyProfile,
   EliteMindPowerLoadout powerLoadout
) {
   public EliteMindSpawnRequest(Plugin owner, NamespacedKey programKey, Location location, int level, boolean persistent) {
      this(owner, programKey, location, level, persistent, EliteMindBodyProfile.GROUNDED, EliteMindPowerLoadout.randomizedPowers());
   }

   public EliteMindSpawnRequest(Plugin owner, NamespacedKey programKey, Location location, int level, boolean persistent, EliteMindBodyProfile bodyProfile) {
      this(owner, programKey, location, level, persistent, bodyProfile, EliteMindPowerLoadout.randomizedPowers());
   }

   public EliteMindSpawnRequest(
      Plugin owner,
      NamespacedKey programKey,
      Location location,
      int level,
      boolean persistent,
      EliteMindBodyProfile bodyProfile,
      EliteMindPowerLoadout powerLoadout
   ) {
      Objects.requireNonNull(owner, "owner");
      Objects.requireNonNull(programKey, "programKey");
      Objects.requireNonNull(location, "location");
      Objects.requireNonNull(bodyProfile, "bodyProfile");
      Objects.requireNonNull(powerLoadout, "powerLoadout");
      if (location.getWorld() == null) {
         throw new IllegalArgumentException("location must have a world");
      } else if (level < 1) {
         throw new IllegalArgumentException("level must be positive");
      } else {
         location = location.clone();
         this.owner = owner;
         this.programKey = programKey;
         this.location = location;
         this.level = level;
         this.persistent = persistent;
         this.bodyProfile = bodyProfile;
         this.powerLoadout = powerLoadout;
      }
   }

   public Location location() {
      return this.location.clone();
   }
}
