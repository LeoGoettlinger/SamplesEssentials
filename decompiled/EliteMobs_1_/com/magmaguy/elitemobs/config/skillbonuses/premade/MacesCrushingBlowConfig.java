package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import java.util.List;
import org.bukkit.Material;

public class MacesCrushingBlowConfig extends SkillBonusConfigFields {
   private static final List<String> LEGACY_DESCRIPTION = List.of("&7Chance to ignore enemy", "&7armor on hit.");
   private static final List<String> DESCRIPTION = List.of("&7Chance to land a crushing hit", "&7that deals bonus damage.");
   private static final List<String> LEGACY_LORE = List.of("&7Proc Chance: &f$procChance%", "&7Armor Ignored: &f$armorIgnore%");
   private static final List<String> LORE = List.of("&7Proc Chance: &f$procChance%", "&7Damage Multiplier: &f$multiplierx");
   private static final String LEGACY_FORMATTED_BONUS = "$armorIgnore% Armor Pen (proc)";
   private static final String FORMATTED_BONUS = "$multiplierx Damage (proc)";

   public MacesCrushingBlowConfig() {
      super("maces_crushing_blow.yml", true, "&eCrushing Blow", DESCRIPTION, SkillType.MACES, 1, 0.25, 0.003, 0.12, Material.ANVIL);
      this.loreTemplates = LORE;
      this.formattedBonusTemplate = "$multiplierx Damage (proc)";
   }

   @Override
   protected void migrateLegacyDisplayDefaults() {
      this.migrateStringListIfExact("description", LEGACY_DESCRIPTION, DESCRIPTION);
      this.migrateStringListIfExact("loreTemplates", LEGACY_LORE, LORE);
      this.migrateStringIfExact("formattedBonusTemplate", "$armorIgnore% Armor Pen (proc)", "$multiplierx Damage (proc)");
   }
}
