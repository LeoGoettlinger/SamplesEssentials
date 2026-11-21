package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import java.util.List;
import java.util.Map;
import org.bukkit.World.Environment;

public class EnchantmentChallenge7 extends ContentPackagesConfigFields {
   public EnchantmentChallenge7() {
      super(
         "enchantment_challenge_7_sanctum",
         true,
         "&2[lvl 000-200] &fEnchantment Challenge 07",
         List.of("&fAn enchantment challenge dungeon!"),
         "https://discord.gg/eSxvPbWYy4",
         ContentPackagesConfigFields.DungeonSizeCategory.SANCTUM,
         "em_id_enchantment_challenge_7",
         Environment.NORMAL,
         true,
         "em_id_enchantment_challenge_7,18.5,95,-15.5,45,0.0",
         "em_id_enchantment_challenge_7,14.5,65,-12.5,45,0",
         0,
         "Difficulty: &4solo hard content!",
         "&bChallenge time!",
         "&bYou have left the enchantment challenge!",
         List.of("filename=enchantment_boss_rock_solid_cold.yml"),
         "em_id_enchantment_challenge_7",
         1,
         false
      );
      this.setDifficulties(List.of(Map.of("name", "normal", "id", 0)));
      this.setEnchantmentChallenge(true);
      this.setMaxPlayerCount(1);
      this.setNightbreakSlug("free-enchantment-challenge-sanctums");
   }
}
