package com.magmaguy.elitemobs.advancedcombat.minions;

import com.magmaguy.elitemobs.api.mind.EliteMindBodyLocomotion;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.bukkit.entity.EntityType;

public enum ClassMinionTheme {
   UNDEAD(
      false,
      24.0,
      List.of(
         carrier(EntityType.ZOMBIE, EliteMindBodyLocomotion.GROUNDED),
         carrier(EntityType.SKELETON, EliteMindBodyLocomotion.GROUNDED),
         carrier(EntityType.HUSK, EliteMindBodyLocomotion.GROUNDED)
      )
   ),
   ANIMAL(
      false,
      20.0,
      List.of(
         carrier(EntityType.WOLF, EliteMindBodyLocomotion.GROUNDED),
         carrier(EntityType.FOX, EliteMindBodyLocomotion.GROUNDED),
         carrier(EntityType.POLAR_BEAR, EliteMindBodyLocomotion.GROUNDED)
      )
   ),
   NETHER(
      false,
      30.0,
      List.of(
         carrier(EntityType.PIGLIN_BRUTE, EliteMindBodyLocomotion.GROUNDED),
         carrier(EntityType.HOGLIN, EliteMindBodyLocomotion.GROUNDED),
         carrier(EntityType.BLAZE, EliteMindBodyLocomotion.FLYING)
      )
   ),
   SPIRIT(true, 18.0, List.of(carrier(EntityType.VEX, EliteMindBodyLocomotion.FLYING)));

   private final boolean supportRole;
   private final double baseHealth;
   private final List<ClassMinionTheme.Carrier> carriers;

   private ClassMinionTheme(boolean supportRole, double baseHealth, List<ClassMinionTheme.Carrier> carriers) {
      this.supportRole = supportRole;
      this.baseHealth = baseHealth;
      this.carriers = List.copyOf(carriers);
   }

   public boolean supportRole() {
      return this.supportRole;
   }

   public double baseHealth() {
      return this.baseHealth;
   }

   public List<ClassMinionTheme.Carrier> carriers() {
      return this.carriers;
   }

   public ClassMinionTheme.Carrier carrierFor(int sequence) {
      return this.carriers.get(Math.floorMod(sequence, this.carriers.size()));
   }

   public static Optional<ClassMinionTheme> forAbility(String abilityId) {
      if (abilityId == null) {
         return Optional.empty();
      } else {
         String normalized = abilityId.toLowerCase(Locale.ROOT);
         if (normalized.equals("necromancer.signature") || normalized.equals("lich.signature") || normalized.equals("plaguebringer.signature")) {
            return Optional.of(UNDEAD);
         } else if (normalized.equals("summoner.signature")) {
            return Optional.of(ANIMAL);
         } else if (normalized.equals("demonologist.signature")) {
            return Optional.of(NETHER);
         } else {
            return normalized.equals("spiritbinder.signature") ? Optional.of(SPIRIT) : Optional.empty();
         }
      }
   }

   private static ClassMinionTheme.Carrier carrier(EntityType entityType, EliteMindBodyLocomotion locomotion) {
      return new ClassMinionTheme.Carrier(entityType, locomotion);
   }

   public record Carrier(EntityType entityType, EliteMindBodyLocomotion locomotion) {
      public Carrier(EntityType entityType, EliteMindBodyLocomotion locomotion) {
         if (entityType == null || locomotion == null) {
            throw new IllegalArgumentException("Carrier is incomplete");
         } else if (entityType.isAlive() && entityType.isSpawnable()) {
            this.entityType = entityType;
            this.locomotion = locomotion;
         } else {
            throw new IllegalArgumentException("Carrier must be a spawnable living entity: " + entityType);
         }
      }
   }
}
