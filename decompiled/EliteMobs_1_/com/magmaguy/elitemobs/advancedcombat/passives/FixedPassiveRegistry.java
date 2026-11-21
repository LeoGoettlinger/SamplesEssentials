package com.magmaguy.elitemobs.advancedcombat.passives;

import com.magmaguy.elitemobs.advancedcombat.classes.ClassCatalog;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassFormDefinition;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public final class FixedPassiveRegistry {
   private static final PassiveProfile DEFENDER = profile(-0.012, 0.0, -0.025, -3.5E-4, -0.01, 0.0, 0.0, 0.0, 0.0, 0.0);
   private static final PassiveProfile PROTECTOR = profile(-0.01, 0.0, -0.015, -2.5E-4, -0.01, 0.0, 0.01, 1.5E-4, 0.0, 0.0);
   private static final PassiveProfile COMMANDER = profile(0.01, 2.0E-4, 0.005, 0.0, 0.005, 1.0E-4, 0.0, 0.0, 0.01, 1.0E-4);
   private static final PassiveProfile BRUISER = profile(0.025, 3.5E-4, 0.018, 2.0E-4, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
   private static final PassiveProfile EXECUTIONER = profile(0.035, 4.0E-4, 0.012, 1.5E-4, 0.0, 0.0, -0.01, 0.0, 0.0, 0.0);
   private static final PassiveProfile SIEGE_SUPPORT = profile(-0.035, -2.5E-4, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
   private static final PassiveProfile JUGGERNAUT = profile(0.01, 1.5E-4, -0.018, -2.5E-4, -0.018, 0.0, 0.0, 0.0, 0.0, 0.0);
   private static final PassiveProfile MOBILE_RANGER = profile(0.015, 2.0E-4, 0.012, 1.0E-4, 0.025, 2.5E-4, 0.0, 0.0, 0.0, 0.0);
   private static final PassiveProfile MARKSMAN = profile(0.03, 3.5E-4, 0.01, 1.0E-4, -0.01, 0.0, 0.0, 0.0, 0.0, 0.0);
   private static final PassiveProfile CONTROLLER = profile(0.008, 1.0E-4, 0.008, 1.0E-4, 0.01, 1.2E-4, 0.0, 0.0, 0.015, 1.2E-4);
   private static final PassiveProfile HEALER = profile(-0.025, 0.0, 0.0, 0.0, 0.0, 0.0, 0.045, 5.0E-4, 0.0, 0.0);
   private static final PassiveProfile BARRIER_HEALER = profile(-0.015, 0.0, -0.008, -1.0E-4, -0.005, 0.0, 0.025, 3.0E-4, 0.01, 1.0E-4);
   private static final PassiveProfile SUSTAIN_HEALER = profile(-0.015, 0.0, 0.008, 8.0E-5, 0.0, 0.0, 0.035, 4.0E-4, 0.008, 8.0E-5);
   private static final PassiveProfile BATTLE_HEALER = profile(0.012, 1.5E-4, 0.01, 1.0E-4, 0.005, 8.0E-5, 0.018, 2.2E-4, 0.0, 0.0);
   private static final PassiveProfile SPELLCASTER = profile(0.015, 2.0E-4, 0.008, 0.0, 0.0, 0.0, 0.0, 0.0, 0.005, 8.0E-5);
   private static final PassiveProfile MAGE = profile(0.0, 0.0, 0.012, 1.0E-4, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
   private static final PassiveProfile ELEMENTALIST = profile(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
   private static final PassiveProfile PYROMANCER = profile(0.0, 0.0, 0.015, 1.0E-4, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
   private static final PassiveProfile CRYOMANCER = profile(0.0, 0.0, -0.015, -1.0E-4, 0.0, 0.0, 0.0, 0.0, 0.02, 1.5E-4);
   private static final PassiveProfile BATTLEMAGE = profile(0.01, 1.5E-4, -0.018, -2.0E-4, -0.01, 0.0, 0.0, 0.0, 0.0, 0.0);
   private static final PassiveProfile SPELLBLADE = profile(0.03, 3.0E-4, 0.005, 0.0, 0.015, 1.5E-4, 0.0, 0.0, 0.0, 0.0);
   private static final PassiveProfile ARCANE_KNIGHT = profile(-0.01, 0.0, -0.03, -2.5E-4, 0.0, 0.0, 0.0, 0.0, 0.012, 1.0E-4);
   private static final PassiveProfile OCCULTIST = profile(0.02, 2.5E-4, 0.0, 0.0, 0.0, 0.0, -0.01, 0.0, 0.012, 1.0E-4);
   private static final PassiveProfile NECROMANCER = profile(0.015, 2.0E-4, 0.0, 0.0, -0.008, 0.0, 0.025, 2.5E-4, 0.0, 0.0);
   private static final PassiveProfile LICH = profile(0.03, 3.5E-4, 0.0, 0.0, 0.0, 0.0, 0.03, 3.0E-4, 0.0, 0.0);
   private static final PassiveProfile PLAGUEBRINGER = profile(0.025, 3.0E-4, 0.0, 0.0, -0.012, 0.0, 0.0, 0.0, 0.018, 1.5E-4);
   private static final PassiveProfile SUMMONER = profile(0.0, 0.0, -0.012, -1.0E-4, 0.0, 0.0, 0.0, 0.0, 0.02, 2.0E-4);
   private static final PassiveProfile DEMONOLOGIST = profile(0.045, 4.0E-4, 0.025, 2.0E-4, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
   private static final PassiveProfile SPIRITBINDER = profile(0.0, 0.0, -0.008, 0.0, 0.0, 0.0, 0.03, 3.5E-4, 0.015, 1.5E-4);
   private final Map<String, PassiveProfile> profiles;
   private final Map<String, List<PassiveTrait>> traits;
   private final Map<String, List<PassiveMechanicTrait>> mechanicTraits;

   private FixedPassiveRegistry(
      Map<String, PassiveProfile> profiles, Map<String, List<PassiveTrait>> traits, Map<String, List<PassiveMechanicTrait>> mechanicTraits
   ) {
      this.profiles = Collections.unmodifiableMap(new LinkedHashMap<>(profiles));
      Map<String, List<PassiveTrait>> copy = new LinkedHashMap<>();
      traits.forEach((id, formTraits) -> copy.put(id, List.copyOf(formTraits)));
      this.traits = Collections.unmodifiableMap(copy);
      Map<String, List<PassiveMechanicTrait>> mechanicCopy = new LinkedHashMap<>();
      mechanicTraits.forEach((id, formTraits) -> mechanicCopy.put(id, List.copyOf(formTraits)));
      this.mechanicTraits = Collections.unmodifiableMap(mechanicCopy);
   }

   public static FixedPassiveRegistry create(ClassCatalog catalog, Map<String, PassiveProfile> definitions) {
      Objects.requireNonNull(catalog, "catalog");
      Map<String, PassiveProfile> profiles = new LinkedHashMap<>(Objects.requireNonNull(definitions, "definitions"));

      for (ClassFormDefinition form : catalog.forms()) {
         if (!profiles.containsKey(form.id())) {
            throw new IllegalArgumentException("No fixed passive mechanic for " + form.id());
         }
      }

      if (profiles.size() != catalog.forms().size()) {
         throw new IllegalArgumentException("Passive registry contains forms outside the catalog");
      } else {
         Map<String, List<PassiveTrait>> traits = traitDefinitions(profiles.keySet());
         if (!traits.keySet().equals(profiles.keySet())) {
            throw new IllegalArgumentException("Passive trait coverage does not match profile coverage");
         } else {
            Map<String, List<PassiveMechanicTrait>> mechanicTraits = mechanicDefinitions(profiles.keySet());
            if (!mechanicTraits.keySet().equals(profiles.keySet())) {
               throw new IllegalArgumentException("Passive mechanic trait coverage does not match profile coverage");
            } else {
               validatePromisedMechanics(mechanicTraits);
               return new FixedPassiveRegistry(profiles, traits, mechanicTraits);
            }
         }
      }
   }

   public static Map<String, PassiveProfile> paladinDefinitions() {
      Map<String, PassiveProfile> profiles = new LinkedHashMap<>();
      put(profiles, DEFENDER, "paladin", "guardian", "aegis", "bulwark", "shieldbearer");
      put(profiles, PROTECTOR, "justicar", "templar");
      put(profiles, COMMANDER, "warlord", "marshal", "bannerlord", "strategist");
      put(profiles, BRUISER, "conqueror", "champion");
      put(profiles, EXECUTIONER, "inquisitor", "tyrant");
      return Collections.unmodifiableMap(profiles);
   }

   public static Map<String, PassiveProfile> berserkerDefinitions() {
      Map<String, PassiveProfile> profiles = new LinkedHashMap<>();
      put(profiles, BRUISER, "berserker", "bloodrager", "bloodstorm", "reaver", "warmonger");
      put(profiles, EXECUTIONER, "slayer", "headsman", "harvester", "titanbane");
      put(profiles, SIEGE_SUPPORT, "siegebreaker");
      put(profiles, JUGGERNAUT, "deathless", "juggernaut", "crusher", "dreadnought", "colossus");
      return Collections.unmodifiableMap(profiles);
   }

   public static Map<String, PassiveProfile> rangerDefinitions() {
      Map<String, PassiveProfile> profiles = new LinkedHashMap<>();
      put(profiles, MOBILE_RANGER, "ranger", "skirmisher", "windrunner", "pathfinder", "tempest_archer");
      put(profiles, MARKSMAN, "sniper", "bowmaster", "deadeye", "raincaller", "arbalist", "artillerist", "dragonslayer");
      put(profiles, CONTROLLER, "saboteur", "trapper", "demolitionist");
      return Collections.unmodifiableMap(profiles);
   }

   public static Map<String, PassiveProfile> clericDefinitions() {
      Map<String, PassiveProfile> profiles = new LinkedHashMap<>();
      put(profiles, HEALER, "cleric", "priest", "hierophant", "saint");
      put(profiles, BATTLE_HEALER, "exorcist", "mistweaver");
      put(profiles, BARRIER_HEALER, "oracle", "fateweaver", "seraph", "spiritcaller", "soulwarden");
      put(profiles, SUSTAIN_HEALER, "shaman", "lifewarden", "grovekeeper", "shepherd");
      return Collections.unmodifiableMap(profiles);
   }

   public static Map<String, PassiveProfile> spellcasterDefinitions() {
      Map<String, PassiveProfile> profiles = new LinkedHashMap<>();
      put(profiles, SPELLCASTER, "spellcaster");
      put(profiles, MAGE, "mage");
      put(profiles, ELEMENTALIST, "elementalist");
      put(profiles, PYROMANCER, "pyromancer");
      put(profiles, CRYOMANCER, "cryomancer");
      put(profiles, BATTLEMAGE, "battlemage");
      put(profiles, SPELLBLADE, "spellblade");
      put(profiles, ARCANE_KNIGHT, "arcane_knight");
      put(profiles, OCCULTIST, "occultist");
      put(profiles, NECROMANCER, "necromancer");
      put(profiles, LICH, "lich");
      put(profiles, PLAGUEBRINGER, "plaguebringer");
      put(profiles, SUMMONER, "summoner");
      put(profiles, DEMONOLOGIST, "demonologist");
      put(profiles, SPIRITBINDER, "spiritbinder");
      return Collections.unmodifiableMap(profiles);
   }

   public PassiveProfile require(String formId) {
      PassiveProfile profile = this.profiles.get(formId);
      if (profile == null) {
         throw new IllegalArgumentException("No fixed passive mechanic for " + formId);
      } else {
         return profile;
      }
   }

   public List<PassiveTrait> traits(String formId) {
      List<PassiveTrait> formTraits = this.traits.get(formId);
      if (formTraits == null) {
         throw new IllegalArgumentException("No passive trait mapping for " + formId);
      } else {
         return formTraits;
      }
   }

   public Set<String> mappedFormIds() {
      return this.traits.keySet();
   }

   public List<PassiveMechanicTrait> mechanicTraits(String formId) {
      List<PassiveMechanicTrait> formTraits = this.mechanicTraits.get(formId);
      if (formTraits == null) {
         throw new IllegalArgumentException("No passive mechanic trait mapping for " + formId);
      } else {
         return formTraits;
      }
   }

   private static Map<String, List<PassiveMechanicTrait>> mechanicDefinitions(Set<String> formIds) {
      Map<String, List<PassiveMechanicTrait>> definitions = new LinkedHashMap<>();
      formIds.forEach(id -> definitions.put(id, List.of()));
      addMechanics(definitions, "guardian", mechanics(new PassiveMechanics(1.0, 1.0, 1.0, 1.0, 1.0, 0.9, 1.0, 1.0, 0.0, 0.0)));
      addMechanics(definitions, "aegis", mechanics(new PassiveMechanics(1.0, 1.0, 1.0, 1.0, 1.15, 0.88, 1.0, 1.0, 0.0, 0.0)));
      addMechanics(definitions, "juggernaut", mechanics(new PassiveMechanics(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.2, 0.0)));
      addMechanics(definitions, "dreadnought", mechanics(new PassiveMechanics(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.15, 0.0)));
      addMechanics(definitions, "pathfinder", mechanics(new PassiveMechanics(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.04)));
      addMechanics(definitions, "trapper", mechanics(new PassiveMechanics(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.2, 1.12, 0.0, 0.0)));
      addMechanics(definitions, "lifewarden", mechanics(new PassiveMechanics(0.9, 1.0, 1.0, 1.18, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0)));
      addMechanics(definitions, "bloodstorm", mechanics(new PassiveMechanics(1.0, 1.0, 1.12, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0)));
      addMechanics(
         definitions,
         "strategist",
         mechanics(PassiveCondition.GROUPED, new PassiveMechanics(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.97, 0.0, 0.0)),
         mechanics(PassiveCondition.SOLO, new PassiveMechanics(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.03, 0.0, 0.0))
      );
      addMechanics(definitions, "oracle", mechanics(new PassiveMechanics(1.0, 1.0, 1.0, 1.0, 1.1, 1.0, 1.0, 1.0, 0.0, 0.0)));
      return Collections.unmodifiableMap(definitions);
   }

   private static void validatePromisedMechanics(Map<String, List<PassiveMechanicTrait>> definitions) {
      requireMechanic(definitions, "guardian", mechanics -> mechanics.redirectedDamageMultiplier() < 1.0, "redirected damage reduction");
      requireMechanic(
         definitions,
         "aegis",
         mechanics -> mechanics.shieldStrengthMultiplier() > 1.0 && mechanics.redirectedDamageMultiplier() < 1.0,
         "shield and link strength"
      );
      requireMechanic(definitions, "juggernaut", mechanics -> mechanics.controlResistanceFraction() > 0.0, "control resistance");
      requireMechanic(definitions, "dreadnought", mechanics -> mechanics.controlResistanceFraction() > 0.0, "control resistance");
      requireMechanic(definitions, "pathfinder", mechanics -> mechanics.partyMovementSpeedAdjustment() > 0.0, "party movement");
      requireMechanic(
         definitions,
         "trapper",
         mechanics -> mechanics.controlDurationMultiplier() > 1.0 && mechanics.controlPotencyMultiplier() > 1.0,
         "control duration and potency"
      );
      requireMechanic(
         definitions,
         "lifewarden",
         mechanics -> mechanics.periodicDurationMultiplier() > 1.0 && mechanics.burstHealingMultiplier() < 1.0,
         "periodic duration and burst tradeoff"
      );
      requireMechanic(definitions, "bloodstorm", mechanics -> mechanics.groupedEnemyHealingMultiplier() > 1.0, "healing against enemy groups");
      requireMechanic(definitions, "strategist", mechanics -> mechanics.abilityCostMultiplier() != 1.0, "grouped and solo ability cost");
      requireMechanic(definitions, "oracle", mechanics -> mechanics.shieldStrengthMultiplier() > 1.0, "barrier strength");
   }

   private static void requireMechanic(
      Map<String, List<PassiveMechanicTrait>> definitions, String formId, Predicate<PassiveMechanics> requirement, String promise
   ) {
      boolean implemented = definitions.getOrDefault(formId, List.of()).stream().map(PassiveMechanicTrait::authoredAtLevelThirty).anyMatch(requirement);
      if (!implemented) {
         throw new IllegalArgumentException(formId + " promises " + promise + " without a typed mechanic");
      }
   }

   private static PassiveMechanicTrait mechanics(PassiveMechanics mechanics) {
      return new PassiveMechanicTrait(Set.of(PassiveCondition.ALWAYS), mechanics);
   }

   private static PassiveMechanicTrait mechanics(PassiveCondition condition, PassiveMechanics mechanics) {
      return new PassiveMechanicTrait(Set.of(condition), mechanics);
   }

   private static void addMechanics(Map<String, List<PassiveMechanicTrait>> definitions, String formId, PassiveMechanicTrait... additions) {
      if (definitions.containsKey(formId)) {
         definitions.put(formId, List.of(additions));
      }
   }

   private static Map<String, List<PassiveTrait>> traitDefinitions(Set<String> formIds) {
      Map<String, List<PassiveTrait>> definitions = new LinkedHashMap<>();
      formIds.forEach(id -> definitions.put(id, List.of()));
      add(definitions, "bulwark", trait(PassiveCondition.STANDING, 0.0, -0.12, 0.0, 0.0, 0.0, 0.0));
      add(definitions, "shieldbearer", trait(PassiveCondition.ALWAYS, -0.035, 0.0, 0.0, 0.0, 0.0, 0.0));
      add(definitions, "justicar", trait(PassiveCondition.ALWAYS, 0.0, 0.0, 0.0, 0.0, 0.0, -0.12));
      add(definitions, "templar", trait(PassiveCondition.ALWAYS, -0.025, 0.0, 0.0, 0.02, 0.0, 0.0));
      add(
         definitions,
         "inquisitor",
         trait(PassiveCondition.TARGET_ISOLATED, 0.1, 0.0, 0.0, 0.0, 0.0, 0.0),
         trait(PassiveCondition.ALWAYS, 0.0, 0.0, 0.0, 0.0, 0.0, -0.15)
      );
      add(definitions, "warlord", trait(PassiveCondition.GROUPED, 0.04, 0.02, 0.0, 0.0, 0.0, 0.0));
      add(
         definitions,
         "marshal",
         trait(PassiveCondition.GROUPED, 0.06, -0.05, 0.0, 0.0, 0.0, 0.0),
         trait(PassiveCondition.SOLO, -0.04, 0.04, 0.0, 0.0, 0.0, 0.0)
      );
      add(definitions, "bannerlord", trait(PassiveCondition.ALWAYS, -0.035, 0.0, 0.0, 0.0, 0.0, 0.0));
      add(definitions, "conqueror", trait(PassiveCondition.TARGET_CONTROLLED, 0.1, 0.0, 0.0, 0.0, 0.0, 0.0));
      add(
         definitions,
         "tyrant",
         trait(PassiveCondition.TARGET_CONTROLLED, 0.16, 0.0, 0.0, 0.0, 0.0, 0.0),
         trait(PassiveCondition.TARGET_UNCONTROLLED, -0.08, 0.0, 0.0, 0.0, 0.0, 0.0)
      );
      add(
         definitions,
         "bloodrager",
         trait(PassiveCondition.HEALTH_BELOW_75, 0.04, 0.03, 0.0, 0.0, 0.0, 0.0),
         trait(PassiveCondition.HEALTH_BELOW_50, 0.06, 0.04, 0.0, 0.0, 0.0, 0.0),
         trait(PassiveCondition.HEALTH_BELOW_25, 0.1, 0.06, 0.0, 0.0, 0.0, 0.0)
      );
      add(
         definitions,
         "reaver",
         trait(PassiveCondition.HEALTH_BELOW_50, 0.04, 0.02, 0.0, 0.0, 0.0, 0.0),
         trait(PassiveCondition.ALWAYS, 0.0, 0.0, 0.0, 0.08, 0.0, -0.1)
      );
      add(
         definitions,
         "bloodstorm",
         trait(EnumSet.of(PassiveCondition.TARGET_BOSS, PassiveCondition.TARGET_ISOLATED), -0.06, 0.0, 0.0, 0.0, 0.0, 0.0),
         trait(PassiveCondition.ALWAYS, 0.0, 0.0, 0.0, 0.06, 0.0, 0.0)
      );
      add(definitions, "deathless", trait(PassiveCondition.HEALTH_BELOW_25, 0.04, -0.16, 0.0, 0.0, 0.0, 0.0));
      add(definitions, "slayer", trait(PassiveCondition.TARGET_WOUNDED, 0.12, 0.0, 0.0, 0.0, 0.0, 0.0));
      add(
         definitions,
         "headsman",
         trait(EnumSet.of(PassiveCondition.TARGET_WOUNDED, PassiveCondition.TARGET_BOSS), 0.18, 0.0, 0.0, 0.0, 0.0, 0.0),
         trait(PassiveCondition.TARGET_GROUPED, -0.12, 0.0, 0.0, 0.0, 0.0, 0.0)
      );
      add(
         definitions,
         "harvester",
         trait(PassiveCondition.TARGET_WOUNDED, 0.1, 0.0, 0.0, 0.0, 0.0, 0.0),
         trait(PassiveCondition.TARGET_GROUPED, 0.08, 0.0, 0.0, 0.0, 0.0, 0.0),
         trait(EnumSet.of(PassiveCondition.TARGET_GROUPED, PassiveCondition.RECENT_ELITE_KILL), 0.1, 0.0, 0.0, 0.0, 0.0, 0.0),
         trait(EnumSet.of(PassiveCondition.TARGET_HEALTHY, PassiveCondition.TARGET_BOSS), -0.12, 0.0, 0.0, 0.0, 0.0, 0.0)
      );
      add(
         definitions,
         "titanbane",
         trait(PassiveCondition.TARGET_BOSS, 0.16, 0.0, 0.0, 0.0, 0.0, 0.0),
         trait(PassiveCondition.TARGET_ORDINARY, -0.06, 0.0, 0.0, 0.0, 0.0, 0.0),
         trait(PassiveCondition.TARGET_GROUPED, -0.12, 0.0, 0.0, 0.0, 0.0, 0.0)
      );
      add(definitions, "crusher", trait(PassiveCondition.CLOSE_RANGE, 0.1, -0.1, 0.0, 0.0, 0.0, 0.0));
      add(definitions, "dreadnought", trait(PassiveCondition.CRITICAL_HIT, -0.1, 0.0, 0.0, 0.0, 0.0, 0.0));
      add(
         definitions,
         "warmonger",
         trait(PassiveCondition.TARGET_GROUPED, 0.12, 0.0, 0.0, 0.0, 0.0, 0.0),
         trait(PassiveCondition.TARGET_ISOLATED, -0.08, 0.0, 0.0, 0.0, 0.0, 0.0)
      );
      add(
         definitions,
         "colossus",
         trait(PassiveCondition.STANDING, 0.12, -0.12, 0.0, 0.0, 0.0, 0.0),
         trait(PassiveCondition.MOVING, -0.08, 0.05, 0.0, 0.0, 0.0, 0.0)
      );
      add(
         definitions,
         "ranger",
         trait(PassiveCondition.NOT_RECENTLY_HIT, 0.0, 0.0, 0.05, 0.0, 0.0, 0.0),
         trait(PassiveCondition.RECENTLY_HIT, 0.0, 0.03, -0.08, 0.0, 0.0, 0.0)
      );
      add(
         definitions,
         "sniper",
         trait(PassiveCondition.LONG_RANGE, 0.1, 0.0, 0.0, 0.0, 0.0, 0.0),
         trait(PassiveCondition.ALWAYS, 0.0, 0.0, -0.025, 0.0, 0.0, 0.0)
      );
      add(
         definitions,
         "bowmaster",
         trait(PassiveCondition.LONG_RANGE, 0.13, 0.0, 0.0, 0.0, 0.0, 0.0),
         trait(PassiveCondition.CLOSE_RANGE, -0.09, 0.0, 0.0, 0.0, 0.0, 0.0)
      );
      add(definitions, "deadeye", trait(PassiveCondition.CRITICAL_HIT, 0.18, 0.0, 0.0, 0.0, 0.0, 0.0));
      add(
         definitions,
         "raincaller",
         trait(PassiveCondition.TARGET_GROUPED, 0.1, 0.0, 0.0, 0.0, 0.0, 0.0),
         trait(PassiveCondition.TARGET_ISOLATED, -0.08, 0.0, 0.0, 0.0, 0.0, 0.0)
      );
      add(definitions, "artillerist", trait(PassiveCondition.ALWAYS, -0.08, 0.0, 0.0, 0.0, 0.0, 0.0));
      add(
         definitions,
         "dragonslayer",
         trait(PassiveCondition.TARGET_BOSS, 0.18, 0.0, 0.0, 0.0, 0.0, 0.0),
         trait(PassiveCondition.TARGET_ORDINARY, -0.1, 0.0, 0.0, 0.0, 0.0, 0.0)
      );
      add(
         definitions,
         "skirmisher",
         trait(PassiveCondition.DRAWING_RANGED_WEAPON, 0.0, 0.0, 0.4, 0.0, 0.0, 0.0),
         trait(PassiveCondition.RECENTLY_HIT, 0.0, 0.04, -0.08, 0.0, 0.0, 0.0)
      );
      add(
         definitions,
         "windrunner",
         trait(PassiveCondition.MOVING, 0.12, 0.0, 0.0, 0.0, 0.0, 0.0),
         trait(PassiveCondition.STANDING, -0.08, 0.0, 0.0, 0.0, 0.0, 0.0)
      );
      add(definitions, "pathfinder", trait(PassiveCondition.ALWAYS, -0.06, 0.0, 0.0, 0.0, 0.0, 0.0));
      add(
         definitions,
         "tempest_archer",
         trait(EnumSet.of(PassiveCondition.MOVING, PassiveCondition.TARGET_GROUPED), 0.12, 0.0, 0.0, 0.0, 0.0, 0.0),
         trait(PassiveCondition.RECENTLY_HIT, 0.0, 0.09, 0.0, 0.0, 0.0, 0.0)
      );
      add(
         definitions,
         "saboteur",
         trait(PassiveCondition.TRAP_CLASS_ABILITY_DAMAGE, 0.14, 0.0, 0.0, 0.0, 0.0, 0.0),
         trait(PassiveCondition.RANGED_ATTACK, -0.07, 0.0, 0.0, 0.0, 0.0, 0.0)
      );
      add(definitions, "trapper", trait(PassiveCondition.ALWAYS, -0.07, 0.0, 0.0, 0.0, 0.0, 0.0));
      add(
         definitions,
         "demolitionist",
         trait(PassiveCondition.BLAST_CLASS_ABILITY_DAMAGE, 0.16, 0.0, 0.0, 0.0, 0.0, 0.0),
         trait(PassiveCondition.CLOSE_RANGE, 0.0, 0.1, 0.0, 0.0, 0.0, 0.0)
      );
      add(
         definitions,
         "hierophant",
         trait(PassiveCondition.GROUPED, 0.0, 0.0, 0.0, 0.08, 0.0, 0.0),
         trait(PassiveCondition.SOLO, 0.0, 0.0, 0.0, -0.06, 0.0, 0.0),
         trait(PassiveCondition.ALWAYS, 0.0, 0.0, 0.0, 0.04, 0.0, 0.0)
      );
      add(definitions, "saint", trait(PassiveCondition.ALWAYS, -0.06, 0.0, 0.0, 0.08, 0.0, 0.0));
      add(definitions, "exorcist", trait(PassiveCondition.ALWAYS, 0.05, 0.0, 0.0, -0.025, 0.0, 0.0));
      add(definitions, "oracle", trait(PassiveCondition.ALWAYS, 0.0, -0.04, 0.0, 0.03, 0.0, 0.0));
      add(
         definitions, "seraph", trait(PassiveCondition.GROUPED, 0.0, -0.04, 0.03, 0.0, 0.0, 0.0), trait(PassiveCondition.ALWAYS, 0.0, 0.0, 0.0, 0.05, 0.0, 0.0)
      );
      add(definitions, "shaman", trait(PassiveCondition.ALWAYS, 0.0, 0.0, 0.0, 0.06, 0.0, 0.0));
      add(definitions, "lifewarden", trait(PassiveCondition.ALWAYS, 0.0, 0.0, 0.0, 0.07, 0.0, 0.0));
      add(
         definitions,
         "grovekeeper",
         trait(PassiveCondition.STANDING, 0.0, -0.06, 0.0, 0.04, 0.0, 0.0),
         trait(PassiveCondition.MOVING, 0.0, 0.04, 0.0, 0.0, 0.0, 0.0)
      );
      add(
         definitions,
         "shepherd",
         trait(PassiveCondition.GROUPED, 0.0, 0.0, 0.0, 0.1, 0.0, 0.0),
         trait(PassiveCondition.SOLO, 0.0, 0.0, 0.0, -0.08, 0.0, 0.0),
         trait(PassiveCondition.ALWAYS, 0.0, 0.0, 0.0, 0.04, 0.0, 0.0)
      );
      add(definitions, "spiritcaller", trait(PassiveCondition.ALWAYS, 0.0, 0.06, 0.0, 0.04, 0.0, 0.0));
      add(definitions, "mistweaver", trait(PassiveCondition.ALWAYS, 0.03, 0.0, 0.0, 0.03, 0.0, 0.0));
      add(
         definitions,
         "soulwarden",
         trait(PassiveCondition.GROUPED, 0.0, -0.08, 0.0, 0.0, 0.0, 0.0),
         trait(PassiveCondition.SOLO, 0.0, 0.04, 0.0, 0.0, 0.0, 0.0),
         trait(PassiveCondition.ALWAYS, 0.0, 0.0, 0.0, 0.035, 0.0, 0.0)
      );
      add(
         definitions,
         "battlemage",
         trait(PassiveCondition.CLOSE_RANGE, 0.04, -0.06, 0.0, 0.0, 0.0, 0.0),
         trait(PassiveCondition.LONG_RANGE, -0.03, 0.03, 0.0, 0.0, 0.0, 0.0)
      );
      add(definitions, "mage", trait(PassiveCondition.SPELL_DAMAGE, 0.04, 0.0, 0.0, 0.0, 0.0, 0.0));
      add(
         definitions,
         "elementalist",
         trait(PassiveCondition.AREA_CLASS_ABILITY_DAMAGE, 0.0385, 0.0, 0.0, 0.0, 0.0, 0.0),
         trait(PassiveCondition.CLOSE_RANGE, 0.0, 0.08, 0.0, 0.0, 0.0, 0.0)
      );
      add(definitions, "pyromancer", trait(PassiveCondition.SPELL_DAMAGE, 0.05, 0.0, 0.0, 0.0, 0.0, 0.0));
      add(definitions, "cryomancer", trait(PassiveCondition.SPELL_DAMAGE, -0.01, 0.0, 0.0, 0.0, 0.0, 0.0));
      add(definitions, "necromancer", trait(PassiveCondition.ALWAYS, 0.0, 0.0, -0.02, 0.05, 0.0, 0.0));
      add(
         definitions,
         "lich",
         trait(PassiveCondition.ALWAYS, 0.04, 0.0, 0.0, 0.05, 0.0, 0.0),
         trait(PassiveCondition.WARD_BROKEN, 0.0, 0.08, 0.0, 0.0, 0.0, 0.0)
      );
      add(definitions, "plaguebringer", trait(PassiveCondition.ALWAYS, 0.04, 0.0, -0.02, 0.0, 0.03, 0.0));
      add(definitions, "demonologist", trait(PassiveCondition.ALWAYS, 0.08, 0.08, 0.0, 0.0, 0.0, 0.0));
      add(definitions, "summoner", trait(PassiveCondition.SPELL_DAMAGE, -0.02, 0.0, 0.0, 0.0, 0.0, 0.0));
      add(
         definitions,
         "spiritbinder",
         trait(PassiveCondition.SPELL_DAMAGE, -0.055, 0.0, 0.0, 0.0, 0.0, 0.0),
         trait(PassiveCondition.ALWAYS, 0.0, 0.0, 0.0, 0.07, 0.03, 0.0)
      );
      return Collections.unmodifiableMap(definitions);
   }

   private static void add(Map<String, List<PassiveTrait>> definitions, String formId, PassiveTrait... additions) {
      if (definitions.containsKey(formId)) {
         definitions.put(formId, List.of(additions));
      }
   }

   private static PassiveTrait trait(
      PassiveCondition condition, double outgoing, double incoming, double movement, double healingDone, double costReduction, double healingReceived
   ) {
      return trait(EnumSet.of(condition), outgoing, incoming, movement, healingDone, costReduction, healingReceived);
   }

   private static PassiveTrait trait(
      Set<PassiveCondition> conditions, double outgoing, double incoming, double movement, double healingDone, double costReduction, double healingReceived
   ) {
      return new PassiveTrait(
         conditions,
         new PassiveProfile(
            traitBase(outgoing),
            traitPerLevel(outgoing),
            traitBase(incoming),
            traitPerLevel(incoming),
            traitBase(movement),
            traitPerLevel(movement),
            traitBase(healingDone),
            traitPerLevel(healingDone),
            traitBase(costReduction),
            traitPerLevel(costReduction),
            traitBase(healingReceived),
            traitPerLevel(healingReceived)
         )
      );
   }

   private static double traitBase(double authoredAtThirty) {
      return authoredAtThirty * 0.7;
   }

   private static double traitPerLevel(double authoredAtThirty) {
      return authoredAtThirty * 0.01;
   }

   private static void put(Map<String, PassiveProfile> profiles, PassiveProfile profile, String... ids) {
      for (String id : ids) {
         if (profiles.putIfAbsent(id, profile) != null) {
            throw new IllegalArgumentException("Duplicate passive mechanic for " + id);
         }
      }
   }

   private static PassiveProfile profile(
      double outgoingBase,
      double outgoingPerLevel,
      double incomingBase,
      double incomingPerLevel,
      double speedBase,
      double speedPerLevel,
      double healingBase,
      double healingPerLevel,
      double costReductionBase,
      double costReductionPerLevel
   ) {
      return new PassiveProfile(
         outgoingBase,
         outgoingPerLevel,
         incomingBase,
         incomingPerLevel,
         speedBase,
         speedPerLevel,
         healingBase,
         healingPerLevel,
         costReductionBase,
         costReductionPerLevel,
         0.0,
         0.0
      );
   }
}
