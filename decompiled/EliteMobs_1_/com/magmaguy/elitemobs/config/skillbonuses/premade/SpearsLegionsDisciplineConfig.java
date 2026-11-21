package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import java.util.List;
import org.bukkit.Material;

public class SpearsLegionsDisciplineConfig extends SkillBonusConfigFields {
   public SpearsLegionsDisciplineConfig() {
      super(
         "spears_legions_discipline.yml",
         true,
         "&bLegion's Discipline",
         List.of("&7Maintain a steady combat pace", "&7to build stacking damage."),
         SkillType.SPEARS,
         3,
         10,
         0.025,
         1.5E-4,
         Material.IRON_BARS
      );
      this.loreTemplates = List.of(
         "&7Max Stacks: &f$maxStacks", "&7Damage/Stack: &f+$perStack%", "&7Max Bonus: &f+$maxBonus%", "&7Stacks fade after 2.5s without a hit"
      );
      this.formattedBonusTemplate = "+$maxBonus% Max (Discipline)";
   }
}
