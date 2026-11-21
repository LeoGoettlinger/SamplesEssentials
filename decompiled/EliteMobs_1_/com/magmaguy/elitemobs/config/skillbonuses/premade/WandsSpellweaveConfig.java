package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import java.util.List;
import org.bukkit.Material;

public class WandsSpellweaveConfig extends SkillBonusConfigFields {
   public WandsSpellweaveConfig() {
      super(
         "wands_spellweave.yml",
         true,
         "&dSpellweave",
         List.of("&7Consecutive missiles on one", "&7target build damage. Stacks up to 8x."),
         SkillType.WANDS,
         2,
         8,
         0.03,
         4.0E-4,
         Material.STRING
      );
      this.loreTemplates = List.of("&7Per stack: &f+$bonusPerStack% damage", "&7Max stacks: &f$maxStacks", "&8Resets on a new target or after 3s");
      this.formattedBonusTemplate = "+$bonusPerStack% per stack (max $maxStacks)";
   }
}
