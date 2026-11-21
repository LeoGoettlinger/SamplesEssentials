package com.magmaguy.elitemobs.advancedcombat.classes;

import com.magmaguy.elitemobs.skills.SkillType;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;

public record ClassFormDefinition(
   String id,
   String displayName,
   ClassBand band,
   String parentId,
   FoundationSkillPair foundationSkills,
   RootClassKit rootKit,
   AbilityDefinition signature,
   AbilityDefinition utility,
   PassiveDefinition passive,
   List<SkillType> weaponAffinities
) {
   private static final Pattern ID_PATTERN = Pattern.compile("[a-z][a-z0-9_]*");
   private static final int PERSISTED_ID_MAX_LENGTH = 64;

   public ClassFormDefinition(
      String id,
      String displayName,
      ClassBand band,
      String parentId,
      FoundationSkillPair foundationSkills,
      RootClassKit rootKit,
      AbilityDefinition signature,
      AbilityDefinition utility,
      PassiveDefinition passive,
      List<SkillType> weaponAffinities
   ) {
      id = requireId(id);
      displayName = requireText(displayName, "displayName");
      band = Objects.requireNonNull(band, "band");
      foundationSkills = Objects.requireNonNull(foundationSkills, "foundationSkills");
      signature = requireSlot(signature, AbilitySlot.SIGNATURE);
      utility = requireSlot(utility, AbilitySlot.UTILITY);
      passive = Objects.requireNonNull(passive, "passive");
      weaponAffinities = List.copyOf(weaponAffinities);
      if (!weaponAffinities.isEmpty() && !weaponAffinities.contains(SkillType.ARMOR) && weaponAffinities.stream().distinct().count() == weaponAffinities.size()
         )
       {
         if (band.isRoot()) {
            if (band == ClassBand.STARTER && parentId != null) {
               throw new IllegalArgumentException("Starter form " + id + " must not have a parent");
            }

            if (parentId != null) {
               parentId = requireId(parentId);
            }

            rootKit = Objects.requireNonNull(rootKit, "rootKit");
         } else {
            parentId = requireId(parentId);
            if (rootKit != null) {
               throw new IllegalArgumentException("Specialization " + id + " must inherit its root kit");
            }
         }

         this.id = id;
         this.displayName = displayName;
         this.band = band;
         this.parentId = parentId;
         this.foundationSkills = foundationSkills;
         this.rootKit = rootKit;
         this.signature = signature;
         this.utility = utility;
         this.passive = passive;
         this.weaponAffinities = weaponAffinities;
      } else {
         throw new IllegalArgumentException("Weapon affinities require distinct weapon skills");
      }
   }

   public static ClassFormDefinition root(
      String id,
      String displayName,
      FoundationSkillPair foundationSkills,
      RootClassKit rootKit,
      AbilityDefinition signature,
      AbilityDefinition utility,
      PassiveDefinition passive
   ) {
      return new ClassFormDefinition(
         id, displayName, ClassBand.ROOT, null, foundationSkills, rootKit, signature, utility, passive, foundationWeapons(foundationSkills)
      );
   }

   public static ClassFormDefinition specialization(
      String id,
      String displayName,
      ClassBand band,
      String parentId,
      FoundationSkillPair foundationSkills,
      AbilityDefinition signature,
      AbilityDefinition utility,
      PassiveDefinition passive
   ) {
      if (band.isRoot()) {
         throw new IllegalArgumentException("A specialization cannot use the root band");
      } else {
         return new ClassFormDefinition(
            id, displayName, band, parentId, foundationSkills, null, signature, utility, passive, foundationWeapons(foundationSkills)
         );
      }
   }

   public ClassFormDefinition withWeaponAffinities(SkillType... weapons) {
      return new ClassFormDefinition(
         this.id, this.displayName, this.band, this.parentId, this.foundationSkills, this.rootKit, this.signature, this.utility, this.passive, List.of(weapons)
      );
   }

   public ClassFormDefinition afterStarter(String starterId) {
      if (this.band != ClassBand.ROOT) {
         throw new IllegalArgumentException("Only root classes can branch from a starter");
      } else {
         return new ClassFormDefinition(
            this.id,
            this.displayName,
            this.band,
            requireId(starterId),
            this.foundationSkills,
            this.rootKit,
            this.signature,
            this.utility,
            this.passive,
            this.weaponAffinities
         );
      }
   }

   private static List<SkillType> foundationWeapons(FoundationSkillPair skills) {
      return skills.asList().stream().filter(skill -> skill != SkillType.ARMOR).toList();
   }

   public Optional<String> optionalParentId() {
      return Optional.ofNullable(this.parentId);
   }

   public Optional<RootClassKit> optionalRootKit() {
      return Optional.ofNullable(this.rootKit);
   }

   public int requiredFoundationSkillLevel() {
      return this.band.skillUnlockLevel();
   }

   public boolean meetsFoundationRequirement(ToIntFunction<SkillType> levelProvider) {
      return this.effectiveSkillCap(levelProvider) >= this.requiredFoundationSkillLevel();
   }

   public int effectiveSkillCap(ToIntFunction<SkillType> levelProvider) {
      return this.foundationSkills.limitingLevel(levelProvider);
   }

   public int effectiveProgressionCap(ToIntFunction<SkillType> levelProvider) {
      int skillCap = this.effectiveSkillCap(levelProvider);
      int cap = Math.min(skillCap, this.band.effectiveEnd());
      return skillCap >= this.requiredFoundationSkillLevel() ? Math.max(this.band.effectiveStart(), cap) : cap;
   }

   public int localProgressionCap(ToIntFunction<SkillType> levelProvider) {
      int effectiveCap = this.effectiveProgressionCap(levelProvider);
      return effectiveCap < this.band.effectiveStart() ? 0 : this.band.toLocalLevel(effectiveCap);
   }

   private static AbilityDefinition requireSlot(AbilityDefinition ability, AbilitySlot slot) {
      Objects.requireNonNull(ability, slot.name().toLowerCase());
      if (ability.slot() != slot) {
         throw new IllegalArgumentException("Expected a " + slot + " ability, got " + ability.slot());
      } else {
         return ability;
      }
   }

   private static String requireId(String id) {
      String value = requireText(id, "id");
      if (value.length() > 64) {
         throw new IllegalArgumentException("Form id must not exceed 64 characters: " + value);
      } else if (!ID_PATTERN.matcher(value).matches()) {
         throw new IllegalArgumentException("Form id must be a stable lowercase identifier: " + value);
      } else {
         return value;
      }
   }

   private static String requireText(String value, String field) {
      Objects.requireNonNull(value, field);
      if (value.isBlank()) {
         throw new IllegalArgumentException(field + " must not be blank");
      } else if (!value.equals(value.strip())) {
         throw new IllegalArgumentException(field + " must not have surrounding whitespace");
      } else {
         return value;
      }
   }
}
