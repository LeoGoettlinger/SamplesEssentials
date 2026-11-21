package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.config.ProceduralItemGenerationSettingsConfig;
import com.magmaguy.elitemobs.items.ItemDurability;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.freeminecraftmodels.api.magic.MagicWeaponAPI;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public record ProceduralItemType(Material material, SkillType magicSkill) {
   public static final ProceduralItemType STAFF = new ProceduralItemType(
      Objects.requireNonNullElse(Material.getMaterial("WOODEN_SPEAR"), Material.STICK), SkillType.STAVES
   );
   public static final ProceduralItemType WAND = new ProceduralItemType(Material.BLAZE_ROD, SkillType.WANDS);

   public ProceduralItemType(Material material, SkillType magicSkill) {
      Objects.requireNonNull(material);
      if (magicSkill != null && magicSkill != SkillType.STAVES && magicSkill != SkillType.WANDS) {
         throw new IllegalArgumentException("Unsupported magic weapon skill: " + magicSkill);
      } else {
         this.material = material;
         this.magicSkill = magicSkill;
      }
   }

   public static ProceduralItemType vanilla(Material material) {
      return new ProceduralItemType(material, null);
   }

   public SkillType skill() {
      return this.magicSkill != null ? this.magicSkill : SkillType.fromMaterialIncludingArmor(this.material);
   }

   public String fmmItemId() {
      if (this.magicSkill == null) {
         return null;
      } else {
         return this.magicSkill == SkillType.STAVES ? "fmm_default_arcane_staff" : "fmm_default_arcane_wand";
      }
   }

   public boolean isAvailable() {
      if (this.magicSkill == null) {
         return true;
      } else if (this.magicSkill == SkillType.STAVES && !ProceduralItemGenerationSettingsConfig.isStavesEnabled()) {
         return false;
      } else if (this.magicSkill == SkillType.WANDS && !ProceduralItemGenerationSettingsConfig.isWandsEnabled()) {
         return false;
      } else if (!Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels")) {
         return false;
      } else {
         try {
            return MagicWeaponAPI.capabilityVersion() >= 6 && MagicWeaponAPI.isOperational() && MagicWeaponAPI.isWeapon(this.fmmItemId());
         } catch (LinkageError var2) {
            return false;
         }
      }
   }

   public boolean applyMagicData(ItemStack item) {
      if (this.magicSkill == null) {
         return true;
      } else if (!this.isAvailable()) {
         return false;
      } else {
         try {
            if (!MagicWeaponAPI.applyWeaponData(item, this.fmmItemId())) {
               return false;
            }
         } catch (LinkageError var3) {
            return false;
         }

         ItemDurability.prepareMagicWeapon(item);
         return true;
      }
   }
}
