package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import java.util.List;
import org.bukkit.Material;

public class StavesConcussiveBlastConfig extends SkillBonusConfigFields {
   public StavesConcussiveBlastConfig() {
      super(
         "staves_concussive_blast.yml",
         true,
         "&eConcussive Blast",
         List.of("&7Fireball hits can fling", "&7enemies away and slow them."),
         SkillType.STAVES,
         1,
         0.0,
         0.0,
         0.1,
         Material.FIRE_CHARGE
      );
      this.loreTemplates = List.of("&7Chance: &f$procChance%", "&7Knockback and Slowness II for 2s", "&8Fireball hits only");
      this.formattedBonusTemplate = "$procChance% knockback and slow";
   }
}
