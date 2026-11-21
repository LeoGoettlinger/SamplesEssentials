package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.advancedcombat.classes.ClassFormDefinition;
import java.util.Set;

public record ClassLootPreferences(ClassLootPreferences.Armor armor, boolean shields) {
   private static final Set<String> TANKS = Set.of(
      "paladin", "guardian", "aegis", "bulwark", "shieldbearer", "templar", "bannerlord", "deathless", "dreadnought", "colossus", "arcane_knight"
   );
   private static final Set<String> HYBRIDS = Set.of("justicar", "marshal", "juggernaut", "warmonger", "battlemage", "cryomancer");
   private static final Set<String> SHIELD_USERS = Set.of(
      "paladin", "guardian", "aegis", "bulwark", "shieldbearer", "justicar", "templar", "marshal", "bannerlord"
   );

   public static ClassLootPreferences defaults(String classId) {
      return new ClassLootPreferences(
         TANKS.contains(classId)
            ? ClassLootPreferences.Armor.TANK
            : (HYBRIDS.contains(classId) ? ClassLootPreferences.Armor.BOTH : ClassLootPreferences.Armor.DPS),
         SHIELD_USERS.contains(classId)
      );
   }

   public boolean relevant(ClassFormDefinition form, ClassLootFamily family) {
      if (family.isWeapon()) {
         return form.weaponAffinities().contains(family.skill());
      } else {
         return family == ClassLootFamily.SHIELDS
            ? this.shields
            : this.armor == ClassLootPreferences.Armor.BOTH || family.name().startsWith(this.armor.name() + "_");
      }
   }

   public static enum Armor {
      DPS,
      TANK,
      BOTH;
   }
}
