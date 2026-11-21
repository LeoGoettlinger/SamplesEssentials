package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import java.util.List;
import org.bukkit.Material;

public class AxesStaggerConfig extends SkillBonusConfigFields {
   public AxesStaggerConfig() {
      super(
         "axes_stagger.yml",
         true,
         "&eStagger",
         List.of("&7Chance to stagger enemies,", "&7slowing and weakening them."),
         SkillType.AXES,
         2,
         0.0,
         0.0,
         0.1,
         0.0,
         Material.SHIELD,
         false
      );
      this.loreTemplates = List.of("&7Proc Chance: &f$procChance%", "&7Effect: &fSlowness + Weakness", "&7Duration: &f2 seconds");
      this.formattedBonusTemplate = "$procChance% Stagger Chance";
   }
}
