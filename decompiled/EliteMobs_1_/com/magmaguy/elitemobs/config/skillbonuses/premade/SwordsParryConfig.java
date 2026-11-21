package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import java.util.List;
import org.bukkit.Material;

public class SwordsParryConfig extends SkillBonusConfigFields {
   public SwordsParryConfig() {
      super(
         "swords_parry.yml",
         true,
         "&aParry",
         List.of("&7Block attacks with your sword to", "&7reduce damage even further."),
         SkillType.SWORDS,
         SkillBonusType.CONDITIONAL,
         3,
         0.3,
         0.005,
         Material.IRON_SWORD
      );
      this.loreTemplates = List.of("&7Damage Reduction: &f$value%", "&7Condition: Blocking with sword", "&7Stacks with normal blocking");
      this.formattedBonusTemplate = "-$value% Damage when Parrying";
   }
}
