package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import java.util.List;
import org.bukkit.Material;

public class AxesDevastatingBlowConfig extends SkillBonusConfigFields {
   public AxesDevastatingBlowConfig() {
      super(
         "axes_devastating_blow.yml",
         true,
         "&4Devastating Blow",
         List.of("&7Chance to deal massive", "&7bonus damage on hit."),
         SkillType.AXES,
         1,
         2.0,
         0.05,
         0.08,
         0.0,
         Material.NETHERITE_AXE,
         false
      );
      this.loreTemplates = List.of("&7Proc Chance: &f$procChance%", "&7Damage: &f$damageMultiplierx");
      this.formattedBonusTemplate = "$damageMultiplierx Damage (proc)";
   }
}
