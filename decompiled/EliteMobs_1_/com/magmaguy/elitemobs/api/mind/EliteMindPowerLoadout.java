package com.magmaguy.elitemobs.api.mind;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import org.bukkit.NamespacedKey;

public record EliteMindPowerLoadout(boolean randomized, List<NamespacedKey> powerKeys) {
   private static final EliteMindPowerLoadout RANDOMIZED = new EliteMindPowerLoadout(true, List.of());

   public EliteMindPowerLoadout(boolean randomized, List<NamespacedKey> powerKeys) {
      Objects.requireNonNull(powerKeys, "powerKeys");
      powerKeys = List.copyOf(powerKeys);
      if (randomized && !powerKeys.isEmpty()) {
         throw new IllegalArgumentException("A randomized power loadout cannot contain exact keys");
      } else if (new LinkedHashSet<>(powerKeys).size() != powerKeys.size()) {
         throw new IllegalArgumentException("An exact power loadout cannot contain duplicate keys");
      } else {
         this.randomized = randomized;
         this.powerKeys = powerKeys;
      }
   }

   public static EliteMindPowerLoadout randomizedPowers() {
      return RANDOMIZED;
   }

   public static EliteMindPowerLoadout exact(List<NamespacedKey> powerKeys) {
      return new EliteMindPowerLoadout(false, powerKeys);
   }
}
