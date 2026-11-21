package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import java.util.List;
import org.bukkit.Material;

public class MacesShatterConfig extends SkillBonusConfigFields {
   public MacesShatterConfig() {
      super(
         "maces_shatter.yml",
         true,
         "&eShatter",
         List.of("&7Slam the ground, damaging", "&7and slowing nearby enemies. 15s CD."),
         SkillType.MACES,
         2,
         1.0,
         0.02,
         0.0,
         15.0,
         Material.CRACKED_STONE_BRICKS,
         true
      );
      this.loreTemplates = List.of(
         "&7Damage: &f$damage% weapon damage", "&7Radius: &f$radius blocks", "&7Slow Duration: &f3 seconds", "&7Cooldown: &f$cooldowns"
      );
      this.formattedBonusTemplate = "AoE $damage% (CD: $cooldowns)";
   }
}
