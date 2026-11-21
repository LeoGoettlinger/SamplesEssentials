package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import java.util.List;
import java.util.Map;
import org.bukkit.World.Environment;

public class EnchantmentChallenge3 extends ContentPackagesConfigFields {
   public EnchantmentChallenge3() {
      super(
         "enchantment_challenge_3_sanctum",
         true,
         "&2[lvl 000-200] &fEnchantment Challenge 03",
         List.of("&fAn enchantment challenge dungeon!"),
         "https://discord.gg/eSxvPbWYy4",
         ContentPackagesConfigFields.DungeonSizeCategory.SANCTUM,
         "em_id_enchantment_challenge_3",
         Environment.NORMAL,
         true,
         "em_id_enchantment_challenge_3,18.5,95,-15.5,45,0.0",
         "em_id_enchantment_challenge_3,14.5,65,-12.5,45,0",
         0,
         "Difficulty: &4solo hard content!",
         "&bChallenge time!",
         "&bYou have left the enchantment challenge!",
         List.of("filename=enchantment_boss_jealous_block.yml"),
         "em_id_enchantment_challenge_3",
         1,
         false
      );
      this.setDifficulties(List.of(Map.of("name", "normal", "id", 0)));
      this.setEnchantmentChallenge(true);
      this.setMaxPlayerCount(1);
      this.setNightbreakSlug("free-enchantment-challenge-sanctums");
   }
}
