package com.magmaguy.elitemobs.advancedcombat.content;

import com.magmaguy.elitemobs.advancedcombat.abilities.FixedAbilityRegistry;
import com.magmaguy.elitemobs.advancedcombat.abilities.FixedAbilitySpec;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassCatalog;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassResourceType;
import com.magmaguy.elitemobs.advancedcombat.passives.FixedPassiveRegistry;
import com.magmaguy.elitemobs.advancedcombat.passives.PassiveProfile;
import com.magmaguy.elitemobs.advancedcombat.resources.ClassResourceDefinition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BuiltInClassContent {
   private static final double BASELINE_MAX_RESOURCE = 100.0;
   private static final double MANA_IN_COMBAT_PER_SECOND = 1.6666666666666667;
   private static final double MANA_OUT_OF_COMBAT_PER_SECOND = 1.6666666666666667;
   private static final int RESOLVE_MANA_RATE_PERCENT = 66;
   private static final int FURY_MANA_RATE_PERCENT = 55;
   private static final int FOCUS_MANA_RATE_PERCENT = 85;
   private static final int GRACE_MANA_RATE_PERCENT = 200;
   private static final ClassResourceDefinition.DamageFreeRecoveryBonus RANGER_FOCUSED_RECOVERY = new ClassResourceDefinition.DamageFreeRecoveryBonus(
      100L, 1.2941176470588236
   );
   private static final ClassResourceDefinition.NearbyRecoveryBonus PALADIN_NEARBY_ELITE_RECOVERY = new ClassResourceDefinition.NearbyRecoveryBonus(
      ClassResourceDefinition.NearbyRecoveryBonus.Target.ELITES, 15.0, 0.2, 5
   );
   private static final ClassResourceDefinition.NearbyRecoveryBonus CLERIC_NEARBY_PLAYER_RECOVERY = new ClassResourceDefinition.NearbyRecoveryBonus(
      ClassResourceDefinition.NearbyRecoveryBonus.Target.OTHER_PLAYERS, 15.0, 0.3, 4
   );
   public static final int PERSISTENCE_VERSION = 1;
   private static final Set<String> RETIRED_FORM_IDS = Set.of();
   private static final List<ClassTreeContribution> TREES = List.of(
      new ClassTreeContribution(
         "adventurer",
         BuiltInClassDefinitions.adventurerTree(),
         FixedAbilityRegistry.adventurerDefinitions(),
         Map.of("adventurer", new PassiveProfile(0.0, 0.0, -0.05, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)),
         resource(
            ClassResourceType.STAMINA,
            100.0,
            1.6666666666666667,
            1.6666666666666667,
            0.0,
            0.0,
            0.0,
            0L,
            0.0,
            0.0,
            0.0,
            0.0,
            ClassResourceDefinition.NearbyRecoveryBonus.NONE,
            ClassResourceDefinition.DamageFreeRecoveryBonus.NONE
         )
      ),
      new ClassTreeContribution(
         "paladin",
         BuiltInClassDefinitions.paladinTree(),
         FixedAbilityRegistry.paladinDefinitions(),
         FixedPassiveRegistry.paladinDefinitions(),
         resource(
            ClassResourceType.RESOLVE,
            0.0,
            manaRelativeRecovery(1.6666666666666667, 66),
            manaRelativeRecovery(1.6666666666666667, 66),
            0.0,
            45.0,
            0.0,
            0L,
            0.0,
            40.0,
            5.0,
            25.0,
            PALADIN_NEARBY_ELITE_RECOVERY,
            ClassResourceDefinition.DamageFreeRecoveryBonus.NONE
         )
      ),
      new ClassTreeContribution(
         "berserker",
         BuiltInClassDefinitions.berserkerTree(),
         FixedAbilityRegistry.berserkerDefinitions(),
         FixedPassiveRegistry.berserkerDefinitions(),
         resource(
            ClassResourceType.FURY,
            0.0,
            manaRelativeRecovery(1.6666666666666667, 55),
            manaRelativeRecovery(1.6666666666666667, 55),
            10.0,
            40.0,
            0.0,
            0L,
            0.0,
            0.0,
            0.0,
            0.0,
            ClassResourceDefinition.NearbyRecoveryBonus.NONE,
            ClassResourceDefinition.DamageFreeRecoveryBonus.NONE
         )
      ),
      new ClassTreeContribution(
         "ranger",
         BuiltInClassDefinitions.rangerTree(),
         FixedAbilityRegistry.rangerDefinitions(),
         FixedPassiveRegistry.rangerDefinitions(),
         resource(
            ClassResourceType.FOCUS,
            100.0,
            manaRelativeRecovery(1.6666666666666667, 85),
            manaRelativeRecovery(1.6666666666666667, 85),
            0.0,
            0.0,
            0.0,
            0L,
            0.0,
            0.0,
            0.0,
            0.0,
            ClassResourceDefinition.NearbyRecoveryBonus.NONE,
            RANGER_FOCUSED_RECOVERY
         )
      ),
      new ClassTreeContribution(
         "cleric",
         BuiltInClassDefinitions.clericTree(),
         FixedAbilityRegistry.clericDefinitions(),
         FixedPassiveRegistry.clericDefinitions(),
         resource(
            ClassResourceType.GRACE,
            100.0,
            manaRelativeRecovery(1.6666666666666667, 200),
            manaRelativeRecovery(1.6666666666666667, 200),
            0.0,
            0.0,
            0.0,
            0L,
            30.0,
            0.0,
            0.0,
            0.0,
            CLERIC_NEARBY_PLAYER_RECOVERY,
            ClassResourceDefinition.DamageFreeRecoveryBonus.NONE
         )
      ),
      new ClassTreeContribution(
         "spellcaster",
         BuiltInClassDefinitions.spellcasterTree(),
         FixedAbilityRegistry.spellcasterDefinitions(),
         FixedPassiveRegistry.spellcasterDefinitions(),
         resource(
            ClassResourceType.MANA,
            100.0,
            1.6666666666666667,
            1.6666666666666667,
            0.0,
            0.0,
            0.0,
            0L,
            0.0,
            0.0,
            0.0,
            0.0,
            ClassResourceDefinition.NearbyRecoveryBonus.NONE,
            ClassResourceDefinition.DamageFreeRecoveryBonus.NONE
         )
      )
   );
   private static final List<ClassFormDefinition> FORMS = aggregateForms();
   private static final Map<String, FixedAbilitySpec> ABILITIES = aggregateAbilities();
   private static final Map<String, PassiveProfile> PASSIVES = aggregatePassives();
   private static final Map<ClassResourceType, ClassResourceDefinition> RESOURCES = aggregateResources();
   private static final ClassCatalog CATALOG = ClassCatalog.create(1, FORMS, RETIRED_FORM_IDS);
   private static final FixedAbilityRegistry ABILITY_REGISTRY = FixedAbilityRegistry.create(CATALOG, ABILITIES);
   private static final FixedPassiveRegistry PASSIVE_REGISTRY = FixedPassiveRegistry.create(CATALOG, PASSIVES);

   private BuiltInClassContent() {
   }

   private static double manaRelativeRecovery(double manaPerSecond, int percent) {
      return manaPerSecond * percent / 100.0;
   }

   public static ClassCatalog catalog() {
      return CATALOG;
   }

   public static FixedAbilityRegistry abilityRegistry() {
      return ABILITY_REGISTRY;
   }

   public static FixedPassiveRegistry passiveRegistry() {
      return PASSIVE_REGISTRY;
   }

   public static Map<ClassResourceType, ClassResourceDefinition> resourceDefinitions() {
      return RESOURCES;
   }

   private static List<ClassFormDefinition> aggregateForms() {
      List<ClassFormDefinition> forms = new ArrayList<>();

      for (ClassTreeContribution tree : TREES) {
         forms.addAll(tree.forms());
      }

      return List.copyOf(forms);
   }

   private static Map<String, FixedAbilitySpec> aggregateAbilities() {
      Map<String, FixedAbilitySpec> abilities = new LinkedHashMap<>();

      for (ClassTreeContribution tree : TREES) {
         tree.abilities().forEach((id, spec) -> putUnique(abilities, id, spec, "ability"));
      }

      return Collections.unmodifiableMap(abilities);
   }

   private static Map<String, PassiveProfile> aggregatePassives() {
      Map<String, PassiveProfile> passives = new LinkedHashMap<>();

      for (ClassTreeContribution tree : TREES) {
         tree.passives().forEach((id, profile) -> putUnique(passives, id, profile, "passive"));
      }

      return Collections.unmodifiableMap(passives);
   }

   private static Map<ClassResourceType, ClassResourceDefinition> aggregateResources() {
      Map<ClassResourceType, ClassResourceDefinition> resources = new LinkedHashMap<>();

      for (ClassTreeContribution tree : TREES) {
         ClassResourceDefinition definition = tree.resource();
         if (resources.putIfAbsent(definition.type(), definition) != null) {
            throw new IllegalArgumentException("Duplicate resource contribution: " + definition.type());
         }
      }

      return Collections.unmodifiableMap(resources);
   }

   private static ClassResourceDefinition resource(
      ClassResourceType type,
      double initialAmount,
      double inCombatTickDelta,
      double outOfCombatTickDelta,
      double damageDealtFlatGain,
      double damageReceivedHealthEquivalentGain,
      double damageReceivedFlatChange,
      long recoveryDelayAfterDamageTicks,
      double healingHealthEquivalentGain,
      double preventedDamageHealthEquivalentGain,
      double tauntGainPerEnemy,
      double tauntGainCap,
      ClassResourceDefinition.NearbyRecoveryBonus nearbyRecoveryBonus,
      ClassResourceDefinition.DamageFreeRecoveryBonus damageFreeRecoveryBonus
   ) {
      return new ClassResourceDefinition(
         type,
         100.0,
         initialAmount,
         inCombatTickDelta,
         outOfCombatTickDelta,
         damageDealtFlatGain,
         damageReceivedHealthEquivalentGain,
         damageReceivedFlatChange,
         recoveryDelayAfterDamageTicks,
         healingHealthEquivalentGain,
         preventedDamageHealthEquivalentGain,
         tauntGainPerEnemy,
         tauntGainCap,
         nearbyRecoveryBonus,
         damageFreeRecoveryBonus
      );
   }

   private static <V> void putUnique(Map<String, V> target, String id, V value, String type) {
      if (target.putIfAbsent(id, value) != null) {
         throw new IllegalArgumentException("Duplicate " + type + " contribution: " + id);
      }
   }
}
