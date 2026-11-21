package com.magmaguy.elitemobs.skills;

import lombok.Generated;
import org.bukkit.Material;

public enum SkillType {
   ARMOR("Armor", 0.3333333333333333),
   SWORDS("Swords", 1.0),
   AXES("Axes", 1.0),
   BOWS("Bows", 1.0),
   CROSSBOWS("Crossbows", 1.0),
   TRIDENTS("Tridents", 1.0),
   HOES("Hoes", 1.0),
   MACES("Maces", 1.0),
   SPEARS("Spears", 1.0),
   STAVES("Staves", 1.0),
   WANDS("Wands", 1.0);

   private final String displayName;
   private final double xpMultiplier;

   private SkillType(String displayName, double xpMultiplier) {
      this.displayName = displayName;
      this.xpMultiplier = xpMultiplier;
   }

   public static SkillType fromMaterial(Material material) {
      if (material == null) {
         return null;
      } else {
         String name = material.name();
         if (name.endsWith("_SWORD")) {
            return SWORDS;
         } else if (name.endsWith("_AXE")) {
            return AXES;
         } else if (name.endsWith("_HOE")) {
            return HOES;
         } else if (material == Material.BOW) {
            return BOWS;
         } else if (material == Material.CROSSBOW) {
            return CROSSBOWS;
         } else if (material == Material.TRIDENT) {
            return TRIDENTS;
         } else {
            try {
               if (material == Material.MACE) {
                  return MACES;
               }
            } catch (NoSuchFieldError var3) {
            }

            return name.endsWith("_SPEAR") ? SPEARS : null;
         }
      }
   }

   public static SkillType[] getWeaponSkills() {
      return new SkillType[]{SWORDS, AXES, BOWS, CROSSBOWS, TRIDENTS, HOES, MACES, SPEARS, STAVES, WANDS};
   }

   public static SkillType fromMaterialIncludingArmor(Material material) {
      if (material == null) {
         return null;
      } else {
         SkillType weaponType = fromMaterial(material);
         if (weaponType != null) {
            return weaponType;
         } else {
            String name = material.name();
            if (name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")) {
               return ARMOR;
            } else if (material == Material.ELYTRA) {
               return ARMOR;
            } else {
               return material == Material.TURTLE_HELMET ? ARMOR : null;
            }
         }
      }
   }

   public boolean isWeaponSkill() {
      return this != ARMOR;
   }

   public String getColumnName() {
      return "SkillXP_" + this.name();
   }

   @Generated
   public String getDisplayName() {
      return this.displayName;
   }

   @Generated
   public double getXpMultiplier() {
      return this.xpMultiplier;
   }
}
