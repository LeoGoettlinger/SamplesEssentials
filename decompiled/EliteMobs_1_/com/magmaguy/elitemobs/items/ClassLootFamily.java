package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.items.itemconstructor.ProceduralItemType;
import com.magmaguy.elitemobs.skills.SkillType;
import java.util.Locale;
import org.bukkit.Material;

public enum ClassLootFamily {
   SWORDS(SkillType.SWORDS, "Sword", "DIAMOND_SWORD"),
   AXES(SkillType.AXES, "Axe", "DIAMOND_AXE"),
   BOWS(SkillType.BOWS, "Bow", "BOW"),
   CROSSBOWS(SkillType.CROSSBOWS, "Crossbow", "CROSSBOW"),
   TRIDENTS(SkillType.TRIDENTS, "Trident", "TRIDENT"),
   HOES(SkillType.HOES, "Scythe", "DIAMOND_HOE"),
   MACES(SkillType.MACES, "Mace", "MACE"),
   SPEARS(SkillType.SPEARS, "Spear", "DIAMOND_SPEAR"),
   STAVES(SkillType.STAVES, "Staff", "WOODEN_SPEAR"),
   WANDS(SkillType.WANDS, "Wand", "BLAZE_ROD"),
   DPS_HELMETS(null, "DPS Helmet", "DIAMOND_HELMET"),
   DPS_CHESTPLATES(null, "DPS Chestplate", "DIAMOND_CHESTPLATE"),
   DPS_LEGGINGS(null, "DPS Leggings", "DIAMOND_LEGGINGS"),
   DPS_BOOTS(null, "DPS Boots", "DIAMOND_BOOTS"),
   TANK_HELMETS(null, "Tank Helmet", "DIAMOND_HELMET"),
   TANK_CHESTPLATES(null, "Tank Chestplate", "DIAMOND_CHESTPLATE"),
   TANK_LEGGINGS(null, "Tank Leggings", "DIAMOND_LEGGINGS"),
   TANK_BOOTS(null, "Tank Boots", "DIAMOND_BOOTS"),
   SHIELDS(null, "Shield", "SHIELD");

   private final SkillType skill;
   private final String label;
   private final String materialName;

   private ClassLootFamily(SkillType skill, String label, String materialName) {
      this.skill = skill;
      this.label = label;
      this.materialName = materialName;
   }

   public SkillType skill() {
      return this.skill;
   }

   public static ClassLootFamily resolve(Material material, SkillType weaponType, String explicitFamily) {
      ClassLootFamily inferred = null;
      if (weaponType == SkillType.STAVES) {
         inferred = STAVES;
      } else if (weaponType == SkillType.WANDS) {
         inferred = WANDS;
      } else if (material != null) {
         SkillType skill = SkillType.fromMaterialIncludingArmor(material);

         for (ClassLootFamily family : values()) {
            if (family.isWeapon() && family.skill() == skill) {
               inferred = family;
               break;
            }
         }

         if (material == Material.SHIELD) {
            inferred = SHIELDS;
         }
      }

      if (explicitFamily != null && !explicitFamily.isBlank()) {
         ClassLootFamily explicit = valueOf(explicitFamily.toUpperCase(Locale.ROOT));
         if (inferred != null && inferred == explicit) {
            return explicit;
         } else if (inferred == null
            && material != null
            && explicit.category() == ClassLootFamily.Category.ARMOR
            && material.name().endsWith("_" + explicit.slot())) {
            return explicit;
         } else {
            throw new IllegalArgumentException("classLootFamily " + explicit + " does not match material/weaponType");
         }
      } else if (inferred != null) {
         return inferred;
      } else {
         throw new IllegalArgumentException("classLootFamily is required for armor; use DPS_<slot> or TANK_<slot>");
      }
   }

   public ClassLootFamily.Category category() {
      return this.isWeapon() ? ClassLootFamily.Category.WEAPONS : (this == SHIELDS ? ClassLootFamily.Category.SHIELDS : ClassLootFamily.Category.ARMOR);
   }

   public String label() {
      return this.label;
   }

   public boolean isWeapon() {
      return this.skill != null;
   }

   public String slot() {
      return this.materialName.substring(this.materialName.lastIndexOf(95) + 1);
   }

   public Material material() {
      return this.magicType() == null ? Material.getMaterial(this.materialName) : this.magicType().material();
   }

   public ProceduralItemType magicType() {
      if (this == STAVES) {
         return ProceduralItemType.STAFF;
      } else {
         return this == WANDS ? ProceduralItemType.WAND : null;
      }
   }

   public static enum Category {
      WEAPONS,
      ARMOR,
      SHIELDS;
   }
}
