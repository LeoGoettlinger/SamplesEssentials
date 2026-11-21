package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import java.util.List;
import org.bukkit.Material;

public class SwordsExposeWeaknessConfig extends SkillBonusConfigFields {
   public SwordsExposeWeaknessConfig() {
      super(
         "swords_expose_weakness.yml",
         true,
         "&dExpose Weakness",
         List.of("&7Attacks have a chance to reduce", "&7the target's defense."),
         SkillType.SWORDS,
         2,
         0.1,
         0.002,
         0.2,
         Material.BONE
      );
      this.loreTemplates = List.of("&7Proc Chance: &f$procChance%", "&7Defense Reduction: &f$reduction%", "&7Duration: &f5 seconds");
      this.formattedBonusTemplate = "-$reduction% Target Defense";
   }
}
