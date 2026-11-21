package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import java.util.List;
import org.bukkit.Material;

public class MacesJudgmentConfig extends SkillBonusConfigFields {
   public MacesJudgmentConfig() {
      super(
         "maces_judgment.yml",
         true,
         "&eJudgment",
         List.of("&7Mark enemies for judgment.", "&7Marked targets take bonus damage."),
         SkillType.MACES,
         2,
         0.4,
         0.005,
         0.2,
         0.0,
         Material.SUNFLOWER,
         false
      );
      this.loreTemplates = List.of("&7Chance: &f$procChance%", "&7Judgment Damage: &f+$damageBonus%", "&7Duration: &f10 seconds", "&7Judged enemies glow");
      this.formattedBonusTemplate = "+$damageBonus% vs Judged";
   }
}
